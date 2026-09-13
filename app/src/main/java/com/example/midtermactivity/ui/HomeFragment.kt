package com.example.midtermactivity.ui

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.midtermactivity.R
import com.example.midtermactivity.adapter.TaskAdapter
import com.example.midtermactivity.model.Priority
import com.example.midtermactivity.model.Task
import com.example.midtermactivity.model.TaskRepository
import com.example.midtermactivity.model.TaskType
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearchBar: EditText
    private lateinit var fabAddTask: FloatingActionButton

    private val displayedTasks = mutableListOf<Task>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        etSearchBar = view.findViewById(R.id.etSearchBar)
        fabAddTask = view.findViewById(R.id.fabAddTask)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Adapter
        taskAdapter = TaskAdapter(
            taskList = displayedTasks,
            onItemClick = { task -> Toast.makeText(requireContext(), "Clicked: ${task.title}", Toast.LENGTH_SHORT).show() },
            onStatusChange = { task ->
                // Update the status in the main repository list
                val index = TaskRepository.tasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    TaskRepository.tasks[index] = task.copy(isCompleted = !task.isCompleted)
                    applyCurrentSearch() // Refresh UI
                }
            },
            onDeleteTask = { task ->
                TaskRepository.tasks.removeIf { it.id == task.id }
                applyCurrentSearch()
            }
        )
        recyclerView.adapter = taskAdapter

        setupSwipeToDelete()

        // Search Bar Logic
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyCurrentSearch()
            }
        })

        // FAB Click Logic
        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Load initial data
        applyCurrentSearch()
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val taskToDelete = displayedTasks[position]
                    TaskRepository.tasks.removeIf { it.id == taskToDelete.id }
                    applyCurrentSearch()
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val background = android.graphics.Paint().apply { color = Color.parseColor("#DB4437") }

                if (dX > 0) {
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), background)
                } else if (dX < 0) {
                    c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), background)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun applyCurrentSearch() {
        val query = etSearchBar.text.toString().trim().lowercase()
        displayedTasks.clear()

        if (query.isEmpty()) {
            displayedTasks.addAll(TaskRepository.tasks)
        } else {
            val filtered = TaskRepository.tasks.filter {
                it.title.lowercase().contains(query) ||
                        it.subject.lowercase().contains(query) ||
                        it.description.lowercase().contains(query)
            }
            displayedTasks.addAll(filtered)
        }

        taskAdapter.updateList(displayedTasks)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (TaskRepository.tasks.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "No tasks!"
        } else if (displayedTasks.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "No matching tasks found."
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etSubject = dialogView.findViewById<EditText>(R.id.etSubject)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
        val etType = dialogView.findViewById<EditText>(R.id.etType)
        val etPriority = dialogView.findViewById<EditText>(R.id.etPriority)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val subject = etSubject.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val dueDate = etDueDate.text.toString().trim()

                val type = try { TaskType.valueOf(etType.text.toString().uppercase().trim()) } catch (e: Exception) { TaskType.ASSIGNMENT }
                val priority = try { Priority.valueOf(etPriority.text.toString().uppercase().trim()) } catch (e: Exception) { Priority.MEDIUM }

                if (title.isNotEmpty() && dueDate.isNotEmpty()) {
                    TaskRepository.tasks.add(Task(TaskRepository.nextId++, title, description, type, subject, dueDate, priority, false))
                    applyCurrentSearch()
                    recyclerView.scrollToPosition(displayedTasks.size - 1)
                } else {
                    Toast.makeText(requireContext(), "Please fill in Title and Due Date", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFF9C27B0.toInt()) // Purple
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF5F6368.toInt()) // Dark Gray
    }
}