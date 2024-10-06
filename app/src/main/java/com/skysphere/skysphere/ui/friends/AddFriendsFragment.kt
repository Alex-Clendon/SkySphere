package com.skysphere.skysphere.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.R

class AddFriendsFragment : Fragment() {

    private lateinit var searchBar: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var searchResults: RecyclerView

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