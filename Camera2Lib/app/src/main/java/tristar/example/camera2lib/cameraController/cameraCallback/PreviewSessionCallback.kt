package tristar.example.camera2lib.cameraController.cameraCallback

import android.hardware.camera2.CameraCaptureSession
import tristar.example.camera2lib.MainActivity

class PreviewSessionCallback(val activity: MainActivity) : CameraCaptureSession.StateCallback() {
    override fun onConfigured(session: CameraCaptureSession) {
        TODO("Not yet implemented")
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        TODO("Not yet implemented")
    }

    override fun onActive(session: CameraCaptureSession) {
        super.onActive(session)
    }

    override fun onReady(session: CameraCaptureSession) {
        super.onReady(session)
    }

}