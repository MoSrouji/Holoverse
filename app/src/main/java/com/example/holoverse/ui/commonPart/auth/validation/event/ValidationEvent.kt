package com.example.holoverse.ui.commonPart.auth.validation.event

import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationState


sealed class ValidationEvent{

    object Submit: ValidationEvent()
    data class TextFieldValueChange(val state: ValidationState):ValidationEvent()
}
