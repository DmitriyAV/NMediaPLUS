import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.api.okhttp
import ru.netology.nmedia.api.retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Provides
    @Singleton
    fun provideApiService(): PostsApiService {
        return retrofit(okhttp())
            .create(PostsApiService::class.java)
    }
}