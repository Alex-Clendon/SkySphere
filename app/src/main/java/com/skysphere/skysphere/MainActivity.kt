package com.skysphere.skysphere

import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.skysphere.skysphere.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_locations, R.id.nav_settings, R.id.nav_login, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Create an on click listener for the log out item
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    // Set login flag to false
                    updateNavigationMenu(false)
                    true // Indicate that the logout was handled
                }
                else -> {
                    // Allow default navigation behavior for other items
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after item selection
                    false // Indicate that the item click was not handled here
                }
            }
        }

        // Set login flag to false
        updateNavigationMenu(false)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Function to swap the nav menu depending on if the user is logged in or not
    public fun updateNavigationMenu(isLoggedIn: Boolean) {

        val navView: NavigationView = binding.navView
        navView.menu.clear()
        if (!isLoggedIn) {
            navView.inflateMenu(R.menu.activity_main_drawer)
        } else {
            navView.inflateMenu(R.menu.login_drawer)
        }
    }

    // Will use this later -James

    /*private fun checkLoginStatus(): Boolean {
        if (UserSession.isLoggedIn) {
            return true
        }
        return false // Change this to actual login status
    }*/

}