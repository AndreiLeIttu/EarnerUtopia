package com.aospi.earnerutopia

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
import com.aospi.earnerutopia.ui.theme.EarnerUtopiaTheme

enum class EarnerUtopia() {
    Start,
    Plan,
    Schedule
}

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(this)) {
                startBubbleService()
            } else {
                Log.d("print", "Overlay permission is required",)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startBubbleService()
        }

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
    }

    private fun startBubbleService() {
        val intent = Intent(this, BubbleService::class.java)
        startService(intent)
    }

}