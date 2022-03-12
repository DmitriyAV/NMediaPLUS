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

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getPostAsync(object : PostRepository.GetAsyncCallback<List<Post>> {

            override fun onError(exception: Exception) {
                _data.postValue(FeedModel(error = true))
            }

            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }
        })
    }

    fun save() {
        edited.value.let {

            repository.save(it, object : PostRepository.GetDeletCallback {

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                }

                override fun onSuccess() {
                    _postCreated.postValue(Unit)
                    edited.value = empty
                }
            })
        }
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
                _data.postValue(FeedModel(error = true))
            }

            override fun onSuccess(posts: Post) {

                val likedPost = _data.value?.posts.orEmpty().map { if (it.id == id) posts else it }
                _data.postValue(FeedModel(posts = likedPost, empty = likedPost.isEmpty()))
            }
        })

    }

    fun dislikeById(id: Long) {
        repository.likeById(id, object : PostRepository.GetAsyncCallback<Post> {
            override fun onError(exception: Exception) {
                _data.postValue(FeedModel(error = true))
            }

            override fun onSuccess(posts: Post) {

                val likedPost = _data.value?.posts.orEmpty().map { if (it.id == id) posts else it }
                _data.postValue(FeedModel(posts = likedPost, empty = likedPost.isEmpty()))
            }
        })

    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.GetDeletCallback {
            override fun onError(exception: Exception) {
                val old = _data.value?.posts.orEmpty()

                _data.postValue(FeedModel(error = true))
                _data.postValue(_data.value?.copy(posts = old))

            }

            override fun onSuccess() {
                val update = _data.value?.posts.orEmpty().filter { it.id != id }
                _data.postValue(
                    _data.value?.copy(
                        posts = update
                    )
                )
            }
        })

    }
}
