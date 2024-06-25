package com.example.intermediateapplication1.ui.story

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.intermediateapplication1.data.ListStoryItem
import com.example.intermediateapplication1.data.UserPreference
import com.example.intermediateapplication1.retrofit.ApiService
import kotlinx.coroutines.flow.first

class StoryRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    private var pagingSource: StoryPagingSource? = null

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                StoryPagingSource(apiService, userPreference).also { pagingSource = it }
            }
        ).liveData
    }

    suspend fun getAllStories(): List<ListStoryItem> {
        val token = userPreference.getUserToken().first() ?: return emptyList()
        val response = apiService.getStories("Bearer $token")
        return response.listStory.filterNotNull()
    }

    fun invalidatePaging() {
        pagingSource?.invalidate()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference).also { instance = it }
            }
    }
}
