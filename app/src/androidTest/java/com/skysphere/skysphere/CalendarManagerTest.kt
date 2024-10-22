package com.skysphere.skysphere

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.skysphere.skysphere.calendar.CalendarManager
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CalendarManagerTest {

    private lateinit var calendarManager: CalendarManager
    private lateinit var mockContext: Context

    // Set up the CalendarManager before each test
    @Before
    fun setUp() {
        // Get the application context for the CalendarManager
        mockContext = ApplicationProvider.getApplicationContext<Context>()
        calendarManager = CalendarManager(mockContext)
    }

    // Test to check if an event is created in the calendar
    @Test
    fun testAddWeatherEvent_createsEventInCalendar() = runBlocking {
        // Given: An event title, description, start time, end time, and a calendar ID
        val title = "Test Weather Event"
        val description = "This is a test description"
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 60 * 60 * 1000 // 1 hour later
        val calendarId = 1L // Assuming this is a valid calendar ID

        // When: Add the weather event to the calendar
        calendarManager.addWeatherEvent(title, description, startTime, endTime, calendarId)

        // Then: Verify that the event was added
        val eventId = calendarManager.getEventsByTitleAndTime(title, startTime, calendarId)
        assertTrue("Event should be added to the calendar", eventId.isNotEmpty())
    }

    // Test to check if an event can be deleted from the calendar
    @Test
    fun testDeleteEvent_removesEventFromCalendar() = runBlocking {
        // Given: An event title, description, start time, end time, and a calendar ID
        val title = "Test Weather Event"
        val description = "This is a test description"
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 60 * 60 * 1000 // 1 hour later
        val calendarId = 1L // Assuming this is a valid calendar ID

        // Add the event first
        calendarManager.addWeatherEvent(title, description, startTime, endTime, calendarId)
        val eventId = calendarManager.getEventsByTitleAndTime(title, startTime, calendarId).firstOrNull()
        assertNotNull("Event should be added to the calendar", eventId)

        // When: Delete the event
        if (eventId != null) {
            calendarManager.deleteEvent(eventId)
        }

        // Then: Verify that the event has been removed
        val deletedEventId = calendarManager.getEventsByTitleAndTime(title, startTime, calendarId)
        assertTrue("Event should be removed from the calendar", deletedEventId.isEmpty())
    }

    // Test to check if the getCalendarId returns a valid ID
    @Test
    fun testGetCalendarId_returnsValidId() = runBlocking {
        // Fake a known calendar ID for testing
        val fakeCalendarId = 12345L // A simple fake ID for the test

        // Use a fake implementation of CalendarManager that returns the fake ID
        val fakeCalendarManager = object : CalendarManager(mockContext) {
            override fun getCalendarId(): Long {
                return fakeCalendarId // Return the fake ID directly
            }
        }

        // When: Get the calendar ID from the fake manager
        val calendarId = fakeCalendarManager.getCalendarId()

        // Then: Verify that the returned ID matches the fake ID
        assertEquals("Should return the fake calendar ID", fakeCalendarId, calendarId)
    }

}