package com.example.calmlearn.ui.forgotpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.databinding.FragmentForgotPasswordBinding
import com.example.calmlearn.ui.common.FormUiState
import com.example.calmlearn.ui.common.hideKeyboard
import com.example.calmlearn.ui.common.toMessageRes

/**
 * Man hinh Quen mat khau. Logic kiem tra email va goi AuthRepository nam trong
 * [ForgotPasswordViewModel]. Chi hien thong bao "da gui" khi dich vu that tra ve thanh cong -
 * voi [com.example.calmlearn.data.auth.UnavailableAuthRepository] hien tai, dieu nay se khong
 * xay ra cho den khi nhom noi dich vu xac thuc that.
 */
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEmail.setText(viewModel.email)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvBackToLogin.setOnClickListener { findNavController().navigateUp() }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onEmailChanged(s?.toString() ?: "")
            }
        })

        binding.etEmail.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) viewModel.onEmailBlurred() }

        binding.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etEmail.hideKeyboard()
                viewModel.submit()
                true
            } else {
                false
            }
        }

        binding.btnSubmit.setOnClickListener {
            binding.root.hideKeyboard()
            viewModel.submit()
        }

        viewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.btnSubmit.isEnabled = isValid && viewModel.uiState.value !is FormUiState.Loading
            binding.btnSubmit.alpha = if (isValid) 1f else 0.5f
        }

        viewModel.fieldError.observe(viewLifecycleOwner) { errorRes ->
            if (errorRes == null) {
                binding.tvEmailError.visibility = View.GONE
            } else {
                binding.tvEmailError.text = getString(errorRes)
                binding.tvEmailError.visibility = View.VISIBLE
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.text = if (isLoading) "" else getString(R.string.forgot_password_btn_submit)
        binding.btnSubmit.isEnabled = !isLoading && viewModel.isFormValid.value == true
        binding.etEmail.isEnabled = !isLoading
        // Khoa lien ket dieu huong trong luc dang gui, tranh nguoi dung roi man hinh giua chung.
        binding.btnBack.isEnabled = !isLoading
        binding.tvBackToLogin.isEnabled = !isLoading

        when (state) {
            is FormUiState.Error -> {
                binding.tvError.text = getString(state.reason.toMessageRes())
                binding.tvError.visibility = View.VISIBLE
            }
            FormUiState.Success -> {
                // Thong bao trung tinh, khong tiet lo email co ton tai trong he thong hay khong.
                binding.tvError.visibility = View.INVISIBLE
                binding.tvSubtitle.text = getString(R.string.forgot_password_success)
                binding.etEmail.isEnabled = false
                binding.btnSubmit.isEnabled = false
                binding.btnSubmit.alpha = 0.5f
            }
            else -> {
                // Idle/Loading: an banner loi dich vu cu (vd nguoi dung vua sua lai email sau loi).
                binding.tvError.visibility = View.INVISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
