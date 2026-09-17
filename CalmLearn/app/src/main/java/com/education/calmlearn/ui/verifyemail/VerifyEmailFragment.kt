package com.education.calmlearn.ui.verifyemail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.databinding.FragmentVerifyEmailBinding
import com.education.calmlearn.ui.common.FormUiState
import com.education.calmlearn.ui.common.toMessageRes

/**
 * Man hinh Xac minh email / Hoan tat ho so - dung CHUNG mot phien Firebase da duoc xac thuc tu
 * Dang ky hoac Dang nhap (xem VerifyEmailViewModel). Tuy trang thai that cua tai khoan ma hien
 * form "Hoan tat ho so" (Firestore chua co ho so) hoac nut "Gui lai email xac minh" (da co ho so,
 * chi con thieu xac minh).
 */
class VerifyEmailFragment : Fragment() {

    private var _binding: FragmentVerifyEmailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VerifyEmailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etFullName.setText(viewModel.fullName)
        setGenderSelection(viewModel.gender)

        binding.etFullName.addTextChangedListener(simpleTextWatcher { viewModel.onFullNameChanged(it) })
        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onGenderChanged(
                when (checkedId) {
                    R.id.rbMale -> Gender.MALE
                    R.id.rbFemale -> Gender.FEMALE
                    R.id.rbOther -> Gender.OTHER
                    else -> null
                }
            )
        }

        binding.btnSaveProfile.setOnClickListener { viewModel.submitProfile() }
        binding.btnResend.setOnClickListener { viewModel.resend() }
        binding.btnCheckAgain.setOnClickListener { viewModel.checkAgain() }

        binding.btnBack.setOnClickListener { leaveWithoutFinishing() }
        binding.tvBackToLogin.setOnClickListener { leaveWithoutFinishing() }

        viewModel.mode.observe(viewLifecycleOwner) { mode -> renderMode(mode) }
        viewModel.isProfileFormValid.observe(viewLifecycleOwner) { valid ->
            binding.btnSaveProfile.isEnabled = valid && viewModel.uiState.value !is FormUiState.Loading
            binding.btnSaveProfile.alpha = if (valid) 1f else 0.5f
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
        viewModel.infoEvent.observe(viewLifecycleOwner) { messageRes ->
            if (messageRes != null) {
                Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
                viewModel.onInfoEventConsumed()
            }
        }

        viewModel.initialize(arguments?.getBoolean(ARG_AUTO_SEND, false) ?: false)
    }

    /** Nguoi dung chu dong roi man hinh ma chua hoan tat - huy phien dang cho roi quay lai Dang nhap. */
    private fun leaveWithoutFinishing() {
        viewModel.abandon()
        findNavController().navigateUp()
    }

    private fun renderMode(mode: VerifyEmailMode) {
        when (mode) {
            VerifyEmailMode.Loading -> {
                binding.profileFormGroup.visibility = View.GONE
                binding.verifyGroup.visibility = View.GONE
            }
            is VerifyEmailMode.ProfileForm -> {
                binding.tvTitle.text = getString(R.string.verify_profile_title)
                binding.tvSubtitle.text = getString(R.string.verify_profile_subtitle, mode.email)
                binding.profileFormGroup.visibility = View.VISIBLE
                binding.verifyGroup.visibility = View.GONE
            }
            is VerifyEmailMode.Verify -> {
                binding.tvTitle.text = getString(R.string.verify_title)
                binding.tvSubtitle.text = getString(R.string.verify_subtitle_pending, mode.email)
                binding.profileFormGroup.visibility = View.GONE
                binding.verifyGroup.visibility = View.VISIBLE
            }
        }
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.profileProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.verifyProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.text = if (isLoading) "" else getString(R.string.verify_btn_save_profile)
        binding.btnResend.text = if (isLoading) "" else getString(R.string.verify_btn_resend)
        binding.btnSaveProfile.isEnabled = !isLoading && viewModel.isProfileFormValid.value == true
        binding.btnResend.isEnabled = !isLoading
        binding.btnCheckAgain.isEnabled = !isLoading
        binding.etFullName.isEnabled = !isLoading
        binding.btnBack.isEnabled = !isLoading
        binding.tvBackToLogin.isEnabled = !isLoading

        when (state) {
            is FormUiState.Error -> {
                binding.tvError.text = getString(state.reason.toMessageRes())
                binding.tvError.visibility = View.VISIBLE
            }
            FormUiState.Success -> {
                binding.tvError.visibility = View.INVISIBLE
                viewModel.consumeTerminalState()
                findNavController().navigate(R.id.action_global_home)
            }
            else -> {
                binding.tvError.visibility = View.INVISIBLE
            }
        }
    }

    private fun setGenderSelection(gender: Gender?) {
        val id = when (gender) {
            Gender.MALE -> R.id.rbMale
            Gender.FEMALE -> R.id.rbFemale
            Gender.OTHER -> R.id.rbOther
            null -> -1
        }
        if (id == -1) binding.rgGender.clearCheck() else binding.rgGender.check(id)
    }

    private fun simpleTextWatcher(onChanged: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = onChanged(s?.toString() ?: "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_AUTO_SEND = "autoSend"
    }
}
