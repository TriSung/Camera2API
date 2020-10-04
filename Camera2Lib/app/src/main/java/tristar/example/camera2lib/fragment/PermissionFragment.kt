package tristar.example.camera2lib.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.camera2lib.R
import tristar.example.camera2lib.MainActivity

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO
)

class PermissionFragment : Fragment(){
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(hasPermissions(requireContext())){
            // permission already granted
            // fragment change, 나중에 실제 앱 구성할땐 초기 화면으로 가게 세팅하면 됨.
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                PermissionFragmentDirections.actionPermissionFragmentToCameraInitFragment()
            )
        }
        else{
            // require permissions about not granted
            requestPermissions(
                PERMISSIONS_REQUIRED,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // fragment change
                // 위에서와 마찬가지로 실제 앱을 구성할땐 초기 화면으로 가도록
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    PermissionFragmentDirections.actionPermissionFragmentToCameraInitFragment()
                )
            }
            else{
                Toast.makeText(context, "Permission Request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object{
        /** Convenience method for check permission **/
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all{
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
