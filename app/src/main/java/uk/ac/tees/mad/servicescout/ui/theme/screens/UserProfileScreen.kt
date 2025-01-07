package uk.ac.tees.mad.servicescout.ui.theme.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.servicescout.repositories.User
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val userProfile = authViewModel.user
    val errorMessage = authViewModel.errorMessage
    var user by remember { mutableStateOf(User()) }
    var uri by remember { mutableStateOf<String?>(null) }
    var editMode by remember { mutableStateOf(false) }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
        uri = u.toString()
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
        userProfile?.let {
            user = userProfile
        }
        uri = userProfile?.profileImageUrl
    }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Column(Modifier.padding(16.dp)) {

                // Profile Image
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .clickable(enabled = editMode) { launcher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Field
                OutlinedTextField(
                    value = user.name,
                    onValueChange = { user = user.copy(name = it) },
                    label = { Text("Name") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF6200EE),
                        unfocusedTextColor = Color(0xFF6200EE),
                        focusedIndicatorColor = Color(0xFF6200EE),
                        disabledIndicatorColor = Color(0xFF603B96)
                    ),
                    enabled = editMode,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Contact Field
                OutlinedTextField(
                    value = user.contact,
                    onValueChange = { user = user.copy(contact = it) },
                    label = { Text("Contact") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF6200EE),
                        unfocusedTextColor = Color(0xFF6200EE),
                        focusedIndicatorColor = Color(0xFF6200EE),
                        disabledIndicatorColor = Color(0xFF603B96)
                    ),
                    enabled = editMode,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (editMode) {
                    // Update Profile Button
                    Row(modifier = Modifier.fillMaxWidth()) {

                        Button(
                            onClick = {
                                editMode = !editMode
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = {
                                authViewModel.updateUserProfile(user, Uri.parse(uri), onSuccess = {
                                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT)
                                        .show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Update Profile")
                        }
                    }
                } else {
                    Button(
                        onClick = { editMode = !editMode },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Edit details")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            navController.navigate("login_screen") {
                                popUpTo("home_screen") { inclusive = true }
                            }
                            authViewModel.logout()

                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Logout")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Error Message
                errorMessage?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
