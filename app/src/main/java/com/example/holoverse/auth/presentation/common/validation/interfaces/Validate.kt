package com.example.holoverse.auth.presentation.common.validation.interfaces

import com.example.holoverse.auth.presentation.common.validation.state.ValidationResultState

interface Validate {

    fun execute(text: String): ValidationResultState
}


