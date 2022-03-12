package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, callback: GetAsyncCallback<Post>)
    fun save(post: Post?, callBack: GetDeletCallback)
    fun removeById(id: Long, callback: GetDeletCallback)
    fun dislikeById(id: Long, callback: GetAsyncCallback<Post>)
    fun getPostAsync(callback: GetAsyncCallback<List<Post>>)

    interface GetAsyncCallback<P>{
        fun onSuccess(posts: P) {}
        fun onError(exception: Exception) {}
    }

    interface GetDeletCallback {
        fun onSuccess() {}
        fun onError(exception: Exception) {}
    }

}
