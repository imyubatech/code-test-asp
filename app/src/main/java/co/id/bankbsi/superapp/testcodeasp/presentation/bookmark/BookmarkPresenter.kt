package co.id.bankbsi.superapp.testcodeasp.presentation.bookmark

import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.data.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BookmarkPresenter(
private var view: BookmarkContract.View?,
private val repository: PostRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pendingRemovals = mutableSetOf<Int>()

    fun start() {
        view?.showLoading(true)
        scope.launch {
            try {
                repository.observeBookmarks().collect { bookmarks ->
                    view?.showLoading(false)
                    view?.displayBookmarks(bookmarks)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                view?.showLoading(false)
                view?.showUserMessage(R.string.bookmark_load_failed)
            }
        }
    }

    fun removeBookmark(postId: Int) {
        if (!pendingRemovals.add(postId)) return
        scope.launch {
            try {
                repository.removeBookmark(postId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                view?.showUserMessage(R.string.bookmark_failed)
            } finally {
                pendingRemovals.remove(postId)
            }
        }
    }

    fun detach() {
        view = null
        scope.cancel()
    }
}
