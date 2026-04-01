package ru.netology.nmadia_hw.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmadia_hw.dto.Post

interface PostRepository {
    fun data(): Flow<PagingData<Post>>

    suspend fun getNewer(id: Long, count: Int = 10): List<Post>
    suspend fun getLatestOnce(count: Int = 1): List<Post>

    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
}