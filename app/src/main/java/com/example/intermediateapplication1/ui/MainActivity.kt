package com.example.intermediateapplication1.ui

import StoryViewModelFactory
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intermediateapplication1.R
import com.example.intermediateapplication1.data.ListStoryItem
import com.example.intermediateapplication1.databinding.ActivityMainBinding
import com.example.intermediateapplication1.injection.Injection
import com.example.intermediateapplication1.ui.login.LoginActivity
import com.example.intermediateapplication1.ui.maps.MapsActivity
import com.example.intermediateapplication1.ui.story.LoadingStateAdapter
import com.example.intermediateapplication1.ui.story.StoryAdapter
import com.example.intermediateapplication1.ui.story.StoryDetailActivity
import com.example.intermediateapplication1.ui.story.StoryViewModel
import com.example.intermediateapplication1.ui.upload.UploadActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(Injection.provideStoryRepository(this))
    }

    private lateinit var postActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                storyViewModel.refreshStories()
            }
        }

        storyViewModel.listStory.observe(this, Observer { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        })


        binding.fab.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            postActivityResultLauncher.launch(intent)
        }

        setList()
        loadStateStory()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btn_maps -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.logout -> {
                    runBlocking {
                        val userPreference = Injection.provideUserRepository(this@MainActivity).getUserPreference()
                        userPreference.saveUserToken("")
                    }
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setList() {
        storyAdapter = StoryAdapter().apply {
            addLoadStateListener { loadState ->
                val listEmpty = loadState.refresh is LoadState.NotLoading && storyAdapter.itemCount == 0
                showEmptyList(listEmpty)

                binding.listStory.isVisible = loadState.source.refresh is LoadState.NotLoading
                binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            }
        }

        binding.listStory.layoutManager = LinearLayoutManager(this)
        binding.listStory.adapter = storyAdapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { storyAdapter.retry() },
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )

        storyAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onClick(data: ListStoryItem) {
                Intent(this@MainActivity, StoryDetailActivity::class.java).also {
                    it.putExtra(StoryDetailActivity.EXTRA_PHOTO_URL, data.photoUrl)
                    it.putExtra(StoryDetailActivity.EXTRA_DESC, data.description)
                    it.putExtra(StoryDetailActivity.EXTRA_NAME, data.name)
                    startActivity(it)
                }
            }
        })

        refreshStories()
    }

    private fun refreshStories() {
        storyViewModel.listStory.observe(this, Observer { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        })
    }

    private fun loadStateStory() {
        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }
    }

    private fun showEmptyList(isEmpty: Boolean) {
        binding.listStory.isVisible = !isEmpty
        binding.emptyList.isVisible = isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
