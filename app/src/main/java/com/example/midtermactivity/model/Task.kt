package com.example.midtermactivity.model

enum class TaskType {
    ASSIGNMENT,
    QUIZ,
    PROJECT,
    PERSONAL
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val type: TaskType,
    val subject: String,
    val dueDate: String,
    val priority: Priority,
    val isCompleted: Boolean
)