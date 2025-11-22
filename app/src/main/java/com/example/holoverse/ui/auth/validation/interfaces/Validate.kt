package com.example.holoverse.ui.auth.validation.interfaces

import com.example.holoverse.ui.auth.validation.state.ValidationResultState

interface Validate {

    fun execute(text: String): ValidationResultState
}

