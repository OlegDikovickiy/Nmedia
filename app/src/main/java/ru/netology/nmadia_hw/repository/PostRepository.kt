package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.model.FeedModel

interface PostRepository {
    fun getAll(): LiveData<FeedModel>
    fun refresh()
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post): Post
}
