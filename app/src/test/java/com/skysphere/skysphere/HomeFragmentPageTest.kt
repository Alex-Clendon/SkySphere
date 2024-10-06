package com.skysphere.skysphere

import android.os.Build
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skysphere.skysphere.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.skysphere.skysphere.ui.home.HomePageFragment
import org.junit.Assert.*
import org.mockito.Mockito.*

// Use AndroidJUnit4 test runner
@RunWith(AndroidJUnit4::class)
// Configure the test to run with a specific Android SDK version
@Config(sdk = [Build.VERSION_CODES.P])
class HomePageFragmentTest {

    // Test to verify that the last refresh time is displayed
    @Test
    fun testLastRefreshTimeDisplayed() {
        // Launch the HomePageFragment in a container
        val scenario = launchFragmentInContainer<HomePageFragment>()

        // Perform actions on the launched fragment
        scenario.onFragment { fragment ->
            // Find the TextView that should display the last refresh time
            val lastRefreshTextView = fragment.view?.findViewById<TextView>(R.id.lastRefreshTextView)

            // Assert that the TextView exists
            assertNotNull(lastRefreshTextView)

            // Assert that the TextView contains some text (is not empty)
            assertTrue(lastRefreshTextView?.text?.isNotEmpty() == true)
        }
    }

    // Test to verify that swiping to refresh triggers a data update
    @Test
    fun testSwipeToRefreshTriggersUpdate() {
        val scenario = launchFragmentInContainer<HomePageFragment>()

        scenario.onFragment { fragment ->
            val swipeRefreshLayout = fragment.view?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            assertNotNull(swipeRefreshLayout)

            // Get the initial text of the lastRefreshTextView
            val initialRefreshText = fragment.view?.findViewById<TextView>(R.id.lastRefreshTextView)?.text.toString()

            // Simulate a swipe refresh action
            swipeRefreshLayout?.isRefreshing = true
            fragment.onRefresh()

            // Wait for a short time to allow for the update to occur
            Thread.sleep(1000)

            // Get the updated text of the lastRefreshTextView
            val updatedRefreshText = fragment.view?.findViewById<TextView>(R.id.lastRefreshTextView)?.text.toString()

            // Assert that the refresh text has changed, indicating an update occurred
            assertNotEquals(initialRefreshText, updatedRefreshText)
        }
    }
}
