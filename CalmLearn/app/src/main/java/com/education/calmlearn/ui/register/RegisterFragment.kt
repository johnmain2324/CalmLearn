package com.education.calmlearn.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.databinding.FragmentRegisterBinding
import com.education.calmlearn.ui.common.FormUiState
import com.education.calmlearn.ui.common.applyEditTextPasswordVisibility
import com.education.calmlearn.ui.common.hideKeyboard
import com.education.calmlearn.ui.common.toMessageRes
import com.education.calmlearn.ui.common.toggleEditTextPasswordVisibility
import com.education.calmlearn.ui.verifyemail.VerifyEmailFragment

/**
 * Man hinh Dang ky. Logic kiem tra du lieu va goi AuthRepository nam trong [RegisterViewModel] de
 * Fragment chi tap trung hien thi va nhan thao tac nguoi dung.
 *
 * Loi nhap lieu duoc hien RIENG cho tung truong (ngay canh o nhap, chi sau khi nguoi dung da
 * tuong tac hoac roi khoi o) - khac voi loi DICH VU (banner tvError dung chung, chi xuat hien khi
 * AuthRepository that tra ve loi).
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

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

        restoreFieldsFromViewModel()
        setupTextWatchers()
        setupFocusListeners()
        setupImeActions()
        setupPasswordToggles()
        setupGenderAndTerms()
        setupNavigationLinks()

        binding.btnRegister.setOnClickListener { submit() }

        viewModel.isFormValid.observe(viewLifecycleOwner) { updateSubmitEnabled() }
        viewModel.fieldErrors.observe(viewLifecycleOwner) { errors -> renderFieldErrors(errors) }
        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun restoreFieldsFromViewModel() {
        binding.etFullName.setText(viewModel.fullName)
        binding.etEmail.setText(viewModel.email)
        binding.etPassword.setText(viewModel.password)
        binding.etConfirmPassword.setText(viewModel.confirmPassword)
        binding.cbAgree.isChecked = viewModel.agreedToTerms
        setGenderSelection(viewModel.gender)

        // Cac o mat khau dat android:saveEnabled="false" (xem fragment_register.xml) nen phai tu
        // ap lai trang thai an/hien tu ViewModel, khong the trong cay vao co che tu luu cua EditText.
        applyEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, viewModel.isPasswordVisible)
        applyEditTextPasswordVisibility(binding.etConfirmPassword, binding.btnToggleConfirmPassword, viewModel.isConfirmPasswordVisible)
    }

    private fun setupTextWatchers() {
        binding.etFullName.addTextChangedListener(simpleTextWatcher { viewModel.onFullNameChanged(it) })
        binding.etEmail.addTextChangedListener(simpleTextWatcher { viewModel.onEmailChanged(it) })
        binding.etPassword.addTextChangedListener(simpleTextWatcher { viewModel.onPasswordChanged(it) })
        binding.etConfirmPassword.addTextChangedListener(simpleTextWatcher { viewModel.onConfirmPasswordChanged(it) })
    }

    private fun setupFocusListeners() {
        binding.etFullName.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(RegisterField.FULL_NAME) }
        binding.etEmail.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(RegisterField.EMAIL) }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(RegisterField.PASSWORD) }
        binding.etConfirmPassword.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(RegisterField.CONFIRM_PASSWORD) }
    }

    /** "Next" chuyen sang o tiep theo, "Done" gui form - ca hai deu goi chung submit() nen khong
     *  the gui trung voi nut tren man hinh (submit() da tu chan trung o tang ViewModel). */
    private fun setupImeActions() {
        binding.etFullName.setOnEditorActionListener { _, actionId, _ -> onNextTo(actionId, binding.etEmail) }
        binding.etEmail.setOnEditorActionListener { _, actionId, _ -> onNextTo(actionId, binding.etPassword) }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ -> onNextTo(actionId, binding.etConfirmPassword) }
        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etConfirmPassword.hideKeyboard()
                submit()
                true
            } else {
                false
            }
        }
    }

    private fun onNextTo(actionId: Int, next: View): Boolean {
        if (actionId != EditorInfo.IME_ACTION_NEXT) return false
        next.requestFocus()
        return true
    }

    private fun setupPasswordToggles() {
        binding.btnTogglePassword.setOnClickListener {
            val newlyVisible = toggleEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, viewModel.isPasswordVisible)
            viewModel.onPasswordVisibilityChanged(newlyVisible)
        }
        binding.btnToggleConfirmPassword.setOnClickListener {
            val newlyVisible = toggleEditTextPasswordVisibility(binding.etConfirmPassword, binding.btnToggleConfirmPassword, viewModel.isConfirmPasswordVisible)
            viewModel.onConfirmPasswordVisibilityChanged(newlyVisible)
        }
    }

    private fun setupGenderAndTerms() {
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
        binding.cbAgree.setOnCheckedChangeListener { _, isChecked -> viewModel.onTermsChanged(isChecked) }
    }

    private fun setupNavigationLinks() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvGoToLogin.setOnClickListener { findNavController().navigateUp() }
    }

    private fun submit() {
        binding.root.hideKeyboard()
        viewModel.submit()
    }

    private fun renderFieldErrors(errors: RegisterFieldErrors) {
        bindFieldError(binding.tvFullNameError, errors.fullName)
        bindFieldError(binding.tvEmailError, errors.email)
        bindFieldError(binding.tvPasswordError, errors.password)
        bindFieldError(binding.tvConfirmPasswordError, errors.confirmPassword)
        bindFieldError(binding.tvGenderError, errors.gender)
        bindFieldError(binding.tvTermsError, errors.terms)
    }

    private fun bindFieldError(target: TextView, errorRes: Int?) {
        if (errorRes == null) {
            target.visibility = View.GONE
        } else {
            target.text = getString(errorRes)
            target.visibility = View.VISIBLE
        }
    }

    private fun updateSubmitEnabled() {
        val isLoading = viewModel.uiState.value is FormUiState.Loading
        val isValid = viewModel.isFormValid.value == true
        binding.btnRegister.isEnabled = isValid && !isLoading
        binding.btnRegister.alpha = if (isValid) 1f else 0.5f
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.text = if (isLoading) "" else getString(R.string.register_btn_submit)
        updateSubmitEnabled()
        setFormEnabled(!isLoading)

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
            FormUiState.RequiresNextStep -> {
                binding.tvError.visibility = View.INVISIBLE
                viewModel.consumeTerminalState()
                // Tai khoan da tao (co the da hoac chua luu xong ho so) nhung CHUA co phien hop le
                // (con thieu xac minh email) - dua sang man hinh Xac minh email, KHONG vao thang
                // Trang chu. autoSend=true vi day la lan dau, chua tung gui email xac minh nao.
                findNavController().navigate(
                    R.id.action_global_verifyEmail,
                    bundleOf(VerifyEmailFragment.ARG_AUTO_SEND to true)
                )
            }
            else -> {
                binding.tvError.visibility = View.INVISIBLE
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etFullName.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.etConfirmPassword.isEnabled = enabled
        binding.btnTogglePassword.isEnabled = enabled
        binding.btnToggleConfirmPassword.isEnabled = enabled
        binding.rgGender.isEnabled = enabled
        for (i in 0 until binding.rgGender.childCount) {
            binding.rgGender.getChildAt(i).isEnabled = enabled
        }
        binding.cbAgree.isEnabled = enabled
        // Khoa ca lien ket dieu huong trong luc dang gui, tranh nguoi dung roi man hinh giua chung.
        binding.btnBack.isEnabled = enabled
        binding.tvGoToLogin.isEnabled = enabled
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
