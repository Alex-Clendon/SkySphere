package com.skysphere.skysphere.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData
import com.skysphere.skysphere.ui.home.HomePageFragment


class LoginFragment : Fragment() {

    private var originalNavBarColor: Int = 0

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var loginButton: Button
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        originalNavBarColor = activity?.window?.navigationBarColor ?: Color.BLACK
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.sunset)

        loginButton = view.findViewById(R.id.login_button)
        usernameText = view.findViewById(R.id.login_username)
        passwordText = view.findViewById(R.id.login_password)

        loginButton.setOnClickListener {
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            }
            else {
                Toast.makeText(requireContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle redirect text click
        // Initialize text
        val signUpRedirect = view.findViewById<TextView>(R.id.signupRedirectText)
        // Set on click listener
        signUpRedirect.setOnClickListener {
            val signUpFragment = SignupFragment()
            // Replace current fragment with sign up fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, signUpFragment)
                .addToBackStack(null)
                .commit()
            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                "Sign Up"
        }


        return view
    }

    private fun loginUser(username: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val userData = userSnapshot.getValue(UserData::class.java)

                        if (userData != null && userData.password == password) {

                            (activity as? MainActivity)?.updateNavigationMenu(true)

                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()

                            val homeFragment = HomePageFragment()
                            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                                "Home"

                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, homeFragment)
                                .addToBackStack(null)
                                .commit()

                            activity?.window?.navigationBarColor = originalNavBarColor
                            return
                        }
                    }
                }
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset the navigation bar color
        activity?.window?.navigationBarColor = originalNavBarColor
    }

}