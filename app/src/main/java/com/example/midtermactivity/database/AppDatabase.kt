package com.example.midtermactivity.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.midtermactivity.model.Task

@Database(
    entities = [Task::class], // This links your Task model to the database
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // This allows us to access the DAO functions we made in Step 3
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_task_manager_database" // Name of the database file
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}