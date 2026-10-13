package com.example.midtermactivity.database

import androidx.room.*
import com.example.midtermactivity.model.Task

@Dao
interface TaskDao {

    // Insert a task (if it already exists, replace it)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Delete a task
    @Delete
    suspend fun deleteTask(task: Task)

    // Get all tasks from the database, ordered by ID
    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasks(): List<Task>
}