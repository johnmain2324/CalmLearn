package com.example.calmlearn.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.data.auth.ProfileResult
import com.example.calmlearn.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

        loadCurrentUser()

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

    /**
     * Hien ten, email, gioi tinh that cua nguoi dang dang nhap (khong con hardcode "Alex Nguyen"/
     * email mau - XML chi con dung tools:text cho preview). XP/streak dat ve trang thai "chua co
     * thanh tich" vi buoc nay chua trien khai theo doi tien do that.
     */
    private fun loadCurrentUser() {
        binding.tvProfileName.text = getString(R.string.profile_name_loading)
        binding.tvProfileEmail.text = getString(R.string.profile_email_loading)
        binding.tvProfileGender.text = getString(R.string.profile_gender_unset)

        binding.tvStreakValue.text = getString(R.string.home_streak_value_empty)
        binding.tvXpValue.text = getString(R.string.home_xp_value_empty)

        val requestedUid = FirebaseAuth.getInstance().currentUser?.uid
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepositoryProvider.repository.currentUserProfile()) {
                is ProfileResult.Loaded -> {
                    // Doi chieu UID truoc khi cap nhat UI: neu phien da doi (vd dang xuat/dang
                    // nhap tai khoan khac ngay trong luc dang cho ket qua) thi bo qua ket qua cu.
                    if (result.profile.uid != requestedUid) return@launch
                    val profile = result.profile
                    binding.tvProfileName.text = profile.fullName.takeIf { it.isNotBlank() }
                        ?: profile.email.substringBefore('@').takeIf { it.isNotBlank() }
                        ?: getString(R.string.profile_name_fallback)
                    binding.tvProfileEmail.text = profile.email.ifBlank { getString(R.string.profile_email_loading) }
                    // gender == null nghia la CHUA GHI NHAN - khong duoc tu suy dien thanh mot lua
                    // chon Nam/Nu/Khac cu the nao (xem UserProfile.gender).
                    binding.tvProfileGender.text = profile.gender?.let { getString(genderLabelRes(it)) }
                        ?: getString(R.string.profile_gender_unset)
                }
                ProfileResult.Missing, ProfileResult.NotSignedIn -> {
                    binding.tvProfileName.text = getString(R.string.profile_name_fallback)
                    binding.tvProfileGender.text = getString(R.string.profile_gender_unset)
                }
                is ProfileResult.ReadError -> {
                    binding.tvProfileName.text = getString(R.string.profile_load_error)
                    binding.tvProfileGender.text = getString(R.string.profile_gender_unset)
                }
            }
        }
    }

    private fun genderLabelRes(gender: Gender): Int = when (gender) {
        Gender.MALE -> R.string.register_gender_male
        Gender.FEMALE -> R.string.register_gender_female
        Gender.OTHER -> R.string.register_gender_other
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
                // Dua qua AuthRepository (khong dung co isLoggedIn cuc bo) roi ve man hinh
                // Dang nhap; action_global_login xoa toan bo back stack nen Back se khong quay
                // lai duoc cac man hinh can tai khoan.
                AuthRepositoryProvider.repository.logout()
                findNavController().navigate(R.id.action_global_login)
            }
            .setNegativeButton(R.string.action_back, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
