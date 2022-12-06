package ru.netology.nmedia.viewmodel

import MediaUpload
import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.lang.Exception
import javax.inject.Inject


private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    show = false,
    authorId = 0,
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth
): ViewModel(){

    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = auth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
        }.flowOn(Dispatchers.Default)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

   /* val newerPost: LiveData<Int> = data
        .switchMap {
            repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L )
                .catch { e -> e.printStackTrace() }
                .asLiveData(Dispatchers.Default)
        }
        .distinctUntilChanged()*/


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    var currentId: Long? = null

    var lastAction: ActionType? = null

    init {
        loadPosts()
    }

    fun tryAgain() {
        when (lastAction) {
            ActionType.LIKEBYID -> retryLikeById()
            ActionType.DISLIKEBYID -> retryDisLikeById()
            ActionType.SAVE -> retrySave()
            ActionType.LOADPOSTS -> retryLoadPosts()
            ActionType.REMOVEBYID -> retryRemoveById()
            else -> loadPosts()
        }
    }

    fun save() {
        lastAction = ActionType.SAVE
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository. saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun retrySave() {
        save()
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            currentId = id
            lastAction = ActionType.LIKEBYID
            try {
                repository.likeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun retryLikeById() {
        currentId?.let {
            likeById(it)
        }
    }

    fun disLikeById(id: Long) {
        viewModelScope.launch {
            currentId = id
            lastAction = ActionType.DISLIKEBYID
            try {
                repository.disLikeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun retryDisLikeById() {
        currentId?.let {
            disLikeById(it)
        }
    }

    fun shareById(id: Long) {
        repository.shareById(id)
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            currentId = id
            lastAction = ActionType.REMOVEBYID
            try {
                _dataState.value = FeedModelState()
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun retryRemoveById() {
        currentId?.let {
            removeById(it)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun editContent(text: String) {
        val formatted = text.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = formatted)
    }

    fun video() {
        repository.video()
    }

    fun loadPosts() = viewModelScope.launch {
        lastAction = ActionType.LOADPOSTS
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getUnreadPosts() =
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.getUnreadPosts()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }

    fun makePostReaded() =
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.makePostReaded()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }


    fun retryLoadPosts() {
        loadPosts()
    }


}

enum class ActionType {
    LIKEBYID,
    DISLIKEBYID,
    SAVE,
    REMOVEBYID,
    LOADPOSTS
}