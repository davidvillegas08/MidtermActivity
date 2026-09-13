package com.example.midtermactivity.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.midtermactivity.R
import com.example.midtermactivity.model.TaskRepository

class DashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalTasks)
        val tvPending = view.findViewById<TextView>(R.id.tvPendingTasks)
        val tvCompleted = view.findViewById<TextView>(R.id.tvCompletedTasks)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvProgressText = view.findViewById<TextView>(R.id.tvProgressText)

        // Calculate stats from the shared repository
        val total = TaskRepository.tasks.size
        val completed = TaskRepository.tasks.count { it.isCompleted }
        val pending = total - completed
        val percentage = if (total > 0) (completed * 100 / total) else 0

        // Update UI
        tvTotal.text = total.toString()
        tvPending.text = pending.toString()
        tvCompleted.text = completed.toString()
        progressBar.progress = percentage
        tvProgressText.text = "$percentage%"
    }
}