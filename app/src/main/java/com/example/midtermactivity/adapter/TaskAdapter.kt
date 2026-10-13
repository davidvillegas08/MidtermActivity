package com.example.midtermactivity.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.midtermactivity.R
import com.example.midtermactivity.model.Task

@Suppress("SetTextI18n")
class TaskAdapter(
    private var taskList: List<Task>,
    private val onItemClick: (Task) -> Unit,
    private val onStatusChange: (Task) -> Unit,
    private val onDeleteTask: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var lastAnimatedPosition = -1

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvTaskType)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvRevert: TextView = itemView.findViewById(R.id.tvRevert)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        val context = holder.itemView.context
        val currentPosition = holder.adapterPosition

        // Entrance Animation
        if (currentPosition != RecyclerView.NO_POSITION && currentPosition > lastAnimatedPosition) {
            holder.itemView.translationY = 300f
            holder.itemView.alpha = 0f
            holder.itemView.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay((currentPosition * 80).toLong()).start()
            lastAnimatedPosition = currentPosition
        }

        // Set Text (Removed .name since they are now Strings)
        holder.tvType.text = task.type
        holder.tvPriority.text = task.priority
        holder.tvTitle.text = task.title
        holder.tvSubject.text = task.subject
        holder.tvDescription.text = task.description
        holder.tvDueDate.text = "📅 Due: ${task.dueDate}"
        holder.tvStatus.text = if (task.isCompleted) "Completed" else "Pending"

        // Colors (Updated to use String literals and added 'else' fallbacks)
        val typeColor = when (task.type.uppercase()) {
            "ASSIGNMENT" -> R.color.task_type_assignment
            "QUIZ" -> R.color.task_type_quiz
            "PROJECT" -> R.color.task_type_project
            "PERSONAL" -> R.color.task_type_personal
            else -> R.color.task_type_assignment // Default fallback
        }
        holder.tvType.setBackgroundColor(ContextCompat.getColor(context, typeColor))

        val priorityColor = when (task.priority.uppercase()) {
            "HIGH" -> R.color.priority_high
            "MEDIUM" -> R.color.priority_medium
            "LOW" -> R.color.priority_low
            else -> R.color.priority_medium // Default fallback
        }
        holder.tvPriority.setBackgroundColor(ContextCompat.getColor(context, priorityColor))

        // Status Animation & Revert Button
        if (task.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.animate().alpha(0.6f).setDuration(300).start()
            holder.tvRevert.visibility = View.VISIBLE
            holder.tvRevert.setOnClickListener { onStatusChange(task) }
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.animate().alpha(1.0f).setDuration(300).start()
            holder.tvRevert.visibility = View.GONE
        }

        // Click Listeners
        holder.itemView.setOnClickListener {
            val clickPos = holder.adapterPosition
            if (clickPos != RecyclerView.NO_POSITION) onItemClick(taskList[clickPos])
        }

        // LONG PRESS TO MARK AS COMPLETED
        holder.itemView.setOnLongClickListener {
            val clickPos = holder.adapterPosition
            if (clickPos != RecyclerView.NO_POSITION) {
                onStatusChange(taskList[clickPos])
            }
            true // Return true to indicate the long press was handled
        }
    }

    override fun getItemCount(): Int = taskList.size

    // Function to update the list when searching
    fun updateList(newList: List<Task>) {
        taskList = newList
        notifyDataSetChanged()
    }
}