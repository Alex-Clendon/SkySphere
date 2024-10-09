package com.skysphere.skysphere

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast

class SkySphereTileService : TileService() {

    override fun onClick() {
        super.onClick()
        Log.d("SkySphereTileService", "Tile clicked")

        // Create an Intent to launch the MainActivity (homepage)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(pendingIntent)
            } else {
                startActivity(intent)
                // Inform the user to collapse Quick Settings manually
                Toast.makeText(this, "Please close Quick Settings to view the app.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SkySphereTileService", "Error starting activity", e)
        }
    }
}
