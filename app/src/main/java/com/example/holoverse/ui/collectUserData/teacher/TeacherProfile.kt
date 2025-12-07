package com.example.holoverse.ui.collectUserData.teacher


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions

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
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.event.ValidationEvent
import com.example.holoverse.utils.Response
import com.example.holoverse.ui.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.auth.widget.loading.LoadingScreen
import com.example.holoverse.ui.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.R
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.navigation.AppScreen
import com.example.holoverse.ui.auth.presentaiton.authentication.signup.SignUpTextFieldId
import com.example.holoverse.ui.auth.widget.ExpandableMenu
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25

@Composable
fun TeacherProfileInput(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: TeacherProfileViewModel = hiltViewModel()


) {
    val genderItems = listOf("Male", "Female")


    val signUpState = viewModel.signUpState

    val context = LocalContext.current

    var isMenuExpanded by remember { mutableStateOf(false) }



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



            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.Bio]!!,
                hint = R.string.bio,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.Bio]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))



            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.PHONE_NUMBER]!!,
                hint = R.string.phoneNumber,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.PHONE_NUMBER]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.PhoneNumber,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(20.dp))


            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.ADDRESS]!!,
                hint = R.string.address,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.ADDRESS]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))



            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.DATE_OF_BIRTH]!!,
                hint = R.string.dateOfBirth,
                onValueChange = {
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.DATE_OF_BIRTH]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))






            ExpandableMenu(
                isExpanded = isMenuExpanded,
                onToggle = { isMenuExpanded = !isMenuExpanded },
                selectedItem = viewModel.selectedItem,
                onItemSelected = { item ->
                    viewModel.selectedItem = item
                    isMenuExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.Gender]!!.copy(text = item)
                        )
                    )

                },
                state = viewModel.forms[SignUpTextFieldId.Gender]!!,
                menuItems = genderItems
            )



            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.sign_up,
                onClick = {
                    //     viewModel.onEvent(ValidationEvent.Submit)
                    //     navController.navigateTo(AppScreen.SignUpTeacher)
                    navController.navigateTo(AppScreen.SignUpTeacherProfessional)


                },

                )


        }


        if (signUpState == Response.Loading)
            LoadingScreen()
    }


}
