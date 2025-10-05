package com.aospi.earnerutopia

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aospi.earnerutopia.ui.theme.EarnerUtopiaTheme

enum class EarnerUtopia() {
    Start,
    Plan,
    Schedule
}

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Settings.canDrawOverlays(this)) {
                checkLocationPermission()
            } else {
                Log.d("MainActivity", "Overlay permission is required")
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startBubbleService()
            } else {
                Log.d("MainActivity", "Location permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startBubbleService()
        }
        */
//        val db = DatabaseHelper.openDatabase(this, "database.db")
//        val cursor = db.rawQuery("SELECT earner_id, rating FROM earners", null)
//        while (cursor.moveToNext()) {
//            val id = cursor.getString(0)
//            val rating = cursor.getDouble(1)
//            Log.d("DB_RESULT", "Earner: $id, $rating")
//            if (id == null) break
//        }
//        cursor.close()
//        db.close()

        setContent {
            EarnerUtopiaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EarnerUtopiaApp(
                        name = "Rider",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            checkLocationPermission()
        }
    }

    override fun onStart() {
        super.onStart()
        stopBubbleService()
    }

    override fun onStop() {
        super.onStop()
        if (Settings.canDrawOverlays(this)) {
            checkLocationPermission()
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun checkLocationPermission() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            android.content.pm.PackageManager.PERMISSION_GRANTED -> startBubbleService()
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startBubbleService() {
        val intent = Intent(this, BubbleService::class.java)
        //startService(intent)
    }

    private fun stopBubbleService() {
        val intent = Intent(this, BubbleService::class.java)
        stopService(intent)
    }

}