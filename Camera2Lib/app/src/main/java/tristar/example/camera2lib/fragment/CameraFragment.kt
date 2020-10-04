package tristar.example.camera2lib.fragment

import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.camera2lib.R
import kotlinx.coroutines.suspendCancellableCoroutine
import tristar.example.camera2lib.cameraController.CameraParams
import tristar.example.camera2lib.cameraController.cameraCallback.CameraStateCallback

class CameraFragment : Fragment(){
    /** anroidx navigation arguments **/
    //private val args: CameraFragmentArgs by navArgs() 추후 argument 필요할 시 추
    private val navController: NavController by lazy{
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    /**
     * 1. Camera Device setting(RGB, DEPTH)
     * 2. Surface setting - recorder surface for record video(RGB, DEPTH)
     *                    - preview surface(RGB)
     * 3. File write setting
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** first open "0" camera **/

    }
}