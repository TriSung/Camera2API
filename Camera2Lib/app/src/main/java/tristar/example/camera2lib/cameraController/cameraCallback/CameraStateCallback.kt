//package tristar.example.camera2lib.cameraController.cameraCallback
//
//import android.hardware.camera2.CameraDevice
//import tristar.example.camera2lib.MainActivity
//import tristar.example.camera2lib.cameraController.CameraParams
//
//class CameraStateCallback(internal val cameraParams: CameraParams) : CameraDevice.StateCallback() {
//    override fun onOpened(camera: CameraDevice) {
//        // after open a camera
//        cameraParams.isOpen = true
//        cameraParams.camera = camera
//    }
//
//    override fun onClosed(camera: CameraDevice) {
//        super.onClosed(camera)
//    }
//
//    override fun onDisconnected(camera: CameraDevice) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onError(camera: CameraDevice, error: Int) {
//        val msg = when(error) {
//            ERROR_CAMERA_DEVICE -> "Fatal (device)"
//            ERROR_CAMERA_DISABLED -> "Device policy"
//            ERROR_CAMERA_IN_USE -> "Camera in use"
//            ERROR_CAMERA_SERVICE -> "Fatal (service)"
//            ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
//            else -> "Unknown"
//        }
//        MainActivity.log("CamState", "msg")
//    }
//}