package co.id.bankbsi.superapp.testcodeasp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.id.bankbsi.superapp.testcodeasp.data.model.Post

@Entity(tableName = "cached_posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    fun toPost() = Post(userId, id, title, body)
}

fun Post.toEntity() = PostEntity(id, userId, title, body)
