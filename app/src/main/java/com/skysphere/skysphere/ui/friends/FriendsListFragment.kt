package com.skysphere.skysphere.ui.friends

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class FriendsListFragment : Fragment() {

    private lateinit var myFriends: RecyclerView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var friendsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")
        friendsRef = firebaseDatabase.reference.child("friends")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends_list, container, false)

        updateColours()

        myFriends = view.findViewById(R.id.friendsList)
        myFriends.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.setReverseLayout(true)
        layoutManager.stackFromEnd = true
        myFriends.layoutManager = layoutManager

        displayFriends()

        return view
    }

    private fun updateColours() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_start)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gradient_start
                )
            )
        )
    }

    private fun displayFriends() {
        // Get current user's ID
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        // Query firebase database for friends of current user
        val getFriends = friendsRef.child(currentUserId)

        // FirebaseRecyclerOptions for displaying friends
        val options = FirebaseRecyclerOptions.Builder<FriendsData>()
            .setQuery(getFriends, FriendsData::class.java)
            .build()

        // FirebaseRecyclerAdapter for displaying friends
        val adapter = object : FirebaseRecyclerAdapter<FriendsData, friendsViewHolder>(options) {
            // Inflates the layout for each friend item
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): friendsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.friend_item, parent, false)
                return friendsViewHolder(view)
            }

            // Binds the user data to the views at a specific position
            override fun onBindViewHolder(holder: friendsViewHolder, position: Int, model: FriendsData) {
                val friendId = getRef(position).key ?: return

                // Get friends user data
                usersRef.child(friendId).get().addOnSuccessListener { snapshot ->
                    val userData = snapshot.getValue(UserData::class.java)
                    holder.tvUsername.text = userData?.username ?: "Unknown User"
                }

                // Set date of when they became friends
                holder.tvFriendsSince.text = "Friends since: ${model.date ?: "Unknown"}"

                // Creates an on click listener for each friend item
                holder.itemView.setOnClickListener {
                    // Options for the alert dialog
                    val options = listOf(
                        "Profile",
                        "Chat"
                    )
                    val builder = AlertDialog.Builder(this@FriendsListFragment.requireContext())
                    builder.setTitle("Select an option")

                    // Handles the user's choice
                    builder.setItems(options.toTypedArray()){ _, which ->
                        when (which) {
                            0 -> {
                                // Navigate to the user's profile
                                val action = FriendsListFragmentDirections.actionFriendsListFragmentToProfilePage(friendId)
                                findNavController().navigate(action)

                            }
                            1 -> {
                                // Navigate to the chat with the user
                                val action = FriendsListFragmentDirections.actionFriendsListFragmentToChatFragment(friendId)
                                findNavController().navigate(action)
                            }
                        }
                    }
                    // Displays the dialog in the alert message
                    builder.show()
                }
            }
        }

        myFriends.adapter = adapter
        adapter.startListening()
    }

    class friendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Updates the TextView's to display username, and date of when they became friends
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvFriendsSince: TextView = itemView.findViewById(R.id.tvFriendsSince)
    }

    // Function that navigates the users to the respective pages
    class FriendsListFragmentDirections private constructor() {
        companion object {
            // Navigates to the profile page
            fun actionFriendsListFragmentToProfilePage(userId: String): NavDirections {
                return object : NavDirections {
                    override val actionId: Int
                        get() = R.id.action_FriendsListFragment_to_profilePage

                    override val arguments: Bundle
                        get() = Bundle().apply {
                            putString("userId", userId)
                        }
                }
            }

            // Navigates to the chat page
            fun actionFriendsListFragmentToChatFragment(userId: String): NavDirections {
                return object : NavDirections {
                    override val actionId: Int
                        get() = R.id.action_FriendsListFragment_to_chatFragment

                    override val arguments: Bundle
                        get() = Bundle().apply {
                            putString("userId", userId)
                        }
                }
            }
        }
    }
}