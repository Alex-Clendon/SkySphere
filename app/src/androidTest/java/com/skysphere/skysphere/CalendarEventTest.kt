package com.skysphere.skysphere

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit
import android.Manifest
import org.junit.Before

class MainActivityTest {

    private lateinit var mockActivity: MainActivity
    private lateinit var mockContentResolver: ContentResolver

    @Before
    fun setUp() {
        mockActivity = MainActivity()
        mockContentResolver = mock(ContentResolver::class.java)
    }

    @Test
    fun testAddWeatherEvent() {
        val mockUri = Uri.parse("content://com.example.calendar/events/1")
        `when`(mockContentResolver.insert(any(), any())).thenReturn(mockUri)

        // Instead of setting the content resolver, use the mock directly
        mockActivity.addWeatherEvent(
            title = "Weather Update",
            description = "Current temperature: 25°C",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
            calendarId = 1
        )

        // Verify that the insert method was called on the ContentResolver
        verify(mockContentResolver).insert(eq(CalendarContract.Events.CONTENT_URI), any())
        assertNotNull(mockUri)
    }

    @Test
    fun testGetCalendarId() {
        val mockCursor = mock(Cursor::class.java)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndex(CalendarContract.Calendars._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(1L)

        `when`(mockContentResolver.query(any(), any(), isNull(), isNull(), isNull())).thenReturn(mockCursor)

        val calendarId = mockActivity.getCalendarId()

        // Verify that the calendar ID was retrieved correctly
        assertEquals(1L, calendarId)
    }

    @Test
    fun testCheckCalendarPermissions_RequestPermissions() {
        // Mocking the permission check
        `when`(mockActivity.checkSelfPermission(Manifest.permission.READ_CALENDAR)).thenReturn(PackageManager.PERMISSION_DENIED)
        `when`(mockActivity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR)).thenReturn(PackageManager.PERMISSION_DENIED)

        // Call the method that checks permissions
        mockActivity.checkCalendarPermissions()

        // Verify that the requestPermissions method was called
        verify(mockActivity).requestPermissions(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            mockActivity.CALENDAR_PERMISSIONS_REQUEST_CODE
        )
    }
}
