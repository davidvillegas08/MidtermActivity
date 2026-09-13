package com.example.midtermactivity.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.midtermactivity.R
import com.example.midtermactivity.model.TaskRepository

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnReset = view.findViewById<Button>(R.id.btnResetTasks)
        val switchDarkMode = view.findViewById<SwitchCompat>(R.id.switchDarkMode)

        // Get the shared preferences
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Set the switch to match the saved preference
        switchDarkMode.isChecked = isDarkMode

        // Handle Switch Toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // 1. Apply the theme
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)

            // 2. Save the preference so it remembers next time
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()
        }

        // Handle Reset Button
        btnReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear Local Data")
                .setMessage("Are you sure you want to delete all tasks? This cannot be undone.")
                .setPositiveButton("Yes, Clear") { _, _ ->
                    TaskRepository.tasks.clear()
                    TaskRepository.nextId = 1
                    Toast.makeText(requireContext(), "Local data cleared!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}