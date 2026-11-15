package co.id.bankbsi.superapp.testcodeasp

import android.app.Application
import androidx.room.Room
import co.id.bankbsi.superapp.testcodeasp.data.local.AppDatabase
import co.id.bankbsi.superapp.testcodeasp.data.remote.ApiService
import co.id.bankbsi.superapp.testcodeasp.data.repository.PostRepository
import co.id.bankbsi.superapp.testcodeasp.utils.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TestCodeAspApp: Application() {

    val sessionManager: SessionManager by lazy { SessionManager(applicationContext) }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "glassmark.db")
            .build()
    }

    private val api: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val postRepository: PostRepository by lazy {
        PostRepository(api, database.postDao(), database.bookmarkDao())
    }
}
