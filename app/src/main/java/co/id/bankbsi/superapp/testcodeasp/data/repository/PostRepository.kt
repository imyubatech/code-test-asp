package co.id.bankbsi.superapp.testcodeasp.data.repository

import co.id.bankbsi.superapp.testcodeasp.data.local.BookmarkDao
import co.id.bankbsi.superapp.testcodeasp.data.local.PostDao
import co.id.bankbsi.superapp.testcodeasp.data.local.toBookmark
import co.id.bankbsi.superapp.testcodeasp.data.local.toEntity
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.data.remote.ApiService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PostRepository(
    private val api: ApiService,
    private val postDao: PostDao,
    private val bookmarkDao: BookmarkDao
) {
    private val bookmarkMutex = Mutex()

    fun observeCachedPosts() = postDao.observeAll().map { rows -> rows.map { it.toPost() } }
    fun observeBookmarks() = bookmarkDao.observeAll().map { rows -> rows.map { it.toPost() } }
    fun observeBookmarkIds() = bookmarkDao.observeIds().map { it.toSet() }

    suspend fun refreshPosts(): List<Post> {
        val posts = api.getPosts()
        postDao.upsert(posts.map { it.toEntity() })
        return posts
    }

    suspend fun toggleBookmark(post: Post): Boolean = bookmarkMutex.withLock {
        val currentlySaved = bookmarkDao.exists(post.id)
        if (currentlySaved) {
            bookmarkDao.deleteById(post.id)
            false
        } else {
            bookmarkDao.insert(post.toBookmark())
            true
        }
    }

    suspend fun removeBookmark(postId: Int) = bookmarkMutex.withLock {
        bookmarkDao.deleteById(postId)
    }
}
