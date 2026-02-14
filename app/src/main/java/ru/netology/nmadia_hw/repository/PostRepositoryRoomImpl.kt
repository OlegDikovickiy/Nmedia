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
import ru.netology.nmadia_hw.model.FeedModel
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

    private val state = MutableLiveData(FeedModel())

    override fun getAll(): LiveData<FeedModel> {
        loadPosts(isRefreshing = false)
        return state
    }

    override fun refresh() {
        loadPosts(isRefreshing = true)
    }

    private fun loadPosts(isRefreshing: Boolean) {
        val prev = state.value ?: FeedModel()

        state.postValue(
            prev.copy(
                loading = !isRefreshing && prev.posts.isEmpty(),
                refreshing = isRefreshing,
                error = false,          // сброс ошибки при каждом запросе
                empty = false,
            )
        )

        thread {
            val request = Request.Builder()
                .url("${BASE_URL}api/posts")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val current = state.value ?: FeedModel()
                        state.postValue(
                            current.copy(
                                loading = false,
                                refreshing = false,
                                error = true,
                            )
                        )
                        return@thread
                    }

                    val body = response.body?.string() ?: run {
                        val current = state.value ?: FeedModel()
                        state.postValue(
                            current.copy(
                                loading = false,
                                refreshing = false,
                                error = true,
                            )
                        )
                        return@thread
                    }

                    val posts: List<Post> = gson.fromJson(body, listType)
                    state.postValue(
                        FeedModel(
                            posts = posts,
                            loading = false,
                            refreshing = false,
                            error = false,          // успех — ошибка точно false
                            empty = posts.isEmpty(),
                        )
                    )
                }
            } catch (_: IOException) {
                val current = state.value ?: FeedModel()
                state.postValue(
                    current.copy(
                        loading = false,
                        refreshing = false,
                        error = true,
                    )
                )
            }
        }
    }

    override fun likeById(id: Long) {
        thread {
            val currentState = state.value ?: return@thread
            val post = currentState.posts.find { it.id == id } ?: return@thread

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

                    val updatedPosts = currentState.posts.map { if (it.id == updated.id) updated else it }
                    state.postValue(
                        currentState.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                        )
                    )
                }
            } catch (_: IOException) {
                val cs = state.value ?: return@thread
                state.postValue(cs.copy(error = true))
            }
        }
    }

    override fun shareById(id: Long) {
        // На сервере API для share нет — оставляем локальный счетчик (опционально) или просто refresh.
        refresh()
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

                    val currentState = state.value ?: return@thread
                    val updatedPosts = currentState.posts.filterNot { it.id == id }
                    state.postValue(
                        currentState.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                        )
                    )
                }
            } catch (_: IOException) {
                val cs = state.value ?: return@thread
                state.postValue(cs.copy(error = true))
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

                    val currentState = state.value ?: return@thread
                    val updatedPosts = if (post.id == 0L) {
                        listOf(saved) + currentState.posts
                    } else {
                        currentState.posts.map { if (it.id == saved.id) saved else it }
                    }

                    state.postValue(
                        currentState.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                        )
                    )
                }
            } catch (_: IOException) {
                val cs = state.value ?: return@thread
                state.postValue(cs.copy(error = true))
            }
        }

        return post
    }
}
