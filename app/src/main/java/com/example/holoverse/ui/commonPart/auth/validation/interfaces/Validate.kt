package com.example.holoverse.ui.commonPart.auth.validation.interfaces

import com.example.holoverse.ui.commonPart.auth.validation.state.ValidationResultState

interface Validate {

    fun execute(text: String): ValidationResultState
}

