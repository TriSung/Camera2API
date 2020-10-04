package tristar.example.camera2lib.cameraController

import androidx.lifecycle.ViewModel

class CamViewModel : ViewModel() {
    private var cameraParams: HashMap<String, CameraParams> = HashMap<String, CameraParams>()

    fun getCameraParams(): HashMap<String, CameraParams> {
        return cameraParams
    }
}