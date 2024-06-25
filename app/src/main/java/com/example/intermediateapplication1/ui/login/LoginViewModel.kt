package com.example.intermediateapplication1.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intermediateapplication1.data.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun loginUser(email: String, password: String, onResult: (LoginResponse?) -> Unit) {
        viewModelScope.launch {
            val response = tryLoginUser(email, password)
            onResult(response)
        }
    }

    private suspend fun tryLoginUser(email: String, password: String): LoginResponse? {
        return try {
            val response = userRepository.loginUser(email, password)
            if (!response.error && response.loginResult != null) {
                userRepository.saveToken(response.loginResult.token)
            }
            response
        } catch (e: Exception) {
            null
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            userRepository.saveToken(token)
        }
    }

    fun getToken(): Flow<String?> = userRepository.getToken()
}
