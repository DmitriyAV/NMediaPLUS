package ru.netology.nmedia.repository

import Media
import MediaUpload
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post


interface PostRepository {

    val data: Flow<PagingData<Post>>
    suspend fun likeById(id: Long)
    suspend fun disLikeById(id: Long)
    fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    fun video()
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun getUnreadPosts()
    suspend fun makePostReaded()
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun signIn(login: String, pass: String)
    suspend fun signUp(name: String, login: String, pass: String)
}
