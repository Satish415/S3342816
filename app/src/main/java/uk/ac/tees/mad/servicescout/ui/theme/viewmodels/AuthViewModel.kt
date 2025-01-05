package uk.ac.tees.mad.servicescout.ui.theme.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}