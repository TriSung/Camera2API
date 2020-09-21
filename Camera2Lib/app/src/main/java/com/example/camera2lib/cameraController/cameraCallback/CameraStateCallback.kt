package com.example.camera2lib.cameraController.cameraCallback

import android.hardware.camera2.CameraDevice
import com.example.camera2lib.MainActivity

class CameraStateCallback(internal var activity: MainActivity) : CameraDevice.StateCallback() {
    override fun onOpened(camera: CameraDevice) {
        // after open a camera
        TODO("Not yet implemented")
    }

    override fun onClosed(camera: CameraDevice) {
        super.onClosed(camera)
    }

    override fun onDisconnected(camera: CameraDevice) {
        TODO("Not yet implemented")
    }

    override fun onError(camera: CameraDevice, error: Int) {
        TODO("Not yet implemented")
    }
}