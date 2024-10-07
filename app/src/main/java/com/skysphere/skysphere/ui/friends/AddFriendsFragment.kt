package com.skysphere.skysphere.ui.friends

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
    lateinit var databaseReference: DatabaseReference
    private lateinit var searchBar: EditText
    private lateinit var searchButton: ImageButton
    lateinit var searchResults: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize firebase variables, table = "users"
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)

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

    fun searchUser(searchQuery: String) {
        val query = databaseReference.orderByChild("username").startAt(searchQuery).endAt(searchQuery + "\uf8ff")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = mutableListOf<UserData>()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    user?.let { users.add(it) }
                }
                updateRecyclerView(users)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateRecyclerView(users: List<UserData>) {
        userAdapter = UserAdapter(users)
        searchResults.adapter = userAdapter
    }

    class UserAdapter(private val users: List<UserData>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val userName: TextView = itemView.findViewById(R.id.tvUsername)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            holder.userName.text = user.username
        }

        override fun getItemCount() = users.size
    }
}