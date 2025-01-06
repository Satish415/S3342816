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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.servicescout.App
import uk.ac.tees.mad.servicescout.repositories.ServiceRepository
import uk.ac.tees.mad.servicescout.ui.theme.screens.Service
import java.util.Locale

class ServiceViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {
    private val locationClient = LocationServices.getFusedLocationProviderClient(App.context)

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    private val _serviceDetail = MutableStateFlow<Service>(Service())
    val serviceDetail: StateFlow<Service> = _serviceDetail

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchServices() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = serviceRepository.fetchServices()
            result.onSuccess {
                _services.value = it
            }.onFailure {
                errorMessage = it.localizedMessage
            }
            isLoading = false
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
        viewModelScope.launch {


            val url = serviceRepository.uploadImage(serviceImageUri)

            val service = Service(
                id = "",
                name = serviceName,
                description = serviceDescription,
                price = servicePrice.toDouble(),
                category = serviceCategory,
                imageUrl = url.getOrThrow(),
                location = serviceLocation ?: ""
            )
            serviceRepository.uploadService(service)
                .onSuccess {
                    isLoading = false
                    errorMessage = null
                    onSuccess()
                }
                .onFailure {
                    isLoading = false
                    errorMessage = it.localizedMessage
                }
        }
    }

    fun fetchServiceById(serviceId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            serviceRepository.getServiceById(serviceId)
                .onSuccess {

                    _serviceDetail.value = it
                }.onFailure {
                    errorMessage = it.localizedMessage
                }
            isLoading = false
        }
    }

    fun clearService() {
        _serviceDetail.value = Service()
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
                    serviceLocation =
                        getAddressFromLatLng(context, "${address.latitude}, ${address.longitude}")
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

    private fun getAddressFromLatLng(context: Context, latLng: String): String {
        return try {
            val parts = latLng.split(",").map { it.trim().toDouble() }
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(parts[0], parts[1], 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Address not available"
            } else {
                "Address not available"
            }
        } catch (e: Exception) {
            "Address not available"
        }
    }

}
