package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import java.lang.Exception

class PostRepositoryImpl : PostRepository {

    override fun likeById(id: Long, callback: PostRepository.GetAsyncCallback<Post>) {
        PostApi.retrofitService.likeById(id)
            .enqueue(object : Callback<Post> {
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(t as Exception)
                }

                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(response.message()))
                        return
                    }
                    val post = response.body() ?: throw RuntimeException("body is null")
                    callback.onSuccess(post)

                }

            })

    }

    override fun dislikeById(id: Long, callback: PostRepository.GetAsyncCallback<Post>) {
        PostApi.retrofitService.dislikeById(id)
            .enqueue(object : Callback<Post> {
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }

                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(response.message()))
                        return
                    }
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))

                }

            })

    }


    override fun save(post: Post?, callback: PostRepository.GetAsyncCallback<Unit>) {
        PostApi.retrofitService.save(post)
            .enqueue(object : Callback<Post> {
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }

                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(response.message()))
                        return
                    }
                    callback.onSuccess(Unit)
                }

            })
    }

    override fun removeById(id: Long, callback: PostRepository.GetAsyncCallback<Unit>) {
        PostApi.retrofitService.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(response.message()))
                        return
                    }
                    callback.onSuccess(Unit)
                }

            })
    }

    override fun getPostAsync(callback: PostRepository.GetAsyncCallback<List<Post>>) {
        PostApi.retrofitService.getAll()
            .enqueue(object : Callback<List<Post>> {

                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {

                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(response.message()))
                        return
                    }
                    callback.onSuccess(
                        response.body() ?: throw java.lang.RuntimeException("Body is null")
                    )

                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }

            })
    }

}
