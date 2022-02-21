package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, callback: GetLikeCallback)
    fun save(post: Post?, callBack: GetDeletCallback)
    fun removeById(id: Long, callback: GetDeletCallback)
    fun dislikeById(id: Long, callback: GetLikeCallback)
    fun getPostAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(exception: Exception) {}
    }

    interface GetLikeCallback {
        fun onSuccess(post: Post) {}
        fun onError(exception: Exception) {}
    }

    interface GetDeletCallback {
        fun onSuccess() {}
        fun onError(exception: Exception) {}
    }

}
