package com.skysphere.skysphere.ui.chat

import android.annotation.SuppressLint
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

        // Calls sendMessage() when send button is clicked
        sendMsg.setOnClickListener {
            sendMessage()
        }

        fetchMessages()

        return view
    }

    // Initialise fields that populate the chat screen
    private fun initialiseChat() {
        msgAdapter = MessagesAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(context)
        messages.setHasFixedSize(true)
        messages.layoutManager = linearLayoutManager
        messages.adapter = msgAdapter
    }

    // Loads the username of the person you are chatting with
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
        // Get the message from the input field
        val message = msgInput.text.toString()

        // Check if user has entered a message
        if(message.isEmpty()){
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        }else{ // Else send the message
            // Gets the path of the message from the sender from firebase
            val message_sender_ref = "messages/$currentUserId/$msgReceiverId"
            // Gets the path of the message for the receiver from firebase
            val message_receiver_ref = "messages/$msgReceiverId/$currentUserId"

            // Creates a unique key for each message
            val user_msg_key = msgsRef.child("messages").child(currentUserId.toString()).child(msgReceiverId).push()

            // Gets the key of the message
            val msg_push_id = user_msg_key.key

            // Gets the date and formats it
            val getDate : Calendar
            getDate = Calendar.getInstance()
            val currentDate : SimpleDateFormat
            currentDate = SimpleDateFormat("dd-MMMM-yyyy")
            saveCurrentDate = currentDate.format(getDate.time)

            // Gets the time and formats it
            val getTime : Calendar
            getTime = Calendar.getInstance()
            val currentTime : SimpleDateFormat
            currentTime = SimpleDateFormat("HH:mm aa")
            saveCurrentTime = currentTime.format(getTime.time)

            // Hashmap that creates the values stored in the firebase database
            val msgMap = HashMap<String, Any>()
            msgMap["message"] = message
            msgMap["time"] = saveCurrentTime
            msgMap["date"] = saveCurrentDate
            msgMap["type"] = "text"
            msgMap["from"] = currentUserId.toString()

            // Inserts the database with the sender & receiver path found above,
            // along with the unique key, and the values stored in msgMap
            val msgMapDetails = HashMap<String, Any>()
            msgMapDetails.put(message_sender_ref + "/" + msg_push_id, msgMap)
            msgMapDetails.put(message_receiver_ref + "/" + msg_push_id, msgMap)

            // Updates the database with the values stored in msgMapDetails
            msgsRef.updateChildren(msgMapDetails).addOnCompleteListener {
                if (it.isSuccessful) {
                    // Let user know that message was sent successfully
                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                    // Clears the input field
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
        // Path of the messages in the firebase database
        msgsRef.child("messages").child(currentUserId.toString()).child(msgReceiverId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        // Creates a Messages object using the firebase
                        val message : Messages
                        message = snapshot.getValue(Messages::class.java)!!
                        // Adds the message to the list
                        messagesList.add(message)
                        // Notifies the adapter that a new message has been added (at the end of the list)
                        msgAdapter.notifyItemInserted(messagesList.size - 1)
                        // Scrolls to the bottom of the chat when chat is opened.
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