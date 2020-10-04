package tristar.example.camera2lib.cameraController.cameraCallback

import android.hardware.camera2.*
import android.view.Surface
import tristar.example.camera2lib.MainActivity
import tristar.example.camera2lib.cameraController.CameraParams

class CaptureSessionCallback(val cameraParams: CameraParams) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureStarted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        timestamp: Long,
        frameNumber: Long
    ) {
        if(!cameraParams.isOpen)
            return
        super.onCaptureStarted(session, request, timestamp, frameNumber)
    }

    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        if(!cameraParams.isOpen)
            return
        /** Camera Preview Session reopen **/
        super.onCaptureCompleted(session, request, result)
    }

    override fun onCaptureProgressed(
        session: CameraCaptureSession,
        request: CaptureRequest,
        partialResult: CaptureResult
    ) {
        if(!cameraParams.isOpen)
            return
        super.onCaptureProgressed(session, request, partialResult)
    }

    override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
        super.onCaptureSequenceAborted(session, sequenceId)
    }

    override fun onCaptureFailed(
        session: CameraCaptureSession,
        request: CaptureRequest,
        failure: CaptureFailure
    ) {
        if(!cameraParams.isOpen)
            return
        super.onCaptureFailed(session, request, failure)
    }

    override fun onCaptureBufferLost(
        session: CameraCaptureSession,
        request: CaptureRequest,
        target: Surface,
        frameNumber: Long
    ) {
        if(!cameraParams.isOpen)
            return
        super.onCaptureBufferLost(session, request, target, frameNumber)
    }
}