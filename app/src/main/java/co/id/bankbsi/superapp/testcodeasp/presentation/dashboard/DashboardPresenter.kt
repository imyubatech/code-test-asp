package co.id.bankbsi.superapp.testcodeasp.presentation.dashboard

import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.data.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DashboardPresenter(
    private var view: DashboardContract.View?,
    private val repository: PostRepository

) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var refreshJob: Job? = null
    private val pendingBookmarks = mutableSetOf<Int>()

    private var posts: List<Post> = emptyList()
    private var bookmarkIds: Set<Int> = emptySet()
    private var loading = false
    private var hasAttemptedFetch = false
    private var errorMessage: String? = null

    fun start() {
        scope.launch {
            repository.observeCachedPosts().collect { cached ->
                posts = cached
                render()
            }
        }
        scope.launch {
            repository.observeBookmarkIds().collect { ids ->
                bookmarkIds = ids
                render()
            }
        }
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            loading = true
            errorMessage = null
            render()
            try {
                // Immediate list update avoids an empty-state flash while Room emits.
                posts = repository.refreshPosts()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (posts.isEmpty()) {
                    // A localized message is rendered by the Activity; avoid exposing raw HTTP errors.
                    errorMessage = ""
                } else {
                    view?.showUserMessage(R.string.offline_cached)
                }
            } finally {
                hasAttemptedFetch = true
                loading = false
                render()
            }
        }
    }

    fun toggleBookmark(post: Post) {
        if (!pendingBookmarks.add(post.id)) return
        scope.launch {
            try {
                repository.toggleBookmark(post)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                view?.showUserMessage(R.string.bookmark_failed)
            } finally {
                pendingBookmarks.remove(post.id)
            }
        }
    }

    private fun render() {
        view?.displayPosts(posts, bookmarkIds)
        view?.showFeedState(
            isLoading = loading && posts.isEmpty(),
            isEmpty = hasAttemptedFetch && !loading && posts.isEmpty() && errorMessage == null,
            errorMessage = if (posts.isEmpty() && !loading) errorMessage else null
        )
        view?.showRefreshing(loading && posts.isNotEmpty())
    }

    fun detach() {
        view = null
        scope.cancel()
    }
}
