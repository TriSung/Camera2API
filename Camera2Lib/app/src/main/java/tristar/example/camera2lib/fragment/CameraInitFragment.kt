package tristar.example.camera2lib.fragment

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import tristar.example.camera2lib.MainActivity
import com.example.camera2lib.R
import tristar.example.camera2lib.cameraController.CameraParams
import java.util.*


class CameraInitFragment : Fragment() {
    private val navController: NavController by lazy{
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeCamera()
        Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            CameraInitFragmentDirections.actionCameraInitFragmentToCameraFragment()
        )
    }
    private fun initializeCamera(){
        val context = requireContext().applicationContext
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        /**
         * HAL3 적용 이후 스마트폰에 여러 카메라가 장착된 경우
         * 여러개의 camera device 를 가지고 있음.
         * 때문에 이를 원하는 대로 사용하기 위해 미리 로드해놓음.
         */
        try{
            MainActivity.numCameras = manager.cameraIdList.size
            for(cameraId in manager.cameraIdList){
                val id: String = cameraId
                MainActivity.params[cameraId] = initializeParams(manager, cameraId)
            }

        } catch (accessError : CameraAccessException){
            accessError.printStackTrace()
        }
    }

    private fun initializeParams(manager: CameraManager, cameraId: String) : CameraParams {
        return CameraParams().apply {
            val cameraChars = manager.getCameraCharacteristics(cameraId)
            val scaleMap = cameraChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val cameraCapabilities = cameraChars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            if(cameraCapabilities != null){
                for(cap in cameraCapabilities){
                    when(cap){
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> hasMulti = true
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> isDepth = true
                    }
                }
            }
            id = cameraId
            isOpen = false
            characteristics = cameraChars
            this.manager = manager

            focalLength = cameraChars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)!!
            apertures = cameraChars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)!!
            physicalCameras = cameraChars.physicalCameraIds

            minSize = Size(640, 480)
            maxSize = Size(640, 480)
        }
    }
}

/**
 * Compares two `Size`s based on their areas.
 */
internal class CompareSizesByArea : Comparator<Size> {
    override fun compare(lhs: Size, rhs: Size): Int {
        // We cast here to ensure the multiplications won't overflow
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}