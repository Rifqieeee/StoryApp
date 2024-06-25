package com.example.intermediateapplication1.ui.story

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.intermediateapplication1.data.ListStoryItem
import com.example.intermediateapplication1.data.UserPreference
import com.example.intermediateapplication1.retrofit.ApiService
import kotlinx.coroutines.flow.first

class StoryPagingSource(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) : PagingSource<Int, ListStoryItem>() {
    companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = userPreference.getUserToken().first() ?: return LoadResult.Error(Exception("Token is null"))
            val response = apiService.getStories("Bearer $token", page = position, size = params.loadSize)
            val stories = response.listStory.filterNotNull()

            LoadResult.Page(
                data = stories,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
