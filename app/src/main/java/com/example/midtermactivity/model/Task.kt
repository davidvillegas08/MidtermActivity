package com.example.midtermactivity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. @Entity tells Room to create a table named "tasks"
@Entity(tableName = "tasks")
data class Task(
    // 2. @PrimaryKey tells Room this is the unique ID for the row
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,

    // We use String here instead of Enums to make database saving much easier!
    val type: String,
    val subject: String,
    val dueDate: String,
    val priority: String,

    val isCompleted: Boolean
)