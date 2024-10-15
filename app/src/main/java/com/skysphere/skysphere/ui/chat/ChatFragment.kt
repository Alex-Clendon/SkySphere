package com.skysphere.skysphere.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class ChatFragment : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    private lateinit var receiverUsername : TextView
    private lateinit var messages : RecyclerView
    private lateinit var sendMsg : ImageButton
    private lateinit var msgInput : EditText

    private lateinit var msgReceiverId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")

        msgReceiverId = arguments?.getString("userId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)

        receiverUsername = view.findViewById(R.id.chatUsername)
        messages = view.findViewById(R.id.messages)
        sendMsg = view.findViewById(R.id.sendButton)
        msgInput = view.findViewById(R.id.sendMsg)

        loadUsername(msgReceiverId)

        return view
    }

    private fun loadUsername(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                userData?.let {
                    receiverUsername.text = it.username
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load username", Toast.LENGTH_SHORT).show()
            }
        })
    }

}