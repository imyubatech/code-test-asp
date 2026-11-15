package co.id.bankbsi.superapp.testcodeasp.data.remote

import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
