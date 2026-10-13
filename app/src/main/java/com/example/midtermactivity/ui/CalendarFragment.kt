package com.example.midtermactivity.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.midtermactivity.R
import com.example.midtermactivity.TaskApp
import com.example.midtermactivity.database.TaskDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var taskDao: TaskDao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the DAO from our global Application class
        taskDao = (requireActivity().application as TaskApp).database.taskDao()

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val tvTodayTasks = view.findViewById<TextView>(R.id.tvTodayTasks)
        val tvTomorrowTasks = view.findViewById<TextView>(R.id.tvTomorrowTasks)
        val tvWeekTasks = view.findViewById<TextView>(R.id.tvWeekTasks)

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val today = Calendar.getInstance()
        val todayStr = sdf.format(today.time)

        // Calculate Tomorrow
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = sdf.format(tomorrowCal.time)

        // Calculate End of Week (Saturday)
        val endOfWeekCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) }
        val endOfWeekStr = sdf.format(endOfWeekCal.time)

        // Helper to get color based on task type (Now uses String instead of Enum)
        fun getTypeColor(type: String): String {
            return when (type.uppercase()) {
                "ASSIGNMENT" -> "#4285F4" // Blue
                "QUIZ" -> "#F4B400"      // Yellow
                "PROJECT" -> "#0F9D58"    // Green
                "PERSONAL" -> "#DB4437"   // Red
                else -> "#757575"         // Grey default
            }
        }

        // Function to build the HTML list
        fun buildTaskList(tasks: List<com.example.midtermactivity.model.Task>): String {
            if (tasks.isEmpty()) return "No tasks."
            return tasks.joinToString("<br>") { task ->
                val color = getTypeColor(task.type)
                "<font color='$color'><b>${task.title}</b></font> <font color='#9E9E9E'>(${task.subject})</font>"
            }
        }

        // Function to update UI (Now loads from database)
        fun updateUI(selectedDateStr: String) {
            tvSelectedDate.text = "Tasks for: $selectedDateStr"

            // Load tasks from database in the background
            viewLifecycleOwner.lifecycleScope.launch {
                val allTasks = taskDao.getAllTasks()

                // Filter logic
                val selectedTasks = allTasks.filter { it.dueDate == selectedDateStr }
                val todayTasks = allTasks.filter { it.dueDate == todayStr }
                val tomorrowTasks = allTasks.filter { it.dueDate == tomorrowStr }

                // This week logic (Tasks between today and Saturday, excluding today/tomorrow to avoid duplicates)
                val weekTasks = allTasks.filter { task ->
                    try {
                        val taskDate = sdf.parse(task.dueDate)
                        val todayDate = sdf.parse(todayStr)
                        val endDate = sdf.parse(endOfWeekStr)
                        taskDate != null && todayDate != null && endDate != null &&
                                !taskDate.before(todayDate) && !taskDate.after(endDate) &&
                                task.dueDate != todayStr && task.dueDate != tomorrowStr
                    } catch (e: Exception) {
                        false
                    }
                }

                // Set Text (using Html.fromHtml for colors)
                tvTodayTasks.text = Html.fromHtml(buildTaskList(todayTasks))
                tvTomorrowTasks.text = Html.fromHtml(buildTaskList(tomorrowTasks))
                tvWeekTasks.text = Html.fromHtml(buildTaskList(weekTasks))
            }
        }

        // Load initial data
        updateUI(todayStr)

        // Handle Calendar Clicks
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, month, dayOfMonth)
            val dateStr = sdf.format(selectedCal.time)
            updateUI(dateStr)
        }
    }
}