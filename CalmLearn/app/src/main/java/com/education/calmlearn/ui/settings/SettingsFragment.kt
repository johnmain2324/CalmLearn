package com.education.calmlearn.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.reminder.ReminderNotifier
import com.education.calmlearn.data.reminder.ReminderPrefs
import com.education.calmlearn.data.reminder.ReminderScheduler
import com.education.calmlearn.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: ReminderPrefs
    private lateinit var scheduler: ReminderScheduler

    private val reminderToggleListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) requestEnableReminder() else disableReminder()
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            enableReminder()
        } else {
            _binding?.let { setReminderChecked(false) }
            Toast.makeText(requireContext(), R.string.settings_notification_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = ReminderPrefs(requireContext())
        scheduler = ReminderScheduler(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        setReminderChecked(prefs.isEnabled && ReminderNotifier.hasPermission(requireContext()))
        renderTime()
        renderTimeRowState()

        binding.rowReminderTime.root.setOnClickListener { showTimePicker() }
    }

    private fun requestEnableReminder() {
        if (ReminderNotifier.hasPermission(requireContext())) {
            enableReminder()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun enableReminder() {
        prefs.isEnabled = true
        scheduler.schedule(prefs.hour, prefs.minute)
        _binding?.let {
            setReminderChecked(true)
            renderTimeRowState()
        }
    }

    private fun disableReminder() {
        prefs.isEnabled = false
        scheduler.cancel()
        renderTimeRowState()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                prefs.setTime(hour, minute)
                if (prefs.isEnabled) scheduler.schedule(hour, minute)
                renderTime()
            },
            prefs.hour,
            prefs.minute,
            true
        ).show()
    }

    private fun setReminderChecked(checked: Boolean) {
        val switch = binding.rowReminder.switchReminder
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = checked
        switch.setOnCheckedChangeListener(reminderToggleListener)
    }

    private fun renderTime() {
        binding.rowReminderTime.reminderTimeValue.text =
            getString(R.string.settings_reminder_time_value, prefs.hour, prefs.minute)
    }

    private fun renderTimeRowState() {
        val enabled = binding.rowReminder.switchReminder.isChecked
        binding.rowReminderTime.root.isEnabled = enabled
        binding.rowReminderTime.root.alpha = if (enabled) 1f else 0.5f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
