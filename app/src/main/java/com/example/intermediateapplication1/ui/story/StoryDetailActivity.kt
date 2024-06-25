package com.example.intermediateapplication1.ui.story

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.intermediateapplication1.databinding.ActivityStoryDetailBinding

class StoryDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO_URL = "photourl"
        const val EXTRA_DESC = "desc"
        const val EXTRA_NAME = "name"
    }

    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTransitionNames()
        displayStoryDetails()
    }

    private fun setupTransitionNames() {
        binding.imageDesc.transitionName = "imageTransition"
        binding.tvName.transitionName = "titleTransition"
        binding.tvDesc.transitionName = "descTransition"
    }

    private fun displayStoryDetails() {
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        val desc = intent.getStringExtra(EXTRA_DESC)
        val name = intent.getStringExtra(EXTRA_NAME)

        binding.apply {
            Glide.with(this@StoryDetailActivity)
                .load(photoUrl)
                .into(imageDesc)
            tvDesc.text = desc
            tvName.text = name
        }
    }
}
