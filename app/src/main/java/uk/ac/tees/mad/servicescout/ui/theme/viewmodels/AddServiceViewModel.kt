package uk.ac.tees.mad.servicescout.ui.theme.viewmodels

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uk.ac.tees.mad.servicescout.App

class AddServiceViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val locationClient = LocationServices.getFusedLocationProviderClient(App.context)

    var serviceName by mutableStateOf("")
    var serviceDescription by mutableStateOf("")
    var servicePrice by mutableStateOf("")
    var serviceCategory by mutableStateOf("")
    var serviceImageUri by mutableStateOf<Uri?>(null)
    var serviceLocation by mutableStateOf<LatLng?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun uploadService() {
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
                    "location" to mapOf(
                        "latitude" to serviceLocation!!.latitude,
                        "longitude" to serviceLocation!!.longitude
                    ),
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("services").add(service)
                    .addOnSuccessListener {
                        isLoading = false
                        errorMessage = null
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

    fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                App.context as Activity,
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
                serviceLocation = LatLng(location.latitude, location.longitude)
            } else {
                errorMessage = "Unable to fetch location."
            }
        }.addOnFailureListener {
            errorMessage = "Location error: ${it.localizedMessage}"
        }
    }
}