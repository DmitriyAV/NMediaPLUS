package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.lang.Exception

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    var action: Action? = null
    var actionId: Long? = null

    fun tryAgane(){
        when(action){
            Action.DELETE_ERROR -> actionId?.let { removeById(it) }
            Action.DISLIKE_ERROR -> actionId?.let { dislikeById(it) }
            Action.LIKE_ERROR -> actionId?.let { likeById(it) }
            Action.SAVE_ERROR -> save()
            else -> loadPosts()
        }
    }

    fun loadPosts() {

        _data.postValue(FeedModel(loading = true))
        repository.getPostAsync(object : PostRepository.GetAsyncCallback<List<Post>> {

            override fun onError(exception: Exception) {
                action = Action.LOADING_ERROR
                _data.postValue(FeedModel(error = true, serverError = true))
            }

            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }
        })
    }

    fun save() {
        edited.value.let {

            repository.save(it, object : PostRepository.GetAsyncCallback<Unit> {

                override fun onError(exception: Exception) {
                    action = Action.SAVE_ERROR
                    _data.postValue(FeedModel(error = true, serverError = true))
                }

                override fun onSuccess(u: Unit) {
                    _postCreated.postValue(u)
                    edited.value = empty
                }
            })
        }
        actionId = null
        action = null
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        repository.likeById(id, object : PostRepository.GetAsyncCallback<Post> {
            override fun onError(exception: Exception) {
                action = Action.LIKE_ERROR
                actionId = id
                _data.postValue(FeedModel(error = true, serverError = true))
            }

            override fun onSuccess(posts: Post) {
                val likedPost = _data.value?.posts.orEmpty().map { if (it.id == id) posts else it }
                _data.postValue(FeedModel(posts = likedPost, empty = likedPost.isEmpty()))
            }
        })
        actionId = null
        action = null
    }

    fun dislikeById(id: Long) {
        repository.dislikeById(id, object : PostRepository.GetAsyncCallback<Post> {
            override fun onError(exception: Exception) {
                actionId = id
                action = Action.DISLIKE_ERROR
                _data.postValue(FeedModel(error = true, serverError = true))
            }

            override fun onSuccess(posts: Post) {
                val likedPost = _data.value?.posts.orEmpty().map { if (it.id == id) posts else it }
                val newData = _data.value?.copy(posts = likedPost)
                _data.postValue(newData)
            }
        })
        actionId = null
        action = null
    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.GetAsyncCallback<Unit> {
            override fun onError(exception: Exception) {
                val old = _data.value?.posts.orEmpty()
                _data.postValue(FeedModel(error = true, serverError = true))
                _data.postValue(_data.value?.copy(posts = old))
                action = Action.DELETE_ERROR
                actionId = id

            }

            override fun onSuccess(u: Unit) {
                val update = _data.value?.posts.orEmpty().filter { it.id != id }
                _data.postValue(
                    _data.value?.copy(
                        posts = update
                    )
                )
            }
        })
        actionId = null
        action = null
    }
}

enum class Action {
    DISLIKE_ERROR,
    LIKE_ERROR,
    SAVE_ERROR,
    DELETE_ERROR,
    LOADING_ERROR,
}