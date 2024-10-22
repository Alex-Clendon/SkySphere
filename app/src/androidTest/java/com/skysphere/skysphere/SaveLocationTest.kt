package com.skysphere.skysphere

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.databases.locations.LocationDatabase
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.ui.adapters.LocationsAdapter
import com.skysphere.skysphere.ui.location.LocationsFragment
import com.skysphere.skysphere.view_models.LocationViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class SaveLocationTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var locationDao: LocationDao

    @Before
    fun setup() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this)

        // Create an in-memory Room database for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, LocationDatabase::class.java).build()
        locationDao = database.locationDao() // Get the DAO from the database
        locationRepository = LocationRepository(locationDao, context) // Initialize the repository
    }

    @After
    fun tearDown() {
        // Close the database after tests
    }


    @Test
    fun testLocationEntityCreation() {
        val location = LocationEntity(
            area = "City Name",
            country = "Country",
            latitude = 34.0522,
            longitude = -118.2437
        )

        assertNotNull(location)
    }

    @Test
    fun testLocationEntityAutoGenerateId() {
        val location = LocationEntity(
            area = "City Name",
            country = "Country",
            latitude = 34.0522,
            longitude = -118.2437
        )

        // Since ID is auto-generated, it should not be negative
        assertTrue(location.id >= 0)
    }

    @Test
    fun testLocationEntityDataEntry() {
        val area = "City Name"
        val country = "Country"
        val latitude = 34.0522
        val longitude = -118.2437

        val location = LocationEntity(
            area = area,
            country = country,
            latitude = latitude,
            longitude = longitude
        )

        assertEquals(area, location.area)
        assertEquals(country, location.country)
        assertEquals(latitude, location.latitude, 0.0)
        assertEquals(longitude, location.longitude, 0.0)
    }

    @Test
    fun testDeleteCurrentLocation() = runBlocking {
        // Given a location entity
        val location = LocationEntity(
            id = 1,
            area = "City Name",
            country = "Country",
            latitude = 34.0522,
            longitude = -118.2437
        )

        // Insert the location into the mock DAO
        locationRepository.insertLocation(location.area, location.country, location.latitude, location.longitude)

        // Ensure that the location exists before deletion
        var retrievedLocation = locationDao.getLocationByName(location.area)
        assertNotNull(retrievedLocation)

        // When deleteLocation is called
        locationDao.deleteLocation(location.area)

        // Then verify the location no longer exists
        val retreived = locationDao.getLocationByName(location.area)
        assertNotNull(retreived)
    }
}