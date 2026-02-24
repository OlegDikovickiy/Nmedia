package ru.netology.nmadia_hw

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmadia_hw.db.AppDb
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.error.AppError
import ru.netology.nmadia_hw.model.FeedModel
import ru.netology.nmadia_hw.repository.PostRepository
import ru.netology.nmadia_hw.repository.PostRepositoryImpl
import ru.netology.nmadia_hw.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = null,
    content = "",
    published = "",
    likes = 0,
    likedByMe = false,
    shares = 0,
    views = 0,
    video = null,
    attachment = null,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDb.getInstance(application).postDao()
    private val repository: PostRepository = PostRepositoryImpl(dao)

    val data: LiveData<List<Post>> = repository.data

    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel> = _dataState

    val edited = MutableLiveData(empty)

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> = _isEditing

    val emptyShareErrorEvent = SingleLiveEvent<Unit>()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModel(loading = true, error = false, errorMessage = null)
            repository.getAll()
            _dataState.value = FeedModel()
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModel(refreshing = true, error = false, errorMessage = null)
            repository.refresh()
            _dataState.value = FeedModel()
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun like(id: Long) = likeById(id)
    fun remove(id: Long) = removeById(id)

    fun share(id: Long) = viewModelScope.launch {
        try {
            repository.shareById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun save(text: String) {
        edited.value?.let {
            val content = text.trim()
            if (content.isNotBlank() && content != it.content) {
                viewModelScope.launch {
                    try {
                        repository.save(it.copy(content = content))
                    } catch (e: AppError) {
                        _dataState.value = FeedModel(error = true, errorMessage = e.message)
                    }
                }
            }
        }
        edited.value = empty
        _isEditing.value = false
    }

    fun edit(post: Post) {
        edited.value = post
        _isEditing.value = true
    }

    fun cancelEdit() {
        edited.value = empty
        _isEditing.value = false
    }

    fun showEmptyShareError() {
        emptyShareErrorEvent.value = Unit
    }
}