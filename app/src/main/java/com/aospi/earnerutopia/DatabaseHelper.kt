package com.aospi.earnerutopia

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileOutputStream
import java.io.IOException

object DatabaseHelper {

    fun copyDatabase(context: Context, dbName: String) {
        val dbPath = context.getDatabasePath(dbName)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            try {
                context.assets.open(dbName).use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun openDatabase(context: Context, dbName: String): SQLiteDatabase {
        copyDatabase(context, dbName)
        return SQLiteDatabase.openDatabase(
            context.getDatabasePath(dbName).path,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )
    }
}
