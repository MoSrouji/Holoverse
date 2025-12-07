package com.example.holoverse.ui.collectUserData.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.TeacherSpecializationMapper
import com.example.holoverse.auth.domain.entities.getAllCategoryNames
import com.example.holoverse.auth.domain.entities.getSpecialization
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.navigation.AppScreen
import com.example.holoverse.ui.auth.presentaiton.authentication.signup.SignUpTextFieldId
import com.example.holoverse.ui.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.auth.widget.CompactMultiSelectMenu
import com.example.holoverse.ui.auth.widget.ExpandableMenu
import com.example.holoverse.ui.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.auth.widget.loading.LoadingScreen
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25
import com.example.holoverse.utils.Response


@Composable
fun TeacherProfessionalInfoInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: TeacherProfileViewModel = hiltViewModel()
) {


    val yearsItems = listOf("0", "+1", "+4", "+8", "+10")

    val languageItems = listOf("Arabic", "English", "France", "Italy", "Spain")

    val certificateItems = listOf("Bachelors", "Masters", "PhD")


    val specializations = getAllCategoryNames()


    val signUpState = viewModel.signUpState.value

    val context = LocalContext.current

    var isYearsExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isSpecializationsExpanded by remember { mutableStateOf(false) }
    var isSubjectExpanded by remember { mutableStateOf(false) }
    var isCertificateExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        SpatialBackground()
        Column(

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState()) // Add this for scrolling

                .fillMaxSize()


        ) {

            Spacer(modifier = Modifier.height(40.dp))


            Spacer(modifier = Modifier.height(20.dp))

            //Create Account Text
            Text(
                text = stringResource(id = com.example.holoverse.R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(com.example.holoverse.R.color.white)
            )

            Spacer(modifier = Modifier.height(5.dp))

            //Please Fill Text
            Text(
                text = stringResource(id = com.example.holoverse.R.string.please_complete_you_auth_to_continue),
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(com.example.holoverse.R.color.white)


            )
            Spacer(modifier = Modifier.height(40.dp))


            //Years Of Exp
            ExpandableMenu(
                isExpanded = isYearsExpanded,
                onToggle = { isYearsExpanded = !isYearsExpanded },
                selectedItem = viewModel.selectedYears,
                onItemSelected = { item ->
                    viewModel.selectedYears = item
                    isYearsExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.YEARS_OF_EXPERIENCE]!!.copy(text = item)
                        )
                    )

                },
                state = viewModel.forms[SignUpTextFieldId.YEARS_OF_EXPERIENCE]!!,
                menuItems = yearsItems,
                showIcon = false
            )


            Spacer(modifier = Modifier.height(20.dp))


            //Language
            CompactMultiSelectMenu(
                isExpanded = isLanguageExpanded,
                onToggle = { isLanguageExpanded = !isLanguageExpanded },
                selectedItems = viewModel.selectLanguage,
                onItemSelected = { item ->
                    viewModel.updateLanguage(item)
                    isYearsExpanded = false
                },
                menuItems = languageItems,
                ifItEmptyText = "Select Your Language",
                labelText = "Language :"
            )


            Spacer(modifier = Modifier.height(20.dp))


            //Certificate
            ExpandableMenu(
                isExpanded = isCertificateExpanded,
                onToggle = { isCertificateExpanded = !isCertificateExpanded },
                selectedItem = viewModel.selectedCertificate,
                onItemSelected = { item ->
                    viewModel.selectedCertificate = item
                    isCertificateExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.CERTIFICATION]!!.copy(text = item)
                        )
                    )

                },
                state = viewModel.forms[SignUpTextFieldId.CERTIFICATION]!!,
                menuItems = certificateItems,
                showIcon = false
            )





            Spacer(modifier = Modifier.height(20.dp))


            //Specializations
            ExpandableMenu(
                isExpanded = isSpecializationsExpanded,
                onToggle = { isSpecializationsExpanded = !isSpecializationsExpanded },
                selectedItem = viewModel.selectSpecializations,
                onItemSelected = { item ->
                    viewModel.selectSpecializations = item
                    viewModel.selectSubjects = setOf<String>(" Select your Subjects")
                    isSpecializationsExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.SPECIALIZATION]!!.copy(text = item)
                        )
                    )
                    viewModel.specializations = getSpecialization(item)
                    viewModel.subject =
                        TeacherSpecializationMapper.getSpecializationsByCategory(viewModel.specializations)


                },
                state = viewModel.forms[SignUpTextFieldId.SPECIALIZATION]!!,
                menuItems = specializations,
                showIcon = false
            )
            Spacer(modifier = Modifier.height(20.dp))


            //Subjects
            CompactMultiSelectMenu(
                isExpanded = isSubjectExpanded,
                onToggle = { isSubjectExpanded = !isSubjectExpanded },
                selectedItems = viewModel.selectSubjects,
                onItemSelected = { item ->
                    viewModel.updateSubject(item)
                    //isSubjectExpanded = false
                },
                menuItems = viewModel.subject,
                ifItEmptyText = "Select Your Subjects ",
                labelText = "Subjects : "
            )


            Spacer(modifier = Modifier.height(20.dp))







            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.sign_up,
                onClick = {
                    //     viewModel.onEvent(ValidationEvent.Submit)
                    navController.navigateTo(AppScreen.SignUpTeacherProfile)

                },

                )
            Spacer(modifier = Modifier.height(40.dp))


        }


        if (signUpState == Response.Loading)
            LoadingScreen()
    }


}



