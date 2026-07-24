package com.example.holoverse.auth.presentation.common.validation.use_case

import com.example.holoverse.R
import com.example.holoverse.auth.presentation.common.validation.interfaces.Validate
import com.example.holoverse.auth.presentation.common.validation.state.ValidationResultState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ValidateAge : Validate {
    override fun execute(text: String): ValidationResultState {
        if (text.isBlank()) {
            return ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        }

        return try {
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val birthDate = formatter.parse(text) ?: return ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )

            if (isAtLeast18(birthDate)) {
                ValidationResultState(isValid = true)
            } else {
                ValidationResultState(
                    isValid = false,
                    errorMessageId = R.string.you_must_be_at_least_18_years_old
                )
            }
        } catch (e: Exception) {
            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        }
    }

    private fun isAtLeast18(birthDate: Date): Boolean {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { time = birthDate }

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age >= 18
    }
}
