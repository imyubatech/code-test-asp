package co.id.bankbsi.superapp.testcodeasp

import co.id.bankbsi.superapp.testcodeasp.data.local.*
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.data.remote.ApiService
import co.id.bankbsi.superapp.testcodeasp.data.repository.PostRepository
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PostRepositoryTest {
    private val post = Post(7, 42, "A saved story", "Body of the story")
    private val posts = FakePostDao()
    private val bookmarks = FakeBookmarkDao()
    private var apiFailure: IOException? = null
    private val api = object : ApiService {
        override suspend fun getPosts(): List<Post> {
            apiFailure?.let { throw it }
            return listOf(post)
        }
    }
    private val repository = PostRepository(api, posts, bookmarks)

    @Test fun refreshPersistsApiResponseAndEmitsCache() = runTest {
        assertEquals(listOf(post), repository.refreshPosts())
        assertEquals(listOf(post.toEntity()), posts.rows.value)
        assertEquals(listOf(post), repository.observeCachedPosts().first())
    }

    @Test fun networkFailurePreservesExistingCache() = runTest {
        posts.upsert(listOf(post.toEntity()))
        apiFailure = IOException("Offline")
        try {
            repository.refreshPosts()
            fail("Expected a network failure")
        } catch (_: IOException) {
            assertEquals(listOf(post), repository.observeCachedPosts().first())
        }
    }

    @Test fun toggleAddsThenRemovesBookmarkAndUpdatesIds() = runTest {
        assertTrue(repository.toggleBookmark(post))
        assertEquals(listOf(post), repository.observeBookmarks().first())
        assertEquals(setOf(post.id), repository.observeBookmarkIds().first())
        assertFalse(repository.toggleBookmark(post))
        assertTrue(repository.observeBookmarks().first().isEmpty())
        assertTrue(repository.observeBookmarkIds().first().isEmpty())
    }

    @Test fun removingBookmarkDoesNotRemoveCachedPost() = runTest {
        repository.refreshPosts()
        repository.toggleBookmark(post)
        repository.removeBookmark(post.id)
        repository.removeBookmark(post.id) // Removing an absent ID remains safe.
        assertTrue(repository.observeBookmarks().first().isEmpty())
        assertEquals(listOf(post), repository.observeCachedPosts().first())
    }

    @Test fun concurrentTogglesAreSerialized() = runTest {
        // Yield in exists() lets both callers race unless repository locking works.
        listOf(async { repository.toggleBookmark(post) }, async { repository.toggleBookmark(post) }).awaitAll()
        assertFalse(bookmarks.exists(post.id))
    }

    @Test fun entityConversionsPreserveEveryField() {
        assertEquals(post, post.toEntity().toPost())
        assertEquals(post, post.toBookmark().toPost())
    }

    private class FakePostDao : PostDao {
        val rows = MutableStateFlow<List<PostEntity>>(emptyList())
        override fun observeAll(): Flow<List<PostEntity>> = rows
        override suspend fun upsert(posts: List<PostEntity>) {
            rows.value = (rows.value.associateBy { it.id } + posts.associateBy { it.id }).values.sortedBy { it.id }
        }
    }

    private class FakeBookmarkDao : BookmarkDao {
        private val rows = MutableStateFlow<List<BookmarkEntity>>(emptyList())
        override fun observeAll(): Flow<List<BookmarkEntity>> = rows
        override fun observeIds(): Flow<List<Int>> = rows.map { list -> list.map { it.id } }
        override suspend fun exists(id: Int): Boolean {
            val result = rows.value.any { it.id == id }
            kotlinx.coroutines.yield()
            return result
        }
        override suspend fun insert(bookmark: BookmarkEntity) {
            rows.value = (rows.value.filterNot { it.id == bookmark.id } + bookmark).sortedByDescending { it.id }
        }
        override suspend fun deleteById(id: Int) {
            rows.value = rows.value.filterNot { it.id == id }
        }
    }
}
