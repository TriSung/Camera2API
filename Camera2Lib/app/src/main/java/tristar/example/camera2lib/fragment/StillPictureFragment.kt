package tristar.example.camera2lib.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.camera2lib.R
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import kotlinx.android.synthetic.main.fragment_camera_preview.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import tristar.example.camera2lib.MainActivity
import tristar.example.camera2lib.cameraController.CameraParams
import tristar.example.camera2lib.utils.AutoFitSurfaceView
import tristar.example.camera2lib.utils.OrientationLiveData
import tristar.example.camera2lib.utils.computeExifOrientation
import tristar.example.camera2lib.utils.getPreviewOutputSize
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StillPictureFragment : Fragment() {
    private val navController: NavController by lazy{
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }
    /** Readers used as buffers for camera still shot */
    private lateinit var imageReader: ImageReader
    private lateinit var camera: CameraDevice

    private val cameraParams: CameraParams = MainActivity.params[MainActivity.selectedCameraId]!!
    private lateinit var surfacePreview: AutoFitSurfaceView

    private val animationTask : Runnable by lazy{
        Runnable{
            overlay.background = Color.argb(150, 255, 255, 255).toDrawable()
            overlay.postDelayed({
                overlay.background = null
            }, 50L)
        }
    }

    private lateinit var overlay: View

    private lateinit var relativeOrientation: OrientationLiveData


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraParams.cameraThread = HandlerThread("CameraThread").apply{ start() }
        cameraParams.cameraHandler = Handler(cameraParams.cameraThread!!.looper)

        cameraParams.imageReaderThread = HandlerThread("ImageReaderThread").apply{ start() }
        cameraParams.imageReaderHandler = Handler(cameraParams.imageReaderThread!!.looper)

        overlay = view.findViewById(R.id.overlay)

        surfacePreview = view.findViewById(R.id.preview_rgb)
        cameraParams.previewSurface = surfacePreview
        cameraParams.previewSurface!!.holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder?) {
                val previewSize = getPreviewOutputSize(
                    cameraParams.previewSurface!!.display, cameraParams.characteristics, SurfaceHolder::class.java
                )

                cameraParams.previewSurface!!.setAspectRatio(previewSize.width, previewSize.height)

                setCamera()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) = Unit
        })

        relativeOrientation = OrientationLiveData(requireContext(), cameraParams.characteristics).apply{
            observe(viewLifecycleOwner, Observer{
                orientation -> MainActivity.log("Orientation", "Orientation Changed: $orientation")
            })
        }
    }

    private fun setCamera() = lifecycleScope.launch(Dispatchers.Main){
        camera = openCamera(cameraParams)

        /** Reader for capture image */
        imageReader = ImageReader.newInstance(cameraParams.maxSize.width, cameraParams.maxSize.height, ImageFormat.JPEG, 3)

        /** 카메라를 통해 들어오는 값을 임시로 저장하는 버퍼 리스트 */
        val targets = listOf(surfacePreview.holder.surface, imageReader.surface)

        cameraParams.session = createCaptureSession(camera, targets, cameraParams.cameraHandler)

        val captureRequest = camera.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply{
            addTarget(surfacePreview.holder.surface)
        }

        cameraParams.session?.setRepeatingRequest(captureRequest.build(), null, cameraParams.cameraHandler)

        btn_capture.setOnClickListener{
            it.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO){
                takePhoto().use{result ->
                    val output = saveResult(result)
                    MainActivity.log("Save Photo", output.absolutePath)
                }
            }

            it.post { it.isEnabled = true }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(cameraParams: CameraParams) : CameraDevice = suspendCancellableCoroutine{ cont->
        cameraParams.manager.openCamera(cameraParams.id, object : CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) = cont.resume(camera)

            override fun onDisconnected(camera: CameraDevice) {
                MainActivity.log("StillPicture", "Cam disconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                val msg = when(error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                MainActivity.log("StillPicture", msg)
            }
        }, cameraParams.cameraHandler)
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->
        device.createCaptureSession(targets, object: CameraCaptureSession.StateCallback(){
            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                MainActivity.log("StillPicture", "Camera configure Failed")
                val exc = RuntimeException("Camera session configure failed")
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    private suspend fun takePhoto():
            CombinedCaptureResult = suspendCoroutine {  cont->
        @Suppress("ControlFlowWithEmptyBody")
        while(imageReader.acquireNextImage() != null) {}

        val imageQueue = ArrayBlockingQueue<Image>(3)
        imageReader.setOnImageAvailableListener({reader ->
            val image = reader.acquireNextImage()
            imageQueue.add(image)
        }, cameraParams.imageReaderHandler)

        val captureRequest = cameraParams.session?.device!!.createCaptureRequest(
            CameraDevice.TEMPLATE_STILL_CAPTURE
        ).apply{
            addTarget(imageReader.surface)
        }

        cameraParams.session?.capture(captureRequest.build(), object: CameraCaptureSession.CaptureCallback(){
            override fun onCaptureStarted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                timestamp: Long,
                frameNumber: Long
            ) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)
                surfacePreview.post(animationTask)
            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                MainActivity.log("Capture Finished", resultTimestamp.toString())

                val exc = TimeoutException("Image dequeueing took too long")
                val timeoutRunnable = Runnable{ cont.resumeWithException(exc)}
                cameraParams.imageReaderHandler?.postDelayed(timeoutRunnable, 5000L)

                @Suppress("BlockingMethodInNonBlockingContext")
                lifecycleScope.launch(cont.context){
                    while(true){
                        val image = imageQueue.take()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            image.format != ImageFormat.DEPTH_JPEG &&
                            image.timestamp != resultTimestamp) continue
                        MainActivity.log("Take Photo", "Matching image dequeued: ${image.timestamp}")

                        // Unset the image reader listener
                        cameraParams.imageReaderHandler?.removeCallbacks(timeoutRunnable)
                        imageReader.setOnImageAvailableListener(null, null)

                        // Clear the queue of images, if there are left
                        while (imageQueue.size > 0) {
                            imageQueue.take().close()
                        }

                        // Compute EXIF orientation metadata
                        val rotation = relativeOrientation.value ?: 0
                        val mirrored = cameraParams.characteristics.get(CameraCharacteristics.LENS_FACING) ==
                                CameraCharacteristics.LENS_FACING_FRONT
                        val exifOrientation = computeExifOrientation(rotation, mirrored)

                        // Build the result and resume progress
                        cont.resume(CombinedCaptureResult(
                            image, result, exifOrientation, imageReader.imageFormat))

                    }
                }
            }
        }, cameraParams.cameraHandler)
    }

    private suspend fun saveResult(result: CombinedCaptureResult): File = suspendCoroutine { cont ->
        when(result.format){
            ImageFormat.JPEG -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply{ buffer.get(this) }

                try{
                    val output = createFile(requireContext(), "jpg")
                    FileOutputStream(output).use { it.write(bytes)}
                    cont.resume(output)
                } catch(exc: IOException){
                    MainActivity.log("Save Result", exc.toString())
                    cont.resumeWithException(exc)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            camera.close()
        } catch (exc: Throwable) {
            MainActivity.log("Fragment Stop" ,"Error closing camera" + exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraParams.cameraThread?.quitSafely()
        cameraParams.imageReaderThread?.quitSafely()
    }


    companion object{
        data class CombinedCaptureResult(
            val image: Image,
            val metadata: CaptureResult,
            val orientation: Int,
            val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        private fun createFile(context: Context, extension: String): File {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_${sdf.format(Date())}.$extension")
        }
    }
}