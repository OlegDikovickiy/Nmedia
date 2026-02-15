package ru.netology.nmadia_hw.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmadia_hw.dto.Post

interface PostsApiService {
    @GET("api/posts")
    fun getAll(): Call<List<Post>>

    @POST("api/posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("api/posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>

    @POST("api/posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @DELETE("api/posts/{id}/likes")
    fun dislikeById(@Path("id") id: Long): Call<Post>
}
