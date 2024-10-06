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
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
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

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var loginButton: Button
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize firebase variables, table = "users"
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        // Change nav bar colour to match new theme
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.sunset)
        // Initialize UI variables
        loginButton = view.findViewById(R.id.login_button)
        usernameText = view.findViewById(R.id.login_username)
        passwordText = view.findViewById(R.id.login_password)

        loginButton.setOnClickListener {
            // Store data from input fields
            val username = usernameText.text.toString().trim()
            val password = passwordText.text.toString()
            // Check if data is not null
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Call loginUser if data is valid
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
                .commit()
            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                "Sign Up"
        }


        return view
    }

    private fun loginUser(username: String, password: String) {
        // Order database by username and compare data
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // If data already exists in the table, and username and password matches, log the user in
                if(dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val userData = userSnapshot.getValue(UserData::class.java)

                        if (userData != null && userData.password == password) {
                            // Set login flag to true
                            (activity as? MainActivity)?.updateNavigationMenu(true)

                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()

                            // Set action bar title to Home and change the navigation bar color
                            activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)
                            (activity as? AppCompatActivity)?.supportActionBar?.let { actionBar ->
                                actionBar.title = "Home"
                            }

                            // Clear back stack to remove login page and previous fragments
                            val navController = findNavController()
                            navController.popBackStack()
                            // Use NavController to navigate to HomeFragment
                            navController.navigate(R.id.nav_home)  // Navigate to the home fragment
                            return
                        }

                    }
                }
                // If data doesn't match, pop a message
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            }
            // Handle database error
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}