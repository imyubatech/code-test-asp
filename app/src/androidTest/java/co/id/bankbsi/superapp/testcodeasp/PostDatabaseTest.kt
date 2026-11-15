package co.id.bankbsi.superapp.testcodeasp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.id.bankbsi.superapp.testcodeasp.data.local.*
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostDatabaseTest {
    private lateinit var database: AppDatabase
    private val first = Post(3, 1, "First", "Body one")
    private val second = Post(4, 2, "Second", "Body two")

    @Before fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
    }
    @After fun closeDatabase() { database.close() }

    @Test fun postsAreSortedAndUpsertReplacesExistingId() = runBlocking {
        database.postDao().upsert(listOf(second.toEntity(), first.toEntity()))
        assertEquals(listOf(first, second), database.postDao().observeAll().first().map { it.toPost() })
        val updated = first.copy(title = "Updated")
        database.postDao().upsert(listOf(updated.toEntity()))
        assertEquals(listOf(updated, second), database.postDao().observeAll().first().map { it.toPost() })
    }

    @Test fun bookmarksAreOrderedWithoutDuplicatesAndCanBeDeleted() = runBlocking {
        val dao = database.bookmarkDao()
        dao.insert(first.toBookmark())
        dao.insert(second.toBookmark())
        dao.insert(first.copy(title = "Updated").toBookmark())
        assertTrue(dao.exists(first.id))
        assertEquals(listOf(2, 1), dao.observeAll().first().map { it.id })
        assertEquals(setOf(1, 2), dao.observeIds().first().toSet())
        assertEquals("Updated", dao.observeAll().first().last().title)
        dao.deleteById(1)
        assertFalse(dao.exists(1))
        assertEquals(listOf(2), dao.observeIds().first())
    }

    @Test fun removingBookmarkPreservesCachedData() = runBlocking {
        database.postDao().upsert(listOf(first.toEntity()))
        database.bookmarkDao().insert(first.toBookmark())
        database.bookmarkDao().deleteById(first.id)
        assertTrue(database.bookmarkDao().observeAll().first().isEmpty())
        assertEquals(first, database.postDao().observeAll().first().single().toPost())
    }
}
