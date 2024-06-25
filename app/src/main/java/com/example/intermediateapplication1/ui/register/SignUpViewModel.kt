package com.example.intermediateapplication1.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intermediateapplication1.data.RegisterResponse
import com.example.intermediateapplication1.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignUpViewModel : ViewModel() {

    private val apiService by lazy { ApiConfig.getApiService() }

    fun registerUser(name: String, email: String, password: String, onResult: (RegisterResponse?) -> Unit) {
        viewModelScope.launch {
            val response = try {
                apiService.register(name, email, password)
            } catch (e: IOException) {
                logError("Network error", e)
                null
            } catch (e: HttpException) {
                logError("HTTP error: ${e.response()?.errorBody()?.string()}", e)
                null
            } catch (e: Exception) {
                logError("Unexpected error", e)
                null
            }
            onResult(response)
        }
    }

    private fun logError(message: String, exception: Exception) {
        Log.e("SignUpViewModel", message, exception)
    }
}
