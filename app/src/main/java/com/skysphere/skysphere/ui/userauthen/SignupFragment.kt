package com.skysphere.skysphere.ui.userauthen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class SignupFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var signUpButton: Button
    private lateinit var signUpEmail: EditText
    private lateinit var signUpUsername: EditText
    private lateinit var signUpPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize firebase variables, table = "users"
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        // Initialize UI variables
        signUpButton = view.findViewById(R.id.signup_button)
        signUpEmail = view.findViewById(R.id.signup_email)
        signUpUsername = view.findViewById(R.id.signup_username)
        signUpPassword = view.findViewById(R.id.signup_password)

        signUpButton.setOnClickListener {
            // Store data from input fields
            val signUpUsername = signUpUsername.text.toString().trim()
            val signUpPassword = signUpPassword.text.toString().trim()
            val signUpEmail = signUpEmail.text.toString().trim()
            // Check if data is not null
            if (signUpEmail.isNotEmpty() && signUpUsername.isNotEmpty() && signUpPassword.isNotEmpty()) {
                // Call registerUser if data is valid
                registerUser(signUpUsername, signUpEmail, signUpPassword)
            }
            else {
                Toast.makeText(requireContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle redirect text click
        // Initialize text
        val loginRedirect = view.findViewById<TextView>(R.id.loginRedirectText)
        // Set on click listener
        loginRedirect.setOnClickListener {
            val navController = findNavController()
            navController.popBackStack()
            // Use NavController to navigate to LoginFragment
            navController.navigate(R.id.nav_login)
            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                "Log In"
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun registerUser(username: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    // Save additional user info to Realtime Database
                    val userData = UserData(userId, email, username, password)
                    usersRef.child(userId).setValue(userData)

                    Toast.makeText(requireContext(), "Successfully Registered!", Toast.LENGTH_SHORT).show()

                    // Update login status
                    (activity as? MainActivity)?.updateNavigationMenu(true)

                    // Navigate to Home
                    findNavController().navigate(R.id.nav_home)
                    (activity as AppCompatActivity?)?.supportActionBar?.title = "Home"
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}