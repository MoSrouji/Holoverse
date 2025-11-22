package com.example.holoverse.ui.auth.validation.use_case

import com.example.holoverse.R
import com.example.holoverse.ui.auth.validation.interfaces.Validate
import com.example.holoverse.ui.auth.validation.state.ValidationResultState

class ValidatePassword : Validate {
    override fun execute(text: String): ValidationResultState {


        val containLettersAndDigits = text.any { it.isDigit() } && text.any { it.isLetter() }

        return if (text.isBlank()) {

            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        } else if (text.length < 8) {
            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_password_needs_to_consist_of_at_least_8_characters
            )
        } else if (!containLettersAndDigits) {
            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_password_needs_to_contain_at_least_one_letter_and_digit
            )
        } else {

            ValidationResultState(isValid = true)
        }
    }
}