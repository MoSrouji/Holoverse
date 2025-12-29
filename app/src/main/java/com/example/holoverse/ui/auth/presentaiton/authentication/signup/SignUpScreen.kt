package com.example.holoverse.ui.auth.presentaiton.authentication.signup


import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.auth.validation.event.ValidationResultEvent
import com.example.holoverse.utils.Response
import com.example.holoverse.ui.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.auth.widget.loading.LoadingScreen
import com.example.holoverse.ui.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.navigation.AppScreen
import com.example.holoverse.ui.auth.widget.RadioButtonMenu
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25

@Composable
fun SignUpScreen(
    navController: AppNavigator,
    navToHomeScreen: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),



) {
    val menuItems = listOf("Teacher", "Student")

    val signUpState = viewModel.signUpState.value

    val context = LocalContext.current

    var isMenuExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = context) {


        viewModel.validationEvent.collect { event ->


            when (event) {
                ValidationResultEvent.Success -> {

                    val userState = User(
                        userId = null,
                        fullName = viewModel.forms[SignUpTextFieldId.FULL_NAME]!!.text,
                        email = viewModel.forms[SignUpTextFieldId.EMAIL]!!.text,
                        accountType = viewModel.getUserType()
                    )

                    viewModel.firebaseSingUp(
                        userDto = userState,
                        password = viewModel.forms[SignUpTextFieldId.PASSWORD]!!.text
                    )
                }

            }

        }
    }


    LaunchedEffect(signUpState) {

        when (signUpState) {

            is Response.Success -> {
                if (signUpState.data) {
                    Toast.makeText(context, R.string.fill_the_form, Toast.LENGTH_LONG).show()
                    navController.navigateTo(AppScreen.SignUpTeacherProfile)

                }
            }

            is Response.Error -> {
                Toast.makeText(context, signUpState.massage, Toast.LENGTH_LONG).show()
            }

            is Response.Loading -> {
                // Toast.makeText(context, "Loading ", Toast.LENGTH_LONG).show()

            }
        }
    }



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
                .verticalScroll(rememberScrollState())
                .fillMaxSize()


        ) {

            Spacer(modifier = Modifier.height(40.dp))
            //Back Image
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                modifier = Modifier
                    .align(Alignment.Start)
                    .width(40.dp)
                    .height(30.dp)
                    .clickable {
                      //  navController.popBackStack()
                    },
                contentDescription = "back"
            )



            Spacer(modifier = Modifier.height(20.dp))

            //Create Account Text
            Text(
                text = stringResource(id = R.string.create_account),
                style = IbarraNovaBoldPlatinum25,
                color = colorResource(R.color.white)
            )

            Spacer(modifier = Modifier.height(5.dp))

            //Please Fill Text
            Text(
                text = stringResource(id = R.string.please_sign_in_to_continue),
                style = IbarraNovaBoldPlatinum18,
                color = colorResource(R.color.white)


            )

            Spacer(modifier = Modifier.height(40.dp))

            //FullName TextField
            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.FULL_NAME]!!,
                hint = R.string.full_name,
                onValueChange = {
                    viewModel.onEvent(

                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.FULL_NAME]!!.copy(
                                text = it
                            )
                        )
                    )
                },
                type = TextFieldType.Text
            )
            Spacer(modifier = Modifier.height(20.dp))

            //Email TextField
            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.EMAIL]!!,
                hint = R.string.email,
                onValueChange = {

                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(

                            viewModel.forms[SignUpTextFieldId.EMAIL]!!.copy(text = it)
                        )
                    )

                },
                type = TextFieldType.Email
            )
            Spacer(modifier = Modifier.height(20.dp))


            //Password TextField
            AuthenticationTextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                state = viewModel.forms[SignUpTextFieldId.PASSWORD]!!,
                hint = R.string.password,
                onValueChange = {


                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.PASSWORD]!!.copy(text = it)
                        )
                    )
                },
                type = TextFieldType.Password
            )
            Spacer(modifier = Modifier.height(20.dp))

            RadioButtonMenu(
                isExpanded = isMenuExpanded,
                onToggle = { isMenuExpanded = !isMenuExpanded },
                selectedItem = viewModel.selectedItem,
                onItemSelected = { item ->
                    viewModel.selectedItem = item
                    isMenuExpanded = false
                    viewModel.onEvent(
                        ValidationEvent.TextFieldValueChange(
                            viewModel.forms[SignUpTextFieldId.ACCOUNT_TYPE]!!.copy(text = item)
                        )
                    )

                },
                state = viewModel.forms[SignUpTextFieldId.ACCOUNT_TYPE]!!,
                menuItems = menuItems
            )






            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.next,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                   // navController.navigateTo(AppScreen.SignUpTeacherProfile)


                },

                )


        }


        if (signUpState == Response.Loading)
            LoadingScreen()
    }


}

