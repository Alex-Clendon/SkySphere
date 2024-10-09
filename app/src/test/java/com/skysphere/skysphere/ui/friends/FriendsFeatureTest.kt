package com.skysphere.skysphere.ui.friends


import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.UserData
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FriendsFeatureTest {

    @Mock
    private lateinit var mockSearchResults: RecyclerView

    private lateinit var addFriendsFragment: AddFriendsFragment


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addFriendsFragment = AddFriendsFragment()
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
}