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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.midtermactivity.R
import com.example.midtermactivity.TaskApp
import com.example.midtermactivity.adapter.TaskAdapter
import com.example.midtermactivity.database.TaskDao
import com.example.midtermactivity.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDao: TaskDao
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

        // 1. Get the DAO from our global Application class
        taskDao = (requireActivity().application as TaskApp).database.taskDao()

        setupRecyclerView(view)
        setupFab(view)

        // 2. Load tasks from the database when the screen opens
        loadTasksFromDatabase()
    }

    private fun loadTasksFromDatabase() {
        // lifecycleScope ensures this runs in the background and cancels if the fragment is destroyed
        viewLifecycleOwner.lifecycleScope.launch {
            val tasksFromDb = taskDao.getAllTasks()
            taskList.clear()
            taskList.addAll(tasksFromDb)
            taskAdapter.notifyDataSetChanged()
            updateEmptyState()
        }
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

                    // Save the updated status to the database
                    viewLifecycleOwner.lifecycleScope.launch {
                        taskDao.insertTask(updatedTask) // REPLACE strategy updates it
                    }
                }
            },
            onDeleteTask = { task ->
                taskList.remove(task)
                taskAdapter.notifyDataSetChanged()

                // Delete from database
                viewLifecycleOwner.lifecycleScope.launch {
                    taskDao.deleteTask(task)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter

        // Swipe to Delete (Left)
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
                    val deletedTask = taskList[position]

                    // 1. Remove from local list and update UI
                    taskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                    updateEmptyState()

                    // 2. Delete from database in the background
                    viewLifecycleOwner.lifecycleScope.launch {
                        taskDao.deleteTask(deletedTask)
                    }

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
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    etDueDate.setText("$year-${month + 1}-$day")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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
                val typeText = etType.text.toString().trim().uppercase().ifEmpty { "ASSIGNMENT" }
                val priorityText = etPriority.text.toString().trim().uppercase().ifEmpty { "MEDIUM" }

                if (titleText.isEmpty() || dueDateText.isEmpty()) {
                    Toast.makeText(requireContext(), "Title and Due Date are required!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Create the new task (ID will be auto-generated by the database)
                val newTask = Task(
                    id = 0, // 0 tells Room to auto-generate the ID
                    title = titleText,
                    description = descText.ifEmpty { "No description" },
                    type = typeText,
                    subject = subjectText.ifEmpty { "General" },
                    dueDate = dueDateText,
                    priority = priorityText,
                    isCompleted = false
                )

                // 1. Insert into database
                viewLifecycleOwner.lifecycleScope.launch {
                    val insertedId = taskDao.insertTask(newTask)
                    // Note: insertTask returns the new row ID if you change the DAO to return Long,
                    // but for now, we just refresh the list to be safe.
                    loadTasksFromDatabase()
                }

                Toast.makeText(requireContext(), "Task Saved to Database!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}