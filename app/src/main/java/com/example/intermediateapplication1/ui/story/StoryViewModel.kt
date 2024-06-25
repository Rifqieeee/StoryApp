package com.example.intermediateapplication1.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.intermediateapplication1.data.ListStoryItem


class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    val listStory: LiveData<PagingData<ListStoryItem>> by lazy {
        val liveData = storyRepository.getStories()
        if (liveData != null) liveData.cachedIn(viewModelScope) else MutableLiveData(PagingData.empty())
    }

    fun refreshStories() {
        storyRepository.invalidatePaging()
    }
}

