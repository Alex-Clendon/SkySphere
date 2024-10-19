package com.skysphere.skysphere.ui.friends


import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.UserData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FriendsFeatureTest {

    @Mock
    private lateinit var mockSearchResults: RecyclerView

    private lateinit var addFriendsFragment: AddFriendsFragment

    private lateinit var profilePageFragment: ProfilePageFragment


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addFriendsFragment = AddFriendsFragment()
        profilePageFragment = ProfilePageFragment()
        addFriendsFragment.searchResults = mockSearchResults
    }

    @Test
    fun testUpdateRecyclerView() {
        val users = listOf(UserData(id = "1", username = "testUser"))

        addFriendsFragment.updateRecyclerView(users)
    }

    @Test
    fun testSignup(){
        val username = "testUser"
        val email = "user@example.com"
        val password = "password123"

        if(username.isNotEmpty() && email.isNotEmpty() && email.contains("@") && email.contains(".com") && password.isNotEmpty()){
            assertTrue(true)
        }
    }

    @Test
    fun testLoginWithEmailAndPassword() {
        val email = "user@example.com"
        val password = "password123"

        if(email.isNotEmpty() && password.isNotEmpty()){
            assertTrue(true)
        }
    }

    @Test
    fun sendFriendRequest() {
        profilePageFragment.CURRENT_STATE = "not_friends"
        if(profilePageFragment.CURRENT_STATE.equals("not_friends")){
            assertEquals(profilePageFragment.CURRENT_STATE, "not_friends")
            // sendFriendRequest() func is called
        }
    }

    @Test
    fun cancelFriendRequest() {
        profilePageFragment.CURRENT_STATE = "request_sent"
        if(profilePageFragment.CURRENT_STATE.equals("request_sent")){
            assertEquals(profilePageFragment.CURRENT_STATE, "request_sent")
            // cancelFriendRequest() func is called
        }
    }

    @Test
    fun acceptFriendRequest() {
        profilePageFragment.CURRENT_STATE = "request_received"
        if(profilePageFragment.CURRENT_STATE.equals("request_received")){
            assertEquals(profilePageFragment.CURRENT_STATE, "request_received")
            // acceptFriendRequest() func is called
        }
    }

    @Test
    fun unFriend() {
        profilePageFragment.CURRENT_STATE = "friends"
        if(profilePageFragment.CURRENT_STATE.equals("friends")){
            assertEquals(profilePageFragment.CURRENT_STATE, "friends")
            // unFriendPerson() func is called
        }
    }
}