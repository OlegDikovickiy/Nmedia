package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmadia_hw.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class PostRepositoryRoomImpl : PostRepository {

    companion object {
        const val BASE_URL = "http://10.0.2.2:9999/"

        private val jsonType = "application/json".toMediaType()
        private val listType = object : TypeToken<List<Post>>() {}.type
        private val postType = object : TypeToken<Post>() {}.type
    }

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val data = MutableLiveData<List<Post>>(emptyList())

    override fun getAll(): LiveData<List<Post>> {
        loadPosts()
        return data
    }

    private fun loadPosts() {
        thread {
            val request = Request.Builder()
                .url("${BASE_URL}api/posts")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@thread
                    val body = response.body?.string() ?: return@thread
                    val posts: List<Post> = gson.fromJson(body, listType)
                    data.postValue(posts)
                }
            } catch (_: IOException) {
            }
        }
    }

    override fun likeById(id: Long) {
        thread {
            val current = data.value.orEmpty()
            val post = current.find { it.id == id } ?: return@thread

            val requestBuilder = Request.Builder()
                .url("${BASE_URL}api/posts/$id/likes")

            val request = if (!post.likedByMe) {
                requestBuilder
                    .post("".toRequestBody(jsonType))
                    .build()
            } else {
                requestBuilder
                    .delete()
                    .build()
            }

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@thread
                    val body = response.body?.string() ?: return@thread
                    val updated: Post = gson.fromJson(body, postType)

                    val updatedList = current.map { if (it.id == updated.id) updated else it }
                    data.postValue(updatedList)
                }
            } catch (_: IOException) {
            }
        }
    }

    override fun shareById(id: Long) {
        thread {
            // На сервере не смог найти API для share
            loadPosts()
        }
    }

    override fun removeById(id: Long) {
        thread {
            val request = Request.Builder()
                .url("${BASE_URL}api/posts/$id")
                .delete()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@thread

                    // После удаления обновляем ленту локально: просто убираем пост из списка
                    val current = data.value.orEmpty()
                    val updatedList = current.filterNot { it.id == id }
                    data.postValue(updatedList)
                }
            } catch (_: IOException) {
            }
        }
    }

    override fun save(post: Post): Post {
        thread {
            val json = gson.toJson(post)
            val body = json.toRequestBody(jsonType)

            val request = Request.Builder()
                .url("${BASE_URL}api/posts")
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@thread
                    val responseBody = response.body?.string() ?: return@thread
                    val saved: Post = gson.fromJson(responseBody, postType)

                    val current = data.value.orEmpty()

                    val updatedList = if (post.id == 0L) {
                        listOf(saved) + current
                    } else {
                        current.map { if (it.id == saved.id) saved else it }
                    }
                    data.postValue(updatedList)
                }
            } catch (_: IOException) {
            }
        }

        return post
    }
}
