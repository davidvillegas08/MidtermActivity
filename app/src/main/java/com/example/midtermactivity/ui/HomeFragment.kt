package com.example.midtermactivity.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.midtermactivity.R
import com.example.midtermactivity.adapter.TaskAdapter
import com.example.midtermactivity.model.Priority
import com.example.midtermactivity.model.Task
import com.example.midtermactivity.model.TaskType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var tvEmptyState: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        setupRecyclerView(view)
        setupFab(view)

        // Check empty state on initial load
        updateEmptyState()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks)

        taskAdapter = TaskAdapter(
            taskList = taskList,
            onItemClick = { task ->
                Toast.makeText(requireContext(), "Clicked: ${task.title}", Toast.LENGTH_SHORT).show()
            },
            onStatusChange = { task ->
                val index = taskList.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    val updatedTask = task.copy(isCompleted = !task.isCompleted)
                    taskList[index] = updatedTask
                    taskAdapter.notifyItemChanged(index)
                }
            },
            onDeleteTask = { task ->
                taskList.remove(task)
                taskAdapter.notifyDataSetChanged()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    taskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                    updateEmptyState() // ✅ Update UI after deleting
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupFab(view: View) {
        val fabAddTask = view.findViewById<FloatingActionButton>(R.id.fabAddTask)

        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    // ✅ MAGIC FUNCTION: Toggles between the List and the Empty Text
    private fun updateEmptyState() {
        if (taskList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etSubject = dialogView.findViewById<EditText>(R.id.etSubject)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
        val etType = dialogView.findViewById<EditText>(R.id.etType)
        val etPriority = dialogView.findViewById<EditText>(R.id.etPriority)

        etDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    etDueDate.setText(formattedDate)
                },
                year, month, day
            )
            datePicker.show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val titleText = etTitle.text.toString().trim()
                val subjectText = etSubject.text.toString().trim()
                val descText = etDescription.text.toString().trim()
                val dueDateText = etDueDate.text.toString().trim()

                val typeText = etType.text.toString().trim().uppercase()
                val taskType = try {
                    TaskType.valueOf(typeText)
                } catch (e: Exception) {
                    TaskType.ASSIGNMENT
                }

                val priorityText = etPriority.text.toString().trim().uppercase()
                val priority = try {
                    Priority.valueOf(priorityText)
                } catch (e: Exception) {
                    Priority.MEDIUM
                }

                if (titleText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in the Task Title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (dueDateText.isEmpty() || dueDateText == "Due Date *") {
                    Toast.makeText(requireContext(), "Please tap to select a Due Date", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newId = if (taskList.isEmpty()) 1 else taskList.maxOf { it.id } + 1

                val newTask = Task(
                    id = newId,
                    title = titleText,
                    description = if (descText.isEmpty()) "No description" else descText,
                    type = taskType,
                    subject = if (subjectText.isEmpty()) "General" else subjectText,
                    dueDate = dueDateText,
                    priority = priority,
                    isCompleted = false
                )

                taskList.add(newTask)
                taskAdapter.notifyItemInserted(taskList.size - 1)
                updateEmptyState() // ✅ Update UI after adding

                Toast.makeText(requireContext(), "Task Saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}