package com.skysphere.skysphere.ui.friends

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AddFriendsFragmentTest{
    private lateinit var fragment: AddFriendsFragment

    @Before
    fun setup() {
        fragment = AddFriendsFragment()
    }

    @Test
    fun testSearchUser() {
        //To test searching for a user
        val searchUsername = "Stephen"
        fragment.searchUser(searchUsername)
    }

    @Test
    fun testAddFriend() {
        //To test adding a friend
        val userId = "Stephen"
        fragment.addFriend(userId)
    }

    @Test
    fun testSearchUserResultsDisplayed() {
        //To test when usernames are searched the results are displayed correctly
        val searchResults = listOf("Stephen", "Alex", "Neil", "James", "Tui")
        fragment.displaySearchResults(searchResults)
    }

    @Test
    fun testFriendAddedSuccessfully() {
        //To test when a user is successfully added
        fragment.friendAdded("Stephen")
    }

    @Test
    fun testFriendAddError() {
        //To test if the error handling when adding a friend doesn't work
        val errorMessage = "Failed to add friend"
        fragment.onFriendAddError(errorMessage)
    }
}