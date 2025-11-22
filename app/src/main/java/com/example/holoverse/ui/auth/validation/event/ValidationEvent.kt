package com.example.holoverse.ui.auth.validation.event

import com.example.holoverse.ui.auth.validation.state.ValidationState


sealed class ValidationEvent{

    object Submit: ValidationEvent()
    data class TextFieldValueChange(val state:ValidationState):ValidationEvent()
}
