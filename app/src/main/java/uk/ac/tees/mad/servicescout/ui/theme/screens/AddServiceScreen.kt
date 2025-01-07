package uk.ac.tees.mad.servicescout.ui.theme.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(viewModel: ServiceViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.serviceImageUri = uri
    }
    val categories = listOf(
        "Plumbing",
        "Electrical",
        "Carpentry",
        "Tutoring",
        "House Cleaning",
        "Gardening",
        "Photography",
        "IT Support",
        "Painting",
        "Event Planning",
        "Cooking/Baking",
        "Fitness Training",
        "Pet Care",
        "Driving Instructor"
    )

    LaunchedEffect(Unit) {
        viewModel.fetchCurrentLocation(context)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            TopAppBar(
                title = {
                    Text(text = "Add Service")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack(); viewModel.clearForm() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                }
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {


                OutlinedTextField(
                    value = viewModel.serviceName,
                    onValueChange = { viewModel.serviceName = it },
                    label = { Text("Service Name") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.serviceDescription,
                    onValueChange = { viewModel.serviceDescription = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.servicePrice,
                    onValueChange = { viewModel.servicePrice = it },
                    label = { Text("Price") },
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.serviceCategory,
                        onValueChange = { /* Read-only */ },
                        readOnly = true,
                        label = { Text("Category") },
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.serviceCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(
                    onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Upload Image")
                }
                Spacer(modifier = Modifier.height(8.dp))

                viewModel.serviceImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Service Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.uploadService { navController.popBackStack() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Submit Service")
                    }
                }

                viewModel.errorMessage?.let { error ->
                    Text(text = error, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

