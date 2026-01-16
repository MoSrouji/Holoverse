package com.example.holoverse.ui.commonPart.auth.validation.state

import androidx.annotation.StringRes
import com.example.holoverse.ui.commonPart.auth.util.TextFieldType
import com.example.holoverse.ui.commonPart.auth.validation.interfaces.TextFieldId

data class ValidationState(
    var text:String = "",
    val type: TextFieldType = TextFieldType.Text,
    val id: TextFieldId,
    val isRequired:Boolean = true,
    var hasError:Boolean = true,
    @StringRes val errorMessageId: Int? = null,
)
