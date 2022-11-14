package ru.netology.nmedia.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.exception.ApiException
import ru.netology.nmedia.exception.NetWorkException
import ru.netology.nmedia.exception.UnknownException
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val postApi: PostsApiService,
) : PostRepository {

    override val data = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000)
                val response = postApi.getNewer(id)
                if (!response.isSuccessful) {
                    throw ApiException(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiException(response.code(), response.message())
                dao.insert(body.map { it.copy(show = false) }.toEntity())
                emit(body.size)
            } catch (e: IOException) {
                throw NetWorkException
            }
            catch (e: CancellationException) {
                throw e
            }
            catch (e: Exception) {
                throw UnknownException
            }
        }
    }
        .flowOn(Dispatchers.Default)

    override suspend fun getUnreadPosts() {
        dao.getUnreadPosts()
    }

    override suspend fun makePostReaded() {
        dao.makePostReaded()
    }

    override suspend fun getAll() {
        try {
            val response = postApi.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(body.map { it.copy(show = true) }.toEntity())
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = postApi.likeById(id)
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
            val response = postApi.removeById(id)
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
            val response = postApi.disLikeById(id)
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
            val response = postApi.save(post)
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