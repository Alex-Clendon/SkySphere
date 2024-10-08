package com.skysphere.skysphere.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class ProfilePage : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var userName: TextView
    private lateinit var email: TextView
    private lateinit var id: TextView

    private lateinit var addBtn: ImageButton
    private lateinit var declineBtn: ImageButton

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        // Manually retrieve the argument
        userId = arguments?.getString("userId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.person_profile, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)

        userName = view.findViewById(R.id.tvUsername)
        email = view.findViewById(R.id.tvEmail)
        id = view.findViewById(R.id.tvId)

        addBtn = view.findViewById(R.id.addBtn)
        declineBtn = view.findViewById(R.id.declineBtn)

        userId?.let { loadUserData(it) } ?: run {
            Toast.makeText(context, "User ID not provided", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadUserData(userId: String) {
        databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                userData?.let {
                    userName.text = it.username
                    email.text = it.email
                    id.text = it.id
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}