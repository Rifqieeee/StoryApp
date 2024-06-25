package com.example.intermediateapplication1.injection

import android.content.Context
import com.example.intermediateapplication1.data.UserPreference
import com.example.intermediateapplication1.data.dataStore
import com.example.intermediateapplication1.retrofit.ApiConfig
import com.example.intermediateapplication1.ui.login.UserRepository
import com.example.intermediateapplication1.ui.story.StoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val dataStore = context.dataStore
        val userPreference = UserPreference.getInstance(dataStore)
        val apiService = ApiConfig.getApiService("")
        return UserRepository(apiService, userPreference)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val dataStore = context.dataStore
        val userPreference = UserPreference.getInstance(dataStore)
        val userToken = runBlocking { userPreference.getUserToken().first() }
        val apiService = ApiConfig.getApiService(userToken ?: "")
        return StoryRepository.getInstance(apiService, userPreference)
    }
}