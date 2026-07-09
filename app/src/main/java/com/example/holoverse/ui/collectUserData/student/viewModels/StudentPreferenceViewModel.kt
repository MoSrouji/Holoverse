package com.example.holoverse.ui.collectuserdata.student.viewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.ui.commonpart.auth.presentation.base.BaseValidationViewModel
import com.example.holoverse.ui.commonpart.auth.util.TextFieldType
import com.example.holoverse.ui.commonpart.auth.validation.interfaces.TextFieldId
import com.example.holoverse.ui.commonpart.auth.validation.state.ValidationState
import com.example.holoverse.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentPreferenceViewModel @Inject constructor(
    private val authenticatingRepo: AuthRepository,
    private val preferenceManager: com.example.holoverse.utils.PreferenceManager
) : BaseValidationViewModel() {

    private val _studentScreenState = MutableStateFlow(User.Student())
    val studentScreenState: StateFlow<User.Student> = _studentScreenState.asStateFlow()

    private val _signUpState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState

    fun updateState(state: User.Student) {
        _studentScreenState.value = state.copy()
    }

    var selectedGradeLevel by mutableStateOf("Select Grade Level")
    var selectedLearningTime by mutableStateOf("Select Preferred Learning Time")
    var selectInterests by mutableStateOf(emptySet<String>())

    fun updateInterests(interest: String) {
        selectInterests = if (selectInterests.contains(interest)) {
            selectInterests - interest
        } else {
            selectInterests + interest
        }
    }

    private var gradeLevelValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentPreferenceTextField.GRADE_LEVEL)
    private var universityValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentPreferenceTextField.UNIVERSITY_NAME)
    private var facultyValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentPreferenceTextField.FACULTY)
    private var learningTimeValidationState =
        ValidationState(type = TextFieldType.Text, id = StudentPreferenceTextField.PREFERRED_LEARNING_TIME)

    init {
        forms[StudentPreferenceTextField.GRADE_LEVEL] = gradeLevelValidationState
        forms[StudentPreferenceTextField.UNIVERSITY_NAME] = universityValidationState
        forms[StudentPreferenceTextField.FACULTY] = facultyValidationState
        forms[StudentPreferenceTextField.PREFERRED_LEARNING_TIME] = learningTimeValidationState
    }

    fun setProfileComplete(isComplete: Boolean) {
        preferenceManager.setProfileComplete(isComplete)
    }

    fun firebaseSignUp(userDto: User.Student) {
        viewModelScope.launch {
            authenticatingRepo.updateStudentProfile(student = userDto).collect {
                if (it is Response.Success && it.data) {
                    preferenceManager.setProfileComplete(true)
                }
                _signUpState.value = it
            }
        }
    }
}

enum class StudentPreferenceTextField : TextFieldId {
    GRADE_LEVEL, UNIVERSITY_NAME, FACULTY, PREFERRED_LEARNING_TIME, INTERESTS
}
