package com.example.holoverse.ui.commonPart.auth.validation.use_case

import android.util.Patterns
import com.example.holoverse.R
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.Validate
import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationResultState

class ValidateAccountType : Validate {
    override fun execute(text: String): ValidationResultState {


        return if (text.isBlank()) {

            ValidationResultState(
                isValid = false,
                errorMessageId = R.string.the_field_can_not_be_blank
            )
        }  else {

            ValidationResultState(isValid = true)
        }
    }
}