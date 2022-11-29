package ru.netology.nmedia.repository


import Media
import MediaUpload
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.exception.ApiException
import ru.netology.nmedia.exception.AppError
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

    @Inject
    lateinit var auth: AppAuth

    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        pagingSourceFactory = { PostPagingSource(postApi) },
    ).flow

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

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: java.lang.Exception) {
            throw UnknownException
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = postApi.upload(media)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }

            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: java.lang.Exception) {
            throw UnknownException
        }
    }

    override suspend fun signIn(login: String, pass: String) {
        try {
            val response = postApi.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val authState =
                response.body() ?: throw ApiException(response.code(), response.message())
            authState.token?.let { auth.setAuth(authState.id, it) }

        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: java.lang.Exception) {
            throw UnknownException
        }
        return
    }

    override suspend fun signUp(name: String, login: String, pass: String) {
        try {
            val response = postApi.registerUser(name, login, pass)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val authState =
                response.body() ?: throw ApiException(response.code(), response.message())
            authState.token?.let { auth.setAuth(authState.id, it) }

        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: java.lang.Exception) {
            throw UnknownException
        }
    }

}