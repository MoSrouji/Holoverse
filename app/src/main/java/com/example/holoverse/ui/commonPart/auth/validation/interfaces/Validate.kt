package com.example.holoverse.ui.commonpart.auth.validation.interfaces

import com.example.holoverse.ui.commonpart.auth.validation.state.ValidationResultState

interface Validate {

    fun execute(text: String): ValidationResultState
}

