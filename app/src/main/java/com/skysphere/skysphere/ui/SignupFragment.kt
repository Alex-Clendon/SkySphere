package com.skysphere.skysphere.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class SignupFragment : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var signUpButton: Button
    private lateinit var signUpEmail: EditText
    private lateinit var signUpUsername: EditText
    private lateinit var signUpPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
    }

    private fun signupUser(email: String, username: String, password: String) {

        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(!dataSnapshot.exists()) {
                    val id = databaseReference.push().key
                    val userData = UserData(id, email, username, password)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(requireContext(), "Successfully Registered!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                else {
                    Toast.makeText(requireContext(), "User already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        signUpButton = view.findViewById(R.id.signup_button)
        signUpEmail = view.findViewById(R.id.signup_email)
        signUpUsername = view.findViewById(R.id.signup_username)
        signUpPassword = view.findViewById(R.id.signup_password)

        signUpButton.setOnClickListener {
            val signUpUsername = signUpUsername.text.toString()
            val signUpPassword = signUpPassword.text.toString()
            val signUpEmail = signUpEmail.text.toString()

            if (signUpUsername.isNotEmpty() && signUpPassword.isNotEmpty()) {
                signupUser(signUpEmail, signUpUsername, signUpPassword)
            }
            else {
                Toast.makeText(requireContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        // Inflate the layout for this fragment
        return view
    }
}