package com.example.holoverse.auth.presentation.common.validation.state

import androidx.annotation.StringRes
import com.example.holoverse.auth.presentation.common.util.TextFieldType
import com.example.holoverse.auth.presentation.common.validation.interfaces.TextFieldId

data class ValidationState(
    var text:String = "",
    val type: TextFieldType = TextFieldType.Text,
    val id: TextFieldId,
    val isRequired:Boolean = true,
    var hasError:Boolean = true,
    @StringRes val errorMessageId: Int? = null,
)

