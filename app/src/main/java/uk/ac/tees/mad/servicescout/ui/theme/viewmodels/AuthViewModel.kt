package uk.ac.tees.mad.servicescout.ui.theme.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import uk.ac.tees.mad.servicescout.repositories.AuthRepository
import uk.ac.tees.mad.servicescout.repositories.User

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var user: User? by mutableStateOf(null)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    init {
        if (Firebase.auth.currentUser != null) {
            fetchUserProfile()
        }
    }

    fun registerUser(email: String, password: String, name: String) {
        viewModelScope.launch {
            errorMessage = null
            val result = authRepository.registerUser(email, password, name)
            result.onSuccess { userData ->
                user = userData
                errorMessage = null
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            errorMessage = null
            val result = authRepository.loginUser(email, password)
            result.onSuccess { userData ->
                user = userData
                errorMessage = null
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getUserProfile()
                user = currentUser
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun updateUserProfile(user: User, profileImageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.updateUserProfile(user, profileImageUri)
                fetchUserProfile()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun logout() {
        authRepository.logout()
        user = null
        errorMessage = null
    }
}