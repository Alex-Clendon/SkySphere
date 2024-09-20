package com.skysphere.skysphere.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.skysphere.skysphere.R
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {

    private lateinit var signUpRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val signUpRedirect = view.findViewById<TextView>(R.id.signupRedirectText)

        signUpRedirect.setOnClickListener {
            val signUpFragment = SignupFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, signUpFragment)
                .addToBackStack(null)
                .commit()
        }


        return view
    }

}