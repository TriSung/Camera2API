package tristar.example.camera2lib.cameraController

import android.hardware.camera2.CameraCharacteristics
import android.os.Handler
import android.os.HandlerThread
import android.util.Size

class CameraParams{
    lateinit var id: String // id is non-nullable variable

    internal var hasMulti: Boolean = false // if logical multi camera is supported, this value set true
        internal var physicalCameras: Set<String> = HashSet<String>() // set for multi physical camera. When hasMulti is true, use this variable

    internal var isOpen: Boolean = false // check camera device is open
    lateinit var characteristics: CameraCharacteristics // camera characteristics.
        internal var isDepth: Boolean = false // check this camera is depth camera, if false: RGB
        lateinit var focalLength: FloatArray // check focal length for image processing
        lateinit var apertures: FloatArray // check aperture for image processing
        /** initialize when capture format is adopted*/
        internal var minSize: Size = Size(-1,-1)
        internal var maxSize: Size = Size(-1,-1)

    /** these variables are not used unconditionally */
    internal var backgroundThread: HandlerThread? = null
    internal var backgroundHandler: Handler? = null // Thread & Handler for background tasking
}