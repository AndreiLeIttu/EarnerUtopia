package com.aospi.earnerutopia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}