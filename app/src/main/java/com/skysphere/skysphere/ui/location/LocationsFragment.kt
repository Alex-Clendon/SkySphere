package com.skysphere.skysphere.ui.location

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.skysphere.skysphere.R
import com.skysphere.skysphere.databinding.FragmentLocationsBinding
import com.skysphere.skysphere.ui.adapters.LocationsAdapter
import com.skysphere.skysphere.ui.adapters.NewsAdapter
import com.skysphere.skysphere.view_models.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager

@AndroidEntryPoint // This annotation enables Hilt's dependency injection in this Fragment
class LocationsFragment : Fragment() {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationsAdapter: LocationsAdapter

    // Use Hilt to inject the ViewModel
    @Inject
    lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)

        // Initialize the RecyclerView and Adapter
        locationsAdapter = LocationsAdapter()
        binding.locationsRecyclerView.adapter = locationsAdapter
        binding.locationsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe locations from the ViewModel
        locationViewModel.locations.observe(viewLifecycleOwner) { locations ->
            locationsAdapter.updateLocations(locations)
            Log.d("LOCATIONSMODELDEBUG", "Updated locations: $locations")
        }

        // Fetch locations when the fragment is created
        locationViewModel.fetchLocations()

        // Navigation to map fragment
        val navController = findNavController()
        binding.addLocationButton.setOnClickListener {
            navController.navigate(R.id.nav_map)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

