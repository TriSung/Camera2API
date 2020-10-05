package tristar.example.camera2lib.fragment

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tristar.example.camera2lib.MainActivity
import com.example.camera2lib.R
import tristar.example.camera2lib.cameraController.CameraParams
import tristar.example.camera2lib.utils.GenericListAdapter
import java.util.*


class CameraInitFragment : Fragment() {
    private val navController: NavController by lazy{
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = RecyclerView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view as RecyclerView
        view.apply{
            layoutManager = LinearLayoutManager(requireContext())

            val layoutId = android.R.layout.simple_list_item_1
            val cameraList: MutableList<FormatItem> = mutableListOf()
            cameraList.add(FormatItem("Facing_Still", "1", ImageFormat.YUV_420_888))
            cameraList.add(FormatItem("Back_Still", "0", ImageFormat.YUV_420_888))
            cameraList.add(FormatItem("Facing_Video", "1", MediaRecorder.OutputFormat.MPEG_4))
            cameraList.add(FormatItem("Back_Video", "0", MediaRecorder.OutputFormat.MPEG_4))
            cameraList.add(FormatItem("Back_Depth", "4", -1))
            adapter = GenericListAdapter(cameraList, itemLayoutId = layoutId){ view, item, _ ->
                view.findViewById<TextView>(android.R.id.text1).text = item.title

                view.setOnClickListener{
                    MainActivity.selectedCameraId = item.cameraId
                    if(item.title == "Facing_Still" || item.title == "Back_Still"){
                        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(CameraInitFragmentDirections.actionCameraInitFragmentToStillPictureFragment())
                    }
                    else if(item.title == "Facing_Video" || item.title == "Back_Video"){
                        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(CameraInitFragmentDirections.actionCameraInitFragmentToVideoFragment())
                    }
                    else{
                        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(CameraInitFragmentDirections.actionCameraInitFragmentToDepthFragment())
                    }
                }
            }
        }
    }

    companion object{
        private data class FormatItem(val title: String, val cameraId: String, val format: Int)
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