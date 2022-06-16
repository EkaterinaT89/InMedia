package ru.netology.inmedia.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.inmedia.BuildConfig
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.auth.AuthState
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.dto.Media
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.PushToken

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/"

private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okhttp = OkHttpClient.Builder()
    .addInterceptor(logging)
    .addInterceptor { chain ->
        AppAuth.getInstance().authStateFlow.value.token?.let { token ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        chain.proceed(chain.request())
    }
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okhttp)
    .build()

interface ApiService {
    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

    //  Получение списка сообщений(всех)
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    //  Получение списка сообщений (последние 10)
    @GET("posts/latest?count=10")
    suspend fun getLastTen(): Response<List<Post>>

    //  Создание нового сообщения (id = 0)
    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    //  Получение сообщения по id
    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    // Обновление сообщения (id != 0)
    @POST("posts")
    suspend fun edit(@Body post: Post): Response<Post>

    //    Like сообщения
    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    // Дизлайк сообщения
    @DELETE("posts/{id}/likes")
    suspend fun disLikeById(@Path("id") id: Long): Response<Post>

    // Удаление сообщения по id
    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    //    Получение несуществующего сообщения
    @GET("posts/{id}")
    suspend fun getPostNotExist(@Path("id") id: Long): Response<Post>

    //   Загрузка изображения, mp3, mp4
    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthState>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthState>

//    EVENTS

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @POST("events")
    suspend fun createNewEvent(@Body event: Event): Response<Event>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events")
    suspend fun editEvent(@Body event: Event): Response<Event>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun disLikeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/participants")
    suspend fun takePartEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun unTakePartEvent(@Path("id") id: Long): Response<Event>

    object Api {
        val retrofitService: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }

    }
}