package uk.ac.tees.mad.servicescout.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val user = authViewModel.user
    val errorMessage = authViewModel.errorMessage

    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 24.sp,
                    color = Color(0xFF333333)
                )
                HorizontalDivider(
                    color = Color(0xFF6200EE),
                    thickness = 2.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

            }
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF6200EE),
                    unfocusedTextColor = Color(0xFF6200EE),
                    focusedIndicatorColor = Color(0xFF6200EE),
                    disabledIndicatorColor = Color(0xFF603B96)
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF6200EE),
                    unfocusedTextColor = Color(0xFF6200EE),
                    focusedIndicatorColor = Color(0xFF6200EE),
                    disabledIndicatorColor = Color(0xFF603B96)
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF6200EE),
                    unfocusedTextColor = Color(0xFF6200EE),
                    focusedIndicatorColor = Color(0xFF6200EE),
                    disabledIndicatorColor = Color(0xFF603B96)
                ),
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.registerUser(email.value, password.value, name.value)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Register", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            errorMessage?.let { error ->
                Text(text = error, color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("login_screen") {
                        popUpTo("register_screen") { inclusive = true }
                    }
                }
            ) {
                Text(text = "Click here to Login.", color = Color(0xFF6200EE))
            }
        }
    }
}