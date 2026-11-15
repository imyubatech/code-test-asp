package co.id.bankbsi.superapp.testcodeasp.presentation.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.databinding.ActivityBookmarkBinding
import co.id.bankbsi.superapp.testcodeasp.presentation.core.BaseActivity
import co.id.bankbsi.superapp.testcodeasp.presentation.dashboard.DashboardActivity
import co.id.bankbsi.superapp.testcodeasp.presentation.shared.PostAdapter

class BookmarkActivity : BaseActivity(), BookmarkContract.View {
    override val sessionRequired = true

    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var presenter: BookmarkPresenter
    private lateinit var bookmarkAdapter: PostAdapter
    private var loading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSessionActive()) return

        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookmarkAdapter = PostAdapter { post -> presenter.removeBookmark(post.id) }

        binding.apply {
            bookmarksList.apply {
                layoutManager = LinearLayoutManager(this@BookmarkActivity)
                adapter = bookmarkAdapter
            }

            tabExplore.setOnClickListener { openDashboard() }
            tabSaved.setOnClickListener { binding.bookmarksList.smoothScrollToPosition(0) }
            buttonBrowse.setOnClickListener { openDashboard() }
            buttonLogout.setOnClickListener { logout() }
            buttonLanguage.setOnClickListener { toggleLanguage() }
        }


        presenter = BookmarkPresenter(this, app.postRepository)
        presenter.start()
    }

    private fun openDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun displayBookmarks(bookmarks: List<Post>) {
        bookmarkAdapter.submitList(bookmarks)
        bookmarkAdapter.setBookmarkedIds(bookmarks.map { it.id }.toSet())
        binding.apply {
            textSavedCount.text = resources.getQuantityString(R.plurals.saved_count, bookmarks.size, bookmarks.size)
            emptyState.visibility = if (!loading && bookmarks.isEmpty()) View.VISIBLE else View.GONE
            bookmarksList.visibility = if (bookmarks.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun showLoading(isLoading: Boolean) {
        loading = isLoading
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun showUserMessage(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) presenter.detach()
        if (::binding.isInitialized) binding.bookmarksList.adapter = null
        super.onDestroy()
    }
}
