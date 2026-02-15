package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmadia_hw.api.PostsApi
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.model.FeedModel
import java.io.IOException

class PostRepositoryRetrofitImpl : PostRepository {

    private val state = MutableLiveData(FeedModel())

    override fun getAll(): LiveData<FeedModel> {
        loadPosts(isRefreshing = false)
        return state
    }

    override fun refresh() {
        loadPosts(isRefreshing = true)
    }

    private fun setLoading(isRefreshing: Boolean) {
        val prev = state.value ?: FeedModel()
        state.postValue(
            prev.copy(
                loading = !isRefreshing && prev.posts.isEmpty(),
                refreshing = isRefreshing,
                error = false,
                errorMessage = null,
                empty = false,
            )
        )
    }

    private fun setError(message: String) {
        val current = state.value ?: FeedModel()
        state.postValue(
            current.copy(
                loading = false,
                refreshing = false,
                error = true,
                errorMessage = message,
            )
        )
    }

    private fun <T> errorMessage(response: Response<T>): String {
        val code = response.code()
        val serverText = try {
            response.errorBody()?.string()
        } catch (_: IOException) {
            null
        }

        val suffix = serverText
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { ": $it" }
            ?: ""

        return "Ошибка сервера ($code)$suffix"
    }

    private fun loadPosts(isRefreshing: Boolean) {
        setLoading(isRefreshing)

        PostsApi.service.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (!response.isSuccessful) {
                    setError(errorMessage(response))
                    return
                }

                val body = response.body()
                if (body == null) {
                    setError("Пустой ответ сервера")
                    return
                }

                state.postValue(
                    FeedModel(
                        posts = body,
                        loading = false,
                        refreshing = false,
                        error = false,
                        errorMessage = null,
                        empty = body.isEmpty(),
                    )
                )
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                setError("Ошибка сети: ${t.message ?: "неизвестная"}")
            }
        })
    }

    override fun likeById(id: Long) {
        val current = state.value ?: return
        val post = current.posts.find { it.id == id } ?: return

        val call = if (post.likedByMe) {
            PostsApi.service.dislikeById(id)
        } else {
            PostsApi.service.likeById(id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    setError(errorMessage(response))
                    return
                }
                val body = response.body()
                if (body == null) {
                    setError("Пустой ответ сервера")
                    return
                }

                val cs = state.value ?: return
                val updated = cs.posts.map { if (it.id == body.id) body else it }
                state.postValue(cs.copy(posts = updated, empty = updated.isEmpty(), error = false, errorMessage = null))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                setError("Ошибка сети: ${t.message ?: "неизвестная"}")
            }
        })
    }

    override fun shareById(id: Long) {
        refresh()
    }

    override fun removeById(id: Long) {
        PostsApi.service.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    setError(errorMessage(response))
                    return
                }

                val cs = state.value ?: return
                val updated = cs.posts.filterNot { it.id == id }
                state.postValue(cs.copy(posts = updated, empty = updated.isEmpty(), error = false, errorMessage = null))
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                setError("Ошибка сети: ${t.message ?: "неизвестная"}")
            }
        })
    }

    override fun save(post: Post): Post {
        PostsApi.service.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    setError(errorMessage(response))
                    return
                }
                val saved = response.body()
                if (saved == null) {
                    setError("Пустой ответ сервера")
                    return
                }

                val cs = state.value ?: return
                val updated = if (post.id == 0L) {
                    listOf(saved) + cs.posts
                } else {
                    cs.posts.map { if (it.id == saved.id) saved else it }
                }

                state.postValue(cs.copy(posts = updated, empty = updated.isEmpty(), error = false, errorMessage = null))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                setError("Ошибка сети: ${t.message ?: "неизвестная"}")
            }
        })

        return post
    }
}
