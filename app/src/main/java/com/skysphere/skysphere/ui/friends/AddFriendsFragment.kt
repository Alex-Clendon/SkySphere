package com.skysphere.skysphere.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R

class AddFriendsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)


        return view
    }
}