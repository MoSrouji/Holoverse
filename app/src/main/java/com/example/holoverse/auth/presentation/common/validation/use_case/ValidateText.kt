package com.example.holoverse.auth.presentation.common.validation.use_case

import com.example.holoverse.R
import com.example.holoverse.auth.presentation.common.validation.interfaces.Validate
import com.example.holoverse.auth.presentation.common.validation.state.ValidationResultState

class ValidateText : Validate {
    override fun execute(text: String): ValidationResultState {


        return if (text.isBlank()) {

            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        } else {

            ValidationResultState(isValid = true)
        }
    }
}
