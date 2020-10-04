package tristar.example.camera2lib.fragment

import android.annotation.SuppressLint
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import tristar.example.camera2lib.MainActivity
import tristar.example.camera2lib.cameraController.CameraParams
import tristar.example.camera2lib.utils.getPreviewOutputSize
import kotlin.coroutines.resume

class StillPictureFragment : Fragment() {
    /** Readers used as buffers for camera still shot */
    private lateinit var imageReader: ImageReader
    private lateinit var camera: CameraDevice
    private val cameraParams: CameraParams = MainActivity.params["0"]!!

    init{
        cameraParams.cameraThread = HandlerThread("CameraThread").apply{ start() }
        cameraParams.cameraHandler = Handler(cameraParams.cameraThread!!.looper)

        cameraParams.imageReaderThread = HandlerThread("ImageReaderThread").apply{ start() }
        cameraParams.imageReaderHandler = Handler(cameraParams.imageReaderThread!!.looper)

        cameraParams.previewSurface = surfaceView
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
    }

    private fun setCamera() = lifecycleScope.launch(Dispatchers.Main){
        camera = openCamera(cameraParams)
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
}