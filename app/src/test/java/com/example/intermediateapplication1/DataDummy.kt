package com.example.intermediateapplication1

import com.example.intermediateapplication1.data.ListStoryItem

object DataDummy {

    fun generateDummyStories(): List<ListStoryItem>{
        val items:MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100){
            val story = ListStoryItem(
                i.toString(),
                "photo $i",
                "name $i",
                "description $i",
            )
            items.add(story)
        }
        return items
    }
}
