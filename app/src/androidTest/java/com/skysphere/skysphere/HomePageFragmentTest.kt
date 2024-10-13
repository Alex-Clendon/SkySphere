package com.skysphere.skysphere

import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skysphere.skysphere.R
import com.skysphere.skysphere.ui.home.HomePageFragment
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class HomePageFragmentTest {

    // Set up the fragment before each test
    @Before
    fun setup() {
        launchFragmentInContainer<HomePageFragment>()
        Thread.sleep(2000)
    }

    // Test that swipe-to-refresh triggers a refresh
    @Test
    fun testSwipeToRefreshTriggersRefresh() {
        // Perform swipe down action on the SwipeRefreshLayout
        onView(withId(R.id.swipeRefreshLayout)).perform(swipeDown())

        Thread.sleep(1000)

        // Check if the SwipeRefreshLayout is no longer refreshing
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
    }

    // Test that weather text changes after refresh
    @Test
    fun testWeatherTextChangesAfterRefresh() {
        // Get initial weather text
        val initialWeatherText = getWeatherText()

        // Perform swipe down action on the SwipeRefreshLayout
        onView(withId(R.id.swipeRefreshLayout)).perform(swipeDown())

        Thread.sleep(1000)

        // Check if the weather text has changed
        onView(withId(R.id.tvWeatherState)).check(matches(not(withText(initialWeatherText))))
    }

    // Test that last refresh time changes after refresh
    @Test
    fun testLastRefreshTimeChangesAfterRefresh() {
        // Get initial last refresh time
        val initialRefreshTime = getLastRefreshTime()

        // Perform swipe down action on the SwipeRefreshLayout
        onView(withId(R.id.swipeRefreshLayout)).perform(swipeDown())

        Thread.sleep(1000)

        // Check if the last refresh time has changed
        onView(withId(R.id.lastRefreshTextView)).check(matches(not(withText(initialRefreshTime))))
    }


    // Helper function to get the current weather text
    private fun getWeatherText(): String {
        var weatherText = ""
        onView(withId(R.id.tvWeatherState)).check { view, _ ->
            weatherText = (view as TextView).text.toString()
        }
        return weatherText
    }

    // Helper function to get the current last refresh time
    private fun getLastRefreshTime(): String {
        var refreshTime = ""
        onView(withId(R.id.lastRefreshTextView)).check { view, _ ->
            refreshTime = (view as TextView).text.toString()
        }
        return refreshTime
    }

    // Custom matcher for checking if SwipeRefreshLayout is not refreshing
    private fun isNotRefreshing(): Matcher<View> {
        return object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("is not refreshing")
            }
            override fun matchesSafely(item: SwipeRefreshLayout): Boolean {
                return !item.isRefreshing
            }
        }
    }
}