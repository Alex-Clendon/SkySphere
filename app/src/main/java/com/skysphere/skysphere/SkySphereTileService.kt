package com.skysphere.skysphere

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

class SkySphereTileService : TileService() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onClick() {
        super.onClick()
        Log.d("SkySphereTileService", "Tile clicked")

        // Create an Intent to launch the MainActivity (homepage)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Launch the activity based on the Android version
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Create a PendingIntent for Android 12 and above
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent) // Directly call this method
            } else {
                startActivity(intent) // Directly start the activity for lower versions
            }
        } catch (e: Exception) {
            Log.e("SkySphereTileService", "Error starting activity", e)
        }
    }
}