package com.example.holoverse.auth.presentation.signup

import androidx.lifecycle.ViewModel
import com.example.holoverse.auth.domain.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor() : ViewModel() {
    private val _mentorState = MutableStateFlow(User.Mentor())
    val mentorState: StateFlow<User.Mentor> = _mentorState.asStateFlow()

    private val _studentState = MutableStateFlow(User.Student())
    val studentState: StateFlow<User.Student> = _studentState.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun updateMentor(mentor: User.Mentor) {
        _mentorState.value = mentor
    }

    fun updateStudent(student: User.Student) {
        _studentState.value = student
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun reset() {
        _mentorState.value = User.Mentor()
        _studentState.value = User.Student()
        _password.value = ""
    }
}
