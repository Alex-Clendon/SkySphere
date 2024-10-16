package com.skysphere.skysphere.ui.chat

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData
import java.text.SimpleDateFormat

class ChatFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var msgsRef: DatabaseReference

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var msgAdapter: MessagesAdapter
    private lateinit var messagesList: MutableList<Messages>

    private lateinit var receiverUsername : TextView
    private lateinit var messages : RecyclerView
    private lateinit var sendMsg : ImageButton
    private lateinit var msgInput : EditText

    private lateinit var msgReceiverId : String
    private var currentUserId : String? = null

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")
        msgsRef = firebaseDatabase.getReference()

        msgReceiverId = arguments?.getString("userId") ?: ""
        currentUserId = firebaseAuth.currentUser?.uid
        messagesList = mutableListOf()
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

        initialiseChat()

        loadUsername(msgReceiverId)

        sendMsg.setOnClickListener {
            sendMessage()
        }

        fetchMessages()

        return view
    }

    private fun initialiseChat() {
        msgAdapter = MessagesAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(context)
        messages.setHasFixedSize(true)
        messages.layoutManager = linearLayoutManager
        messages.adapter = msgAdapter
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

    private fun sendMessage() {
        val message = msgInput.text.toString()

        if(message.isEmpty()){
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        }else{
            val message_sender_ref = "messages/$currentUserId/$msgReceiverId"
            val message_receiver_ref = "messages/$msgReceiverId/$currentUserId"

            val user_msg_key = msgsRef.child("messages").child(currentUserId.toString()).child(msgReceiverId).push()

            val msg_push_id = user_msg_key.key

            val getDate : Calendar
            getDate = Calendar.getInstance()
            val currentDate : SimpleDateFormat
            currentDate = SimpleDateFormat("dd-MMMM-yyyy")
            saveCurrentDate = currentDate.format(getDate.time)

            val getTime : Calendar
            getTime = Calendar.getInstance()
            val currentTime : SimpleDateFormat
            currentTime = SimpleDateFormat("HH:mm aa")
            saveCurrentTime = currentTime.format(getTime.time)

            val msgMap = HashMap<String, Any>()
            msgMap["message"] = message
            msgMap["time"] = saveCurrentTime
            msgMap["date"] = saveCurrentDate
            msgMap["type"] = "text"
            msgMap["from"] = currentUserId.toString()

            val msgMapDetails = HashMap<String, Any>()
            msgMapDetails.put(message_sender_ref + "/" + msg_push_id, msgMap)
            msgMapDetails.put(message_receiver_ref + "/" + msg_push_id, msgMap)

            msgsRef.updateChildren(msgMapDetails).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                    msgInput.text.clear()
                } else {
                    val errorMsg = it.exception?.message
                    Toast.makeText(context, "Error: "+errorMsg, Toast.LENGTH_SHORT).show()
                    msgInput.text.clear()
                }
            }
        }
    }

    private fun fetchMessages() {
        msgsRef.child("messages").child(currentUserId.toString()).child(msgReceiverId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        val message : Messages
                        message = snapshot.getValue(Messages::class.java)!!
                        messagesList.add(message)
                        msgAdapter.notifyDataSetChanged()
                        messages.scrollToPosition(messagesList.size - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}