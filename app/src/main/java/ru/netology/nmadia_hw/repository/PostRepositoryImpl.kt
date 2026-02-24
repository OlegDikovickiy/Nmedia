package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.nmadia_hw.api.PostsApi
import ru.netology.nmadia_hw.dao.PostDao
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.entity.toDto
import ru.netology.nmadia_hw.entity.toEntity
import ru.netology.nmadia_hw.error.ApiError
import ru.netology.nmadia_hw.error.NetworkError
import ru.netology.nmadia_hw.error.UnknownError
import ru.netology.nmadia_hw.util.LocalId
import java.io.IOException

class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map { it.toDto() }

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            withContext(Dispatchers.IO) {
                dao.insert(body.map { it.toEntity(pending = false, pendingError = false) })
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun refresh() = getAll()

    override suspend fun likeById(id: Long) {
        if (withContext(Dispatchers.IO) { dao.isPending(id) }) {
            throw ApiError(400, "Нельзя лайкать несохранённый пост")
        }

        try {
            withContext(Dispatchers.IO) { dao.likeById(id) }

            val likedNow = withContext(Dispatchers.IO) { dao.getLikedByMe(id) }
            val response = if (likedNow) PostsApi.service.likeById(id) else PostsApi.service.dislikeById(id)

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            withContext(Dispatchers.IO) { dao.likeById(id) }
            throw NetworkError
        } catch (e: ApiError) {
            withContext(Dispatchers.IO) { dao.likeById(id) }
            throw e
        } catch (e: Exception) {
            withContext(Dispatchers.IO) { dao.likeById(id) }
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        getAll()
    }

    override suspend fun removeById(id: Long) {
        try {
            withContext(Dispatchers.IO) { dao.removeById(id) }

            // если это pending-пост, на сервер не ходим
            if (id < 0) return

            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            getAll()
            throw NetworkError
        } catch (e: ApiError) {
            getAll()
            throw e
        } catch (e: Exception) {
            getAll()
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        val localId = if (post.id == 0L) LocalId.nextId() else post.id

        val localPost = post.copy(id = localId)
        withContext(Dispatchers.IO) {
            dao.insert(localPost.toEntity(pending = true, pendingError = false))
        }
        try {
            val request = localPost.copy(id = 0L) // на сервер отправляем как новый
            val response = PostsApi.service.save(request)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            val saved = response.body() ?: throw ApiError(response.code(), response.message())

            withContext(Dispatchers.IO) {
                // заменяем временный id на серверный
                dao.replaceId(localId = localId, serverId = saved.id)
                dao.insert(saved.toEntity(pending = false, pendingError = false))
            }
        } catch (e: IOException) {
            withContext(Dispatchers.IO) { dao.setPendingError(localId, true) }
            throw NetworkError
        } catch (e: ApiError) {
            withContext(Dispatchers.IO) { dao.setPendingError(localId, true) }
            throw e
        } catch (e: Exception) {
            withContext(Dispatchers.IO) { dao.setPendingError(localId, true) }
            throw UnknownError
        }
    }

    // Для Retry
    override suspend fun retryPendingSaves() {
        val pending = withContext(Dispatchers.IO) { dao.getPending() }
        for (entity in pending) {
            try {
                withContext(Dispatchers.IO) { dao.setPendingState(entity.id, pending = true, pendingError = false) }

                val dto = entity.toDto().copy(id = 0L)
                val response = PostsApi.service.save(dto)
                if (!response.isSuccessful) throw ApiError(response.code(), response.message())

                val saved = response.body() ?: throw ApiError(response.code(), response.message())

                withContext(Dispatchers.IO) {
                    dao.replaceId(localId = entity.id, serverId = saved.id)
                    dao.insert(saved.toEntity(pending = false, pendingError = false))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.IO) { dao.setPendingError(entity.id, true) }
            }
        }
    }
}
