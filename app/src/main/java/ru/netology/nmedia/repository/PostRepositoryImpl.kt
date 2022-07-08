package ru.netology.nmedia.repository


import androidx.lifecycle.map
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.exception.ApiException
import ru.netology.nmedia.exception.NetWorkException
import ru.netology.nmedia.exception.UnknownException
import java.io.IOException
import java.lang.Exception


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = PostApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }


    override suspend fun disLikeById(id: Long) {
        try {
            val response = PostApi.retrofitService.disLikeById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override fun shareById(id: Long) {}

    override suspend fun save(post: Post) {
        try {
            val response = PostApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override fun video() {}

}