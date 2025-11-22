package com.example.holoverse.ui.auth.presentaiton.authentication.signup


import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController


import com.example.holoverse.ui.auth.util.TextFieldType
import com.example.holoverse.ui.auth.validation.event.ValidationEvent
import com.example.holoverse.ui.auth.validation.event.ValidationResultEvent
import com.example.holoverse.utils.Response
import com.example.holoverse.ui.auth.widget.button.AuthenticationButton
import com.example.holoverse.ui.auth.widget.loading.LoadingScreen
import com.example.holoverse.ui.auth.widget.textfield.AuthenticationTextField
import com.example.holoverse.R
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.ui.auth.widget.ExpandableMenu
import com.example.holoverse.ui.spatialTheme.SpatialBackground
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum18
import com.example.holoverse.ui.theme.IbarraNovaBoldPlatinum25

@Composable
fun SignUpScreen(
    navController: NavController,
    navToHomeScreen: () -> Unit

) {


    val viewModel: SignUpViewModel = hiltViewModel()

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
                        //                        when(viewModel.forms[SignUpTextFieldId.ACCOUNT_TYPE]!!.text){
//                            "student" -> UserType.Student
//                            "teacher" -> UserType.Teacher
//                            else -> UserType.Student
//                        }
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
                    Toast.makeText(context, R.string.sign_up_successfully, Toast.LENGTH_LONG).show()
                    navToHomeScreen()

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

            ExpandableMenu(
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
            )






            Spacer(modifier = Modifier.height(40.dp))

            AuthenticationButton(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                textId = R.string.sign_up,
                onClick = {
                    viewModel.onEvent(ValidationEvent.Submit)
                },

                )


        }


        if (signUpState == Response.Loading)
            LoadingScreen()
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun UserTypeMenu(
    onOptionSelected: (String) -> Unit = {}
) {
    var selectedOption by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Select Your Role",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Student Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clickable {
                    selectedOption = "student"
                    onOptionSelected("student")
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedOption == "student") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👨‍🎓 Student",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedOption == "student") {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Teacher Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clickable {
                    selectedOption = "teacher"
                    onOptionSelected("teacher")
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedOption == "teacher") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👨‍🏫 Teacher",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedOption == "teacher") {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // Selected option display
        if (selectedOption.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Selected: ${selectedOption.replaceFirstChar { it.uppercase() }}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}