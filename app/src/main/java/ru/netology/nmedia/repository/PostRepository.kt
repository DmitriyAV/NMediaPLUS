package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun likeById(id: Long, callback: GetAsyncCallback<Post>)
    fun save(post: Post?, callback: GetAsyncCallback<Unit>)
    fun removeById(id: Long, callback: GetAsyncCallback<Unit>)
    fun dislikeById(id: Long, callback: GetAsyncCallback<Post>)
    fun getPostAsync(callback: GetAsyncCallback<List<Post>>)

    interface GetAsyncCallback<P>{
        fun onSuccess(posts: P) {}
        fun onError(exception: Exception) {}
    }

}
