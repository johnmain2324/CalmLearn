package com.education.calmlearn.ui.editprofile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.databinding.FragmentEditProfileBinding
import com.education.calmlearn.ui.common.FormUiState
import com.education.calmlearn.ui.common.hideKeyboard
import com.education.calmlearn.ui.common.toMessageRes

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreFieldsFromViewModel()
        setupTextWatchers()
        setupFocusListeners()
        setupImeActions()
        setupGender()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { submit() }

        viewModel.loadResult.observe(viewLifecycleOwner) { result -> renderLoad(result) }
        viewModel.isFormValid.observe(viewLifecycleOwner) { updateSubmitEnabled() }
        viewModel.fieldErrors.observe(viewLifecycleOwner) { errors -> renderFieldErrors(errors) }
        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun restoreFieldsFromViewModel() {
        binding.etFullName.setText(viewModel.fullName)
        binding.tvEmail.text = viewModel.email
        setGenderSelection(viewModel.gender)
    }

    private fun setupTextWatchers() {
        binding.etFullName.addTextChangedListener(simpleTextWatcher { viewModel.onFullNameChanged(it) })
    }

    private fun setupFocusListeners() {
        binding.etFullName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.onFieldBlurred(EditProfileField.FULL_NAME)
        }
    }

    private fun setupImeActions() {
        binding.etFullName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etFullName.hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun setupGender() {
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
    }

    private fun submit() {
        binding.root.hideKeyboard()
        viewModel.submit()
    }

    private fun renderLoad(result: ProfileResult?) {
        when (result) {
            is ProfileResult.Loaded, ProfileResult.Missing -> {
                restoreFieldsFromViewModel()
                binding.tvLoadError.visibility = View.GONE
            }
            ProfileResult.NotSignedIn -> findNavController().navigate(R.id.action_global_login)
            is ProfileResult.ReadError -> {
                binding.tvLoadError.text = getString(R.string.profile_load_error)
                binding.tvLoadError.visibility = View.VISIBLE
            }
            null -> binding.tvLoadError.visibility = View.GONE
        }
        updateSubmitEnabled()
        setFormEnabled(viewModel.uiState.value !is FormUiState.Loading)
    }

    private fun renderFieldErrors(errors: EditProfileFieldErrors) {
        bindFieldError(binding.tvFullNameError, errors.fullName)
        bindFieldError(binding.tvGenderError, errors.gender)
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
        val isLoaded = viewModel.loadResult.value.let { it is ProfileResult.Loaded || it == ProfileResult.Missing }
        val isValid = viewModel.isFormValid.value == true
        binding.btnSave.isEnabled = isValid && isLoaded && !isLoading
        binding.btnSave.alpha = if (binding.btnSave.isEnabled) 1f else 0.5f
    }

    private fun render(state: FormUiState) {
        val isLoading = state is FormUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.text = if (isLoading) "" else getString(R.string.edit_profile_btn_save)
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
                findNavController().navigateUp()
            }
            else -> {
                binding.tvError.visibility = View.INVISIBLE
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etFullName.isEnabled = enabled
        binding.rgGender.isEnabled = enabled
        for (i in 0 until binding.rgGender.childCount) {
            binding.rgGender.getChildAt(i).isEnabled = enabled
        }
        binding.btnBack.isEnabled = enabled
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
