package com.example.holoverse.auth.presentation.common.validation.state

import androidx.annotation.StringRes

data class ValidationResultState(

    val isValid:Boolean,
    @StringRes val errorMessageId:Int? =null,
)

