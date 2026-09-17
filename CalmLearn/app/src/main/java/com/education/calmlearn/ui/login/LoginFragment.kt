package com.education.calmlearn.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.databinding.FragmentLoginBinding
import com.education.calmlearn.ui.common.FormUiState
import com.education.calmlearn.ui.common.applyEditTextPasswordVisibility
import com.education.calmlearn.ui.common.hideKeyboard
import com.education.calmlearn.ui.common.toMessageRes
import com.education.calmlearn.ui.common.toggleEditTextPasswordVisibility
import com.education.calmlearn.ui.verifyemail.VerifyEmailFragment

/**
 * Man hinh Dang nhap. Logic kiem tra du lieu va goi AuthRepository nam trong [LoginViewModel].
 *
 * DIEM CHO TICH HOP: repository hien tai ([com.education.calmlearn.data.auth.UnavailableAuthRepository])
 * luon tra ve loi "chua cau hinh dich vu xac thuc" - man hinh nay se khong bao gio dieu huong
 * that su ve Trang chu cho den khi nhom noi mot AuthRepository that.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEmail.setText(viewModel.email)
        binding.etPassword.setText(viewModel.password)
        // etPassword dat android:saveEnabled="false" nen phai tu ap lai trang thai an/hien tu ViewModel.
        applyEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, viewModel.isPasswordVisible)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.etEmail.addTextChangedListener(simpleTextWatcher { viewModel.onEmailChanged(it) })
        binding.etPassword.addTextChangedListener(simpleTextWatcher { viewModel.onPasswordChanged(it) })

        binding.etEmail.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(LoginField.EMAIL) }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onFieldBlurred(LoginField.PASSWORD) }

        binding.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etPassword.requestFocus()
                true
            } else {
                false
            }
        }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etPassword.hideKeyboard()
                submit()
                true
            } else {
                false
            }
        }

        binding.btnTogglePassword.setOnClickListener {
            val newlyVisible = toggleEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, viewModel.isPasswordVisible)
            viewModel.onPasswordVisibilityChanged(newlyVisible)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_global_forgotPassword)
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_global_register)
        }

        binding.btnGoogleSignIn.setOnClickListener { viewModel.onGoogleSignInClicked() }

        binding.btnLogin.setOnClickListener { submit() }

        viewModel.isFormValid.observe(viewLifecycleOwner) { updateSubmitEnabled() }

        viewModel.fieldErrors.observe(viewLifecycleOwner) { errors ->
            bindFieldError(binding.tvEmailError, errors.email)
            bindFieldError(binding.tvPasswordError, errors.password)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }

        viewModel.googleUnavailableEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                Toast.makeText(requireContext(), R.string.auth_google_unavailable, Toast.LENGTH_LONG).show()
                viewModel.onGoogleUnavailableEventConsumed()
            }
        }
    }

    private fun submit() {
        binding.root.hideKeyboard()
        viewModel.submit()
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
        binding.btnLogin.isEnabled = isValid && !isLoading
        binding.btnLogin.alpha = if (isValid) 1f else 0.5f
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.login_btn_submit)
        updateSubmitEnabled()
        setFormEnabled(!isLoading)

        when (state) {
            is FormUiState.Error -> {
                binding.tvError.text = getString(state.reason.toMessageRes())
                binding.tvError.visibility = View.VISIBLE
            }
            FormUiState.Success -> {
                binding.tvError.visibility = View.INVISIBLE
                // Thuc su xay ra chi khi AuthRepository that tra ve thanh cong; gan lai Idle ngay
                // de trang thai nay khong "phat lai" gay dieu huong them lan nua.
                viewModel.consumeTerminalState()
                findNavController().navigate(R.id.action_global_home)
            }
            FormUiState.RequiresNextStep -> {
                binding.tvError.visibility = View.INVISIBLE
                viewModel.consumeTerminalState()
                // Dung mat khau nhung chua du dieu kien vao app (email chua xac minh, hoac ho so
                // chua luu xong tu lan dang ky truoc do) - dua sang man hinh Xac minh email.
                // autoSend=false: co the da tung gui xac minh truoc do, khong tu gui them lan nua.
                findNavController().navigate(
                    R.id.action_global_verifyEmail,
                    bundleOf(VerifyEmailFragment.ARG_AUTO_SEND to false)
                )
            }
            else -> {
                binding.tvError.visibility = View.INVISIBLE
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.btnTogglePassword.isEnabled = enabled
        // Khoa ca lien ket dieu huong trong luc dang gui, tranh nguoi dung roi man hinh giua chung.
        binding.btnBack.isEnabled = enabled
        binding.tvForgotPassword.isEnabled = enabled
        binding.tvGoToRegister.isEnabled = enabled
        binding.btnGoogleSignIn.isEnabled = enabled
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
