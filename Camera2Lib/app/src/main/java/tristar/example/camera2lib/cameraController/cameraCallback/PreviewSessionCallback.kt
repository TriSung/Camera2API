package tristar.example.camera2lib.cameraController.cameraCallback

import android.hardware.camera2.CameraCaptureSession
import tristar.example.camera2lib.MainActivity
import tristar.example.camera2lib.cameraController.CameraParams

class PreviewSessionCallback(val cameraParams: CameraParams) : CameraCaptureSession.StateCallback() {
    override fun onConfigured(session: CameraCaptureSession) {
        TODO("Not yet implemented")
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        TODO("Not yet implemented")
    }

    override fun onActive(session: CameraCaptureSession) {
        if(!cameraParams.isOpen)
            return
        super.onActive(session)
    }

    override fun onReady(session: CameraCaptureSession) {
        if(!cameraParams.isOpen)
            return
        super.onReady(session)
    }

}