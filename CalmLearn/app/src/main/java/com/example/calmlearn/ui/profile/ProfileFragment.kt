package com.example.calmlearn.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val languages = arrayOf("Tiếng Việt", "English")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rowAccount.root.setOnClickListener {
            Toast.makeText(requireContext(), R.string.profile_account, Toast.LENGTH_SHORT).show()
        }

        binding.rowDarkMode.switchDarkMode.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        binding.rowDarkMode.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.rowLanguage.languageValue.setOnClickListener { showLanguagePicker() }
        binding.rowLanguage.root.setOnClickListener { showLanguagePicker() }

        binding.rowAbout.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_onboarding)
        }

        binding.rowLogout.setOnClickListener { confirmLogout() }
    }

    private fun showLanguagePicker() {
        val current = languages.indexOf(binding.rowLanguage.languageValue.text.toString())
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_language)
            .setSingleChoiceItems(languages, if (current >= 0) current else 0) { dialog, which ->
                binding.rowLanguage.languageValue.text = languages[which]
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_back, null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_logout)
            .setMessage(R.string.profile_logout_confirm)
            .setPositiveButton(R.string.profile_logout) { _, _ ->
                findNavController().navigate(R.id.action_global_onboarding)
            }
            .setNegativeButton(R.string.action_back, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
