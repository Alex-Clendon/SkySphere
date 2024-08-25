package com.skysphere.skysphere.ui.savedLocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skysphere.skysphere.databinding.FragmentSavedlocationBinding

class SavedLocationFragment : Fragment() {

    private var _binding: FragmentSavedlocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val savedLocationViewModel =
            ViewModelProvider(this).get(SavedLocationViewModel::class.java)

        _binding = FragmentSavedlocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSavedLocation
        savedLocationViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}