package com.skysphere.skysphere.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skysphere.skysphere.R
import com.skysphere.skysphere.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        val weatherCode: ImageView = binding.ivWeatherIcon
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Fetch weather data (you might want to get these values from user's location)
        homeViewModel.fetchDataFromAPI()

        // Observe weather type for icon
        homeViewModel.weatherType.observe(viewLifecycleOwner) { weatherType ->
            weatherCode.setImageResource(weatherType.iconRes)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}