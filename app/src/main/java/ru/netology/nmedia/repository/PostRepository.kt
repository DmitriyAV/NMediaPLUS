package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post



interface PostRepository {

    val data: Flow<List<Post>>
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
}
