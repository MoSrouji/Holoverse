package com.example.holoverse.auth.presentation.common.validation.event

import com.example.holoverse.auth.presentation.common.validation.state.ValidationState


sealed class ValidationEvent{

    object Submit: ValidationEvent()
    data class TextFieldValueChange(val state: ValidationState):ValidationEvent()
}

