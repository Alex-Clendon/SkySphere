package com.skysphere.skysphere.ui.friends

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData
import java.text.SimpleDateFormat

class ProfilePageFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var friendRequestRef: DatabaseReference
    private lateinit var friendsRef: DatabaseReference

    private lateinit var userName: TextView
    private lateinit var email: TextView
    private lateinit var id: TextView

    private lateinit var addBtn: Button
    private lateinit var declineBtn: Button

    private var currentUserId: String? = null
    private var profileUserId: String? = null
    lateinit var CURRENT_STATE: String
    private lateinit var saveCurrentDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")
        currentUserId = firebaseAuth.currentUser?.uid
        friendRequestRef = firebaseDatabase.reference.child("friend_requests")
        friendsRef = firebaseDatabase.reference.child("friends")

        // Manually retrieve the argument
        profileUserId = arguments?.getString("userId")
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

        CURRENT_STATE = "not_friends"

        declineBtn.visibility = View.INVISIBLE
        declineBtn.isEnabled = false

        buttonFunctions()
        buttonMaintenance()

        return view
    }

    private fun loadUserData(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
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

    // Assigns methods for buttons depending on your CURRENT_STATE
    private fun buttonFunctions() {
        profileUserId?.let { profileUserId ->
            // Checks to see if you are on your own profile
            if (!profileUserId.equals(currentUserId)) {
                // You are not on your own profile
                addBtn.setOnClickListener {
                    addBtn.isEnabled = false
                    if(CURRENT_STATE.equals("not_friends")){
                        sendFriendRequest()
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest()
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest()
                    }
                    if(CURRENT_STATE.equals("friends")){
                        unFriendPerson()
                    }
                }
            } else { // You are on your own profile
                declineBtn.visibility = View.INVISIBLE
                addBtn.visibility = View.INVISIBLE
            }
            loadUserData(profileUserId)
        } ?: run {
            Toast.makeText(context, "User ID not provided", Toast.LENGTH_SHORT).show()
        }
    }

    // Keeps the buttons in the same state when you quit the app and come back
    private fun buttonMaintenance() {
        friendRequestRef.child(currentUserId.toString())
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(profileUserId.toString())) {
                        val request_type: String
                        request_type =
                            snapshot.child(profileUserId.toString()).child("request_type")
                                .getValue().toString()
                        if (request_type.equals("sent")) {
                            CURRENT_STATE = "request_sent"
                            addBtn.text = "Cancel Request"
                            declineBtn.visibility = View.INVISIBLE
                            declineBtn.isEnabled = false
                        } else if(request_type.equals("received")){
                            CURRENT_STATE = "request_received"
                            addBtn.text = "Accept Request"
                            declineBtn.visibility = View.VISIBLE
                            declineBtn.isEnabled = true
                            declineBtn.setOnClickListener {
                                cancelFriendRequest()
                            }
                        }
                    } else {
                        friendsRef.child(currentUserId.toString())
                            .addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.hasChild(profileUserId.toString())){
                                        CURRENT_STATE = "friends"
                                        addBtn.text = "Unfriend"
                                        declineBtn.visibility = View.INVISIBLE
                                        declineBtn.isEnabled = false
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun sendFriendRequest() {
        // Writes to the database
        friendRequestRef.child(currentUserId.toString()).child(profileUserId.toString())
            .child("request_type").setValue("sent")
            .addOnCompleteListener{
                friendRequestRef.child(profileUserId.toString()).child(currentUserId.toString())
                    .child("request_type").setValue("received")
                    .addOnCompleteListener {
                        addBtn.isEnabled = true
                        CURRENT_STATE = "request_sent"
                        addBtn.text = "Cancel Request"

                        declineBtn.visibility = View.INVISIBLE
                        declineBtn.isEnabled = false
                    }
            }
    }

    private fun cancelFriendRequest() {
        // Removes from database
        friendRequestRef.child(currentUserId.toString()).child(profileUserId.toString())
            .removeValue()
            .addOnCompleteListener{
                friendRequestRef.child(profileUserId.toString()).child(currentUserId.toString())
                    .removeValue()
                    .addOnCompleteListener {
                        addBtn.isEnabled = true
                        CURRENT_STATE = "not_friends"
                        addBtn.text = "Add Friend"

                        declineBtn.visibility = View.INVISIBLE
                        declineBtn.isEnabled = false
                    }
            }
    }

    private fun acceptFriendRequest() {
        val getDate : Calendar
        getDate = Calendar.getInstance()
        val currentDate : SimpleDateFormat
        currentDate = SimpleDateFormat("dd-MMMM-yyyy")
        saveCurrentDate = currentDate.format(getDate.time)

        // Writes to database
        friendsRef.child(currentUserId.toString()).child(profileUserId.toString())
            .child("date").setValue(saveCurrentDate)
            .addOnCompleteListener {
                friendsRef.child(profileUserId.toString()).child(currentUserId.toString())
                    .child("date").setValue(saveCurrentDate)
                    .addOnCompleteListener {
                        friendRequestRef.child(currentUserId.toString()).child(profileUserId.toString())
                            .removeValue()
                            .addOnCompleteListener{
                                friendRequestRef.child(profileUserId.toString()).child(currentUserId.toString())
                                    .removeValue()
                                    .addOnCompleteListener {
                                        addBtn.isEnabled = true
                                        CURRENT_STATE = "friends"
                                        addBtn.text = "Unfriend"

                                        declineBtn.visibility = View.INVISIBLE
                                        declineBtn.isEnabled = false
                                    }
                            }
                    }
            }
    }

    private fun unFriendPerson() {
        // Removes from database
        friendsRef.child(currentUserId.toString()).child(profileUserId.toString())
            .removeValue()
            .addOnCompleteListener{
                friendsRef.child(profileUserId.toString()).child(currentUserId.toString())
                    .removeValue()
                    .addOnCompleteListener {
                        addBtn.isEnabled = true
                        CURRENT_STATE = "not_friends"
                        addBtn.text = "Add Friend"

                        declineBtn.visibility = View.INVISIBLE
                        declineBtn.isEnabled = false
                    }
            }
    }
}