package com.example.calmlearn.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.databinding.FragmentRegisterBinding
import com.example.calmlearn.ui.common.FormUiState
import com.example.calmlearn.ui.common.toMessageRes
import com.example.calmlearn.ui.common.toggleEditTextPasswordVisibility

/**
 * Man hinh Dang ky - vi du minh hoa cho Nhom chu de 2:
 * cac View cua form (EditText, RadioGroup/RadioButton, CheckBox, Button) va cach
 * gan/xu ly cac loai su kien: Click, TextChanged, FocusChange, CheckedChanged.
 *
 * Logic kiem tra du lieu va goi AuthRepository nam trong [RegisterViewModel] de Fragment chi
 * tap trung hien thi va nhan thao tac nguoi dung.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etFullName.setText(viewModel.fullName)
        binding.etEmail.setText(viewModel.email)
        binding.etPassword.setText(viewModel.password)
        binding.etConfirmPassword.setText(viewModel.confirmPassword)
        binding.cbAgree.isChecked = viewModel.agreedToTerms
        setGenderSelection(viewModel.gender)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvGoToLogin.setOnClickListener { findNavController().navigateUp() }

        binding.etFullName.addTextChangedListener(simpleTextWatcher {
            viewModel.fullName = it
            viewModel.onFieldChanged()
        })
        binding.etEmail.addTextChangedListener(simpleTextWatcher {
            viewModel.email = it
            viewModel.onFieldChanged()
        })
        binding.etPassword.addTextChangedListener(simpleTextWatcher {
            viewModel.password = it
            viewModel.onFieldChanged()
        })
        binding.etConfirmPassword.addTextChangedListener(simpleTextWatcher {
            viewModel.confirmPassword = it
            viewModel.onFieldChanged()
        })

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = toggleEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, isPasswordVisible)
        }
        binding.btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = toggleEditTextPasswordVisibility(binding.etConfirmPassword, binding.btnToggleConfirmPassword, isConfirmPasswordVisible)
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            viewModel.gender = when (checkedId) {
                R.id.rbMale -> Gender.MALE
                R.id.rbFemale -> Gender.FEMALE
                R.id.rbOther -> Gender.OTHER
                else -> null
            }
            viewModel.onFieldChanged()
        }

        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            viewModel.agreedToTerms = isChecked
            viewModel.onFieldChanged()
        }

        binding.btnRegister.setOnClickListener { viewModel.submit() }

        viewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.btnRegister.isEnabled = isValid && viewModel.uiState.value !is FormUiState.Loading
            binding.btnRegister.alpha = if (isValid) 1f else 0.5f
        }

        viewModel.fieldError.observe(viewLifecycleOwner) { errorRes ->
            binding.tvError.text = errorRes?.let { getString(it) } ?: ""
            binding.tvError.visibility = if (errorRes != null) View.VISIBLE else View.INVISIBLE
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }

        viewModel.onFieldChanged()
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.text = if (isLoading) "" else getString(R.string.register_btn_submit)
        binding.btnRegister.isEnabled = !isLoading && viewModel.isFormValid.value == true
        setFormEnabled(!isLoading)

        when (state) {
            is FormUiState.Error -> {
                binding.tvError.text = getString(state.reason.toMessageRes())
                binding.tvError.visibility = View.VISIBLE
            }
            FormUiState.Success -> {
                // Chi xay ra khi AuthRepository that tra ve thanh cong (chua co trong project nay).
                findNavController().navigate(R.id.action_global_home)
            }
            else -> Unit
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etFullName.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.etConfirmPassword.isEnabled = enabled
        binding.rgGender.isEnabled = enabled
        for (i in 0 until binding.rgGender.childCount) {
            binding.rgGender.getChildAt(i).isEnabled = enabled
        }
        binding.cbAgree.isEnabled = enabled
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
}
