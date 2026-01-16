package com.example.holoverse.ui.teacherPart.courses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holoverse.courses.domain.Courses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCourseViewModel @Inject constructor() : ViewModel() {

    var name by mutableStateOf("")
    var category by mutableStateOf("")
    var price by mutableStateOf("")
    var duration by mutableStateOf("")
    var level by mutableStateOf("")
    var description by mutableStateOf("")
    var imageUrl by mutableStateOf("")

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: CreateCourseEvent) {
        when (event) {
            is CreateCourseEvent.EnteredName -> name = event.value
            is CreateCourseEvent.EnteredCategory -> category = event.value
            is CreateCourseEvent.EnteredPrice -> price = event.value
            is CreateCourseEvent.EnteredDuration -> duration = event.value
            is CreateCourseEvent.EnteredLevel -> level = event.value
            is CreateCourseEvent.EnteredDescription -> description = event.value
            is CreateCourseEvent.EnteredImageUrl -> imageUrl = event.value
            is CreateCourseEvent.SaveCourse -> {
                viewModelScope.launch {
                    // Here you would normally call a use case to save the course
                    // For now, let's just emit a success event
                    _eventFlow.emit(UiEvent.SaveCourse)
                }
            }
        }
    }

    sealed class UiEvent {
        object SaveCourse : UiEvent()
    }
}

sealed class CreateCourseEvent {
    data class EnteredName(val value: String) : CreateCourseEvent()
    data class EnteredCategory(val value: String) : CreateCourseEvent()
    data class EnteredPrice(val value: String) : CreateCourseEvent()
    data class EnteredDuration(val value: String) : CreateCourseEvent()
    data class EnteredLevel(val value: String) : CreateCourseEvent()
    data class EnteredDescription(val value: String) : CreateCourseEvent()
    data class EnteredImageUrl(val value: String) : CreateCourseEvent()
    object SaveCourse : CreateCourseEvent()
}
