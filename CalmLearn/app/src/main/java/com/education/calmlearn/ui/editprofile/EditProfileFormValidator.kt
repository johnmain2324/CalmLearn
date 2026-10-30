package com.education.calmlearn.ui.editprofile

import androidx.annotation.StringRes
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.util.Validators

data class EditProfileInput(
    val fullName: String,
    val gender: Gender?
)

enum class EditProfileField {
    FULL_NAME, GENDER
}

data class EditProfileFieldErrors(
    @StringRes val fullName: Int? = null,
    @StringRes val gender: Int? = null
) {
    val isValid: Boolean
        get() = fullName == null && gender == null

    fun visibleTo(touched: Set<EditProfileField>): EditProfileFieldErrors = EditProfileFieldErrors(
        fullName = fullName.takeIf { EditProfileField.FULL_NAME in touched },
        gender = gender.takeIf { EditProfileField.GENDER in touched }
    )
}

object EditProfileFormValidator {

    fun errors(input: EditProfileInput): EditProfileFieldErrors = EditProfileFieldErrors(
        fullName = if (Validators.isBlank(input.fullName)) R.string.register_error_fullname else null,
        gender = if (input.gender == null) R.string.register_error_gender else null
    )
}
