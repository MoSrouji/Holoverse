package com.example.holoverse.ui.auth.validation.state

import androidx.annotation.StringRes

data class ValidationResultState(

    val isValid:Boolean,
    @StringRes val errorMessageId:Int? =null,
)
