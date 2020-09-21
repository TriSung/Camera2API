package com.example.camera2lib.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.camera2lib.R

class CameraFragment : Fragment(){
    /** anroidx navigation arguments **/
    //private val args: CameraFragmentArgs by navArgs() 추후 argument 필요할 시 추
    private val navController: NavController by lazy{
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }
    // TODO:
    /**
     * 1. Camera Device setting(RGB, DEPTH)
     * 2. Surface setting - recorder surface for record video(RGB, DEPTH)
     *                    - preview surface(RGB)
     * 3. File write setting
     *
     */
}