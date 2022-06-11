package ru.netology.inmedia.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.inmedia.BuildConfig
import ru.netology.inmedia.dto.Media
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.PushToken

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/"

fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
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


//    @Multipart
//    @POST("media")
//    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

//    @FormUrlEncoded
//    @POST("users/authentication")
//    suspend fun updateUser(
//        @Field("login") login: String,
//        @Field("pass") pass: String
//    ): Response<AuthState>
//
//    @FormUrlEncoded
//    @POST("users/registration")
//    suspend fun registerUser(
//        @Field("login") login: String,
//        @Field("pass") pass: String,
//        @Field("name") name: String
//    ): Response<AuthState>

}