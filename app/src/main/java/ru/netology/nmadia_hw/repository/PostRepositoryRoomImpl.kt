package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.model.FeedModel
import java.io.IOException
import java.util.concurrent.TimeUnit

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
                error = false,
                empty = false,
            )
        )

        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val current = state.value ?: FeedModel()
                state.postValue(
                    current.copy(
                        loading = false,
                        refreshing = false,
                        error = true,
                    )
                )
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        val current = state.value ?: FeedModel()
                        state.postValue(
                            current.copy(
                                loading = false,
                                refreshing = false,
                                error = true,
                            )
                        )
                        return
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        val current = state.value ?: FeedModel()
                        state.postValue(
                            current.copy(
                                loading = false,
                                refreshing = false,
                                error = true,
                            )
                        )
                        return
                    }

                    val posts: List<Post> = gson.fromJson(body, listType)
                    state.postValue(
                        FeedModel(
                            posts = posts,
                            loading = false,
                            refreshing = false,
                            error = false,
                            empty = posts.isEmpty(),
                        )
                    )
                }
            }
        })
    }

    override fun likeById(id: Long) {
        val currentState = state.value ?: return
        val post = currentState.posts.find { it.id == id } ?: return

        val requestBuilder = Request.Builder()
            .url("${BASE_URL}api/posts/$id/likes")

        val request = if (!post.likedByMe) {
            requestBuilder.post("".toRequestBody(jsonType)).build()
        } else {
            requestBuilder.delete().build()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val cs = state.value ?: FeedModel()
                state.postValue(cs.copy(error = true))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val body = response.body?.string() ?: return
                    val updated: Post = gson.fromJson(body, postType)

                    val cs = state.value ?: return
                    val updatedPosts = cs.posts.map { if (it.id == updated.id) updated else it }
                    state.postValue(
                        cs.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                            error = false,
                        )
                    )
                }
            }
        })
    }

    override fun shareById(id: Long) {
        // Если на сервере нет API для share — просто обновляем список
        refresh()
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val cs = state.value ?: FeedModel()
                state.postValue(cs.copy(error = true))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val cs = state.value ?: return
                    val updatedPosts = cs.posts.filterNot { it.id == id }
                    state.postValue(
                        cs.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                            error = false,
                        )
                    )
                }
            }
        })
    }

    override fun save(post: Post): Post {
        val json = gson.toJson(post)
        val body = json.toRequestBody(jsonType)

        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val cs = state.value ?: FeedModel()
                state.postValue(cs.copy(error = true))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val responseBody = response.body?.string() ?: return
                    val saved: Post = gson.fromJson(responseBody, postType)

                    val cs = state.value ?: return
                    val updatedPosts = if (post.id == 0L) {
                        listOf(saved) + cs.posts
                    } else {
                        cs.posts.map { if (it.id == saved.id) saved else it }
                    }

                    state.postValue(
                        cs.copy(
                            posts = updatedPosts,
                            empty = updatedPosts.isEmpty(),
                            error = false,
                        )
                    )
                }
            }
        })

        return post
    }
}
