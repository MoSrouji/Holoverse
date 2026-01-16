package com.example.holoverse.ui.commonPart.auth.validation.use_case


import android.util.Patterns
import com.example.holoverse.R
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.Validate
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationResultState

class ValidatePhone : Validate {
    override fun execute(text: String): ValidationResultState {


        return if (text.isBlank()) {

            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        } else if (!Patterns.PHONE.matcher(text).matches()) {
            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.that_is_not_a_valid_phone_number
            )
        } else if (text.length < 10) {
            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_less_then_10_digits
            )

        } else {

            ValidationResultState(isValid = true)
        }
    }
}