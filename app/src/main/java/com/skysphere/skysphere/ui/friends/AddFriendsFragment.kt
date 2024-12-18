package com.skysphere.skysphere.ui.friends

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.UserData

class AddFriendsFragment : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    private lateinit var searchBar: EditText
    private lateinit var searchButton: ImageButton
    lateinit var searchResults: RecyclerView

    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize firebase variables, table = "users"
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)

        updateColours()

        searchBar = view.findViewById(R.id.searchBar)
        searchButton = view.findViewById(R.id.searchButton)

        searchResults = view.findViewById(R.id.searchResults)
        searchResults.setHasFixedSize(true)
        searchResults.layoutManager = LinearLayoutManager(requireContext())

        searchButton.setOnClickListener {
            val searchQuery = searchBar.text.toString()
            searchUser(searchQuery)
        }

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

    private fun searchUser(searchQuery: String) {
        // Query firebase database for users with matching username
        val query = usersRef.orderByChild("username").startAt(searchQuery).endAt(searchQuery + "\uf8ff")

        // Listener for response from firebase
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Create and store a list of users that match the search query
                val users = mutableListOf<UserData>()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    user?.let { users.add(it) }
                }
                // Fetched users are added to the RecyclerView
                updateRecyclerView(users)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateRecyclerView(users: List<UserData>) {
        userAdapter = UserAdapter(users){ userId ->
            // Navigate to ProfilePage when a user is clicked
            val action = AddFriendsFragmentDirections.actionAddFriendsFragmentToProfilePage(userId)
            findNavController().navigate(action)
        }
        searchResults.adapter = userAdapter
    }

    class UserAdapter(private val users: List<UserData>,
                      private val onUserClick: (String) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        // Updates TextView to display the username
        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val userName: TextView = itemView.findViewById(R.id.tvUsername)
        }

        // Inflates the layout for each user item
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
            return UserViewHolder(view)
        }

        // Binds the user data to the views at a specific position
        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            holder.userName.text = user.username
            holder.itemView.setOnClickListener {
                onUserClick(user.id ?: "")
            }
        }

        override fun getItemCount() = users.size
    }

    class AddFriendsFragmentDirections private constructor() {

        companion object {
            // Navigate to the user's ProfilePage when a user is clicked
            fun actionAddFriendsFragmentToProfilePage(userId: String): NavDirections {
                return object : NavDirections {
                    // Get the ID of the action to navigate to
                    override val actionId: Int
                        get() = R.id.action_addFriendsFragment_to_profilePage

                    // Pass the user's ID as an argument to the ProfilePage fragment
                    override val arguments: Bundle
                        get() = Bundle().apply {
                            putString("userId", userId)
                        }
                }
            }
        }
    }
}