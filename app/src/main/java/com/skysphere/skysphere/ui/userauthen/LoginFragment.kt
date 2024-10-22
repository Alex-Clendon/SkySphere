package com.skysphere.skysphere.ui.userauthen

import android.graphics.drawable.ColorDrawable
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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R


class LoginFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var loginButton: Button
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText

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
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        // Change nav bar colour to match new theme
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_start)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gradient_start
                )
            )
        )
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.sunset)
        // Initialize UI variables
        loginButton = view.findViewById(R.id.login_button)
        emailText = view.findViewById(R.id.login_email)
        passwordText = view.findViewById(R.id.login_password)

        loginButton.setOnClickListener {
            val email = emailText.text.toString().trim()
            val password = passwordText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle redirect text click
        // Initialize text
        val signUpRedirect = view.findViewById<TextView>(R.id.signupRedirectText)
        // Set on click listener
        signUpRedirect.setOnClickListener {
            val navController = findNavController()
            navController.popBackStack()
            // Use NavController to navigate to HomeFragment
            navController.navigate(R.id.nav_signup)
            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                "Sign Up"
        }


        return view
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()

                    // Set login flag to true
                    (activity as? MainActivity)?.updateNavigationMenu(true)

                    // Change the navigation bar color
                    activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)

                    // Navigate to Home
                    findNavController().navigate(R.id.nav_home)
                    (activity as AppCompatActivity?)?.supportActionBar?.title = "Home"
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}