package ru.netology.nmadia_hw

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.repository.PostRepository
import ru.netology.nmadia_hw.repository.PostRepositoryInMemoryImpl

class PostViewModel : ViewModel() {
    private val repository : PostRepository = PostRepositoryInMemoryImpl()
    val data: LiveData<List<Post>> = repository.get()

    fun like(id: Long) = repository.likeById(id)
    fun share(id: Long) = repository.shareById(id)


}