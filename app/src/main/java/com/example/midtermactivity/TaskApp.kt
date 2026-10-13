package com.example.midtermactivity

import android.app.Application
import com.example.midtermactivity.database.AppDatabase

class TaskApp : Application() {

    // This creates a single, global instance of your database
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}