package com.skysphere.skysphere.ui.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.skysphere.skysphere.R
import com.skysphere.skysphere.databinding.FragmentLocationsBinding


class LocationsFragment : Fragment(){

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val navController = findNavController()
        // Inflate the layout using view binding
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        binding.addLocationButton.setOnClickListener {
            navController.navigate(R.id.nav_map)
        }

        return binding.root
    }
}