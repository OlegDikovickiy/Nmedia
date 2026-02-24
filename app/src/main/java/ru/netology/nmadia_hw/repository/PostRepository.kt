package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import ru.netology.nmadia_hw.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>

    suspend fun getAll()
    suspend fun refresh()

    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)

    suspend fun retryPendingSaves()
}
