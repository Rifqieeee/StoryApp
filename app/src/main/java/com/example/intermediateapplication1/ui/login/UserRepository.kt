package com.example.intermediateapplication1.ui.login

import com.example.intermediateapplication1.data.LoginResponse
import com.example.intermediateapplication1.data.UserPreference
import com.example.intermediateapplication1.retrofit.ApiService

class UserRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun loginUser(email: String, password: String): LoginResponse =
        apiService.login(email, password)

    suspend fun saveToken(token: String) {
        userPreference.saveUserToken(token)
    }

    fun getToken() = userPreference.getUserToken()

    fun getUserPreference(): UserPreference = userPreference
}
