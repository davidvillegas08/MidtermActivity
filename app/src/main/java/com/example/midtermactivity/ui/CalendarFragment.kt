package com.example.midtermactivity.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.midtermactivity.R
import com.example.midtermactivity.model.Task
import com.example.midtermactivity.model.TaskRepository
import com.example.midtermactivity.model.TaskType
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Helper to get color based on task type
        fun getTypeColor(type: TaskType): String {
            return when (type) {
                TaskType.ASSIGNMENT -> "#4285F4" // Blue
                TaskType.QUIZ -> "#F4B400"      // Yellow
                TaskType.PROJECT -> "#0F9D58"    // Green
                TaskType.PERSONAL -> "#DB4437"   // Red
            }
        }

        // Function to build the HTML list
        fun buildTaskList(tasks: List<Task>): String {
            if (tasks.isEmpty()) return "No tasks."
            return tasks.joinToString("<br>") { task ->
                val color = getTypeColor(task.type)
                "<font color='$color'><b>${task.title}</b></font> <font color='#9E9E9E'>(${task.subject})</font>"
            }
        }

        // Function to update UI
        fun updateUI(selectedDateStr: String) {
            tvSelectedDate.text = "Tasks for: $selectedDateStr"

            // Filter logic
            val selectedTasks = TaskRepository.tasks.filter { it.dueDate == selectedDateStr }
            val todayTasks = TaskRepository.tasks.filter { it.dueDate == todayStr }
            val tomorrowTasks = TaskRepository.tasks.filter { it.dueDate == tomorrowStr }

            // This week logic (Tasks between today and Saturday, excluding today/tomorrow to avoid duplicates)
            val weekTasks = TaskRepository.tasks.filter { task ->
                try {
                    val taskDate = sdf.parse(task.dueDate)
                    val todayDate = sdf.parse(todayStr)
                    val endDate = sdf.parse(endOfWeekStr)
                    taskDate != null && todayDate != null && endDate != null &&
                            !taskDate.before(todayDate) && !taskDate.after(endDate) &&
                            task.dueDate != todayStr && task.dueDate != tomorrowStr
                } catch (e: Exception) { false }
            }

            // Set Text (using Html.fromHtml for colors)
            tvTodayTasks.text = Html.fromHtml(buildTaskList(todayTasks))
            tvTomorrowTasks.text = Html.fromHtml(buildTaskList(tomorrowTasks))
            tvWeekTasks.text = Html.fromHtml(buildTaskList(weekTasks))
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