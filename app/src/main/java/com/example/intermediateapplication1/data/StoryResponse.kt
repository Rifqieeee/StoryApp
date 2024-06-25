package com.example.intermediateapplication1.data

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName

data class StoryResponse(

	@field:SerializedName("listStory")
	val listStory: List<ListStoryItem?> = emptyList(),

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class ListStoryItem(

	@field:SerializedName("photoUrl")
	val photoUrl: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("lon")
	val lon: Double? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
){
	companion object {
		val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
			override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
				return oldItem.id == newItem.id
			}
			override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
				return oldItem == newItem
			}
		}
	}
}
