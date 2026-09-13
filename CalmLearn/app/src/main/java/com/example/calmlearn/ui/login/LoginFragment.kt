package com.example.calmlearn.ui.login

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
import com.example.calmlearn.R
import com.example.calmlearn.databinding.FragmentLoginBinding
import com.example.calmlearn.ui.common.FormUiState
import com.example.calmlearn.ui.common.toMessageRes
import com.example.calmlearn.ui.common.toggleEditTextPasswordVisibility

/**
 * Man hinh Dang nhap. Logic kiem tra du lieu va goi AuthRepository nam trong [LoginViewModel].
 *
 * DIEM CHO TICH HOP: repository hien tai ([com.example.calmlearn.data.auth.UnavailableAuthRepository])
 * luon tra ve loi "chua cau hinh dich vu xac thuc" - man hinh nay se khong bao gio dieu huong
 * that su ve Trang chu cho den khi nhom noi mot AuthRepository that.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private var isPasswordVisible = false

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
        binding.cbRememberMe.isChecked = viewModel.rememberMe

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.etEmail.addTextChangedListener(simpleTextWatcher {
            viewModel.email = it
            viewModel.onFieldChanged()
        })
        binding.etPassword.addTextChangedListener(simpleTextWatcher {
            viewModel.password = it
            viewModel.onFieldChanged()
        })

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = toggleEditTextPasswordVisibility(binding.etPassword, binding.btnTogglePassword, isPasswordVisible)
        }

        binding.cbRememberMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.rememberMe = isChecked
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_global_forgotPassword)
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_global_register)
        }

        binding.btnGoogleSignIn.setOnClickListener { viewModel.onGoogleSignInClicked() }

        binding.btnLogin.setOnClickListener { viewModel.submit() }

        viewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.btnLogin.isEnabled = isValid && viewModel.uiState.value !is FormUiState.Loading
            binding.btnLogin.alpha = if (isValid) 1f else 0.5f
        }

        viewModel.fieldError.observe(viewLifecycleOwner) { errorRes ->
            binding.tvError.text = errorRes?.let { getString(it) } ?: ""
            binding.tvError.visibility = if (errorRes != null) View.VISIBLE else View.INVISIBLE
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }

        viewModel.googleUnavailableEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                Toast.makeText(requireContext(), R.string.auth_google_unavailable, Toast.LENGTH_LONG).show()
                viewModel.onGoogleUnavailableEventConsumed()
            }
        }

        viewModel.onFieldChanged()
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.login_btn_submit)
        binding.btnLogin.isEnabled = !isLoading && viewModel.isFormValid.value == true
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
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.cbRememberMe.isEnabled = enabled
        binding.tvForgotPassword.isEnabled = enabled
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
