package com.skysphere.skysphere.calendar

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import java.util.Calendar
import java.util.TimeZone

class CalendarManager(private val context: Context) {

    // Method to add weather data to the calendar as an event
    fun addWeatherEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        calendarId: Long
    ) {
        val existingEventId = findExistingEventId(title, startTime)
        if (existingEventId != null) {
            // If an existing event is found, delete it before adding a new one
            deleteEvent(existingEventId)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
        }

        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        if (uri != null) {
            Toast.makeText(context, "Event added to calendar", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to add event", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to delete an event in Calendar
    private fun deleteEvent(eventId: Long) {
        val uri = CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(eventId.toString()).build()
        context.contentResolver.delete(uri, null, null)
    }

    // Method to retrieve the calendar ID
    fun getCalendarId(): Long? {
        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val uri = CalendarContract.Calendars.CONTENT_URI
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val calendarIdIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                    if (calendarIdIndex != -1) {
                        return it.getLong(calendarIdIndex)
                    } else {
                        Log.e("CalendarProvider", "No calendar found or query failed")
                        null
                    }
                }
            }
            null // Return null if no calendar is found
        } catch (e: SecurityException) {
            Log.e("CalendarProvider", "Permission denied: ${e.message}")
            null // Handle the absence of permission gracefully
        }
    }

    // Method to find existing events based on the title
    private fun findExistingEventId(title: String, startTime: Long): Long? {
        val projection = arrayOf(CalendarContract.Events._ID)
        val selection = "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DTSTART} = ?"
        val selectionArgs = arrayOf(title, startTime.toString())
        val cursor = context.contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val eventIdIndex = it.getColumnIndex(CalendarContract.Events._ID)
                return if (eventIdIndex != -1) {
                    it.getLong(eventIdIndex)
                } else {
                    null
                }
            }
        }
        return null // Return null if no event is found
    }

    // Helper function to check if two timestamps are on the same day
    fun isSameDay(time1: Long, time2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
}
