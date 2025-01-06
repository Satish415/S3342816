package uk.ac.tees.mad.servicescout.ui.theme.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.servicescout.App
import uk.ac.tees.mad.servicescout.ui.theme.screens.Service

class ServiceViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val locationClient = LocationServices.getFusedLocationProviderClient(App.context)

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchServices() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val querySnapshot = firestore.collection("services").get().await()
                _services.value = querySnapshot.documents.mapNotNull { document ->
                    Service(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        category = document.getString("category") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        location = document.getString("location") ?: ""
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    var serviceName by mutableStateOf("")
    var serviceDescription by mutableStateOf("")
    var servicePrice by mutableStateOf("")
    var serviceCategory by mutableStateOf("")
    var serviceImageUri by mutableStateOf<Uri?>(null)
    var serviceLocation by mutableStateOf<String?>(null)

    fun uploadService(onSuccess: () -> Unit) {
        if (serviceName.isBlank() || serviceDescription.isBlank() || servicePrice.isBlank() || serviceCategory.isBlank() || serviceImageUri == null || serviceLocation == null) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        val imageRef = storage.reference.child("services/${System.currentTimeMillis()}.jpg")
        serviceImageUri?.let { uri ->
            val uploadTask = imageRef.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                imageRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                val service = mapOf(
                    "name" to serviceName,
                    "description" to serviceDescription,
                    "price" to servicePrice.toDouble(),
                    "category" to serviceCategory,
                    "imageUrl" to downloadUri.toString(),
                    "location" to serviceLocation,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("services").add(service)
                    .addOnSuccessListener {
                        isLoading = false
                        errorMessage = null
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.localizedMessage
                    }
            }.addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage
            }
        }
    }

    fun fetchCurrentLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
            return
        }
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    serviceLocation = "${address.latitude}, ${address.longitude}"
                    Log.d("ADDRESS", serviceLocation.toString())

                } else {
                    errorMessage = "Unable to fetch location."
                    serviceLocation = null
                }
            } else {
                errorMessage = "Unable to fetch location."
            }
        }.addOnFailureListener {
            errorMessage = "Location error: ${it.localizedMessage}"
        }
    }
}
