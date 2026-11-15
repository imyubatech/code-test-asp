package co.id.bankbsi.superapp.testcodeasp.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.databinding.ActivityDashboardBinding
import co.id.bankbsi.superapp.testcodeasp.presentation.bookmark.BookmarkActivity
import co.id.bankbsi.superapp.testcodeasp.presentation.core.BaseActivity
import co.id.bankbsi.superapp.testcodeasp.presentation.shared.PostAdapter

class DashboardActivity : BaseActivity(), DashboardContract.View {
    override val sessionRequired = true

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var presenter: DashboardPresenter
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSessionActive()) return

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textUsername.text = getString(R.string.hello_user, app.sessionManager.username())

        postAdapter = PostAdapter { post -> presenter.toggleBookmark(post) }
        binding.apply {
            postsList.apply {
                layoutManager = LinearLayoutManager(this@DashboardActivity)
                adapter = postAdapter
                setHasFixedSize(false)
            }

            swipeRefresh.setColorSchemeResources(R.color.accent)
            swipeRefresh.setOnRefreshListener { presenter.refresh() }
            buttonRetry.setOnClickListener { presenter.refresh() }
            buttonLogout.setOnClickListener { logout() }
            buttonLanguage.setOnClickListener { toggleLanguage() }

            tabSaved.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, BookmarkActivity::class.java))
                finish()
            }

            tabExplore.setOnClickListener { binding.postsList.smoothScrollToPosition(0) }
        }


        presenter = DashboardPresenter(this, app.postRepository)
        presenter.start()
    }

    override fun displayPosts(posts: List<Post>, bookmarkedIds: Set<Int>) {
        postAdapter.submitList(posts)
        postAdapter.setBookmarkedIds(bookmarkedIds)
        binding.textPostCount.text = resources.getQuantityString(R.plurals.post_count, posts.size, posts.size)
    }

    override fun showFeedState(isLoading: Boolean, isEmpty: Boolean, errorMessage: String?) {
        binding.apply {
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            errorState.visibility = if (errorMessage != null) View.VISIBLE else View.GONE
            postsList.visibility = if (isLoading || isEmpty || errorMessage != null) View.INVISIBLE else View.VISIBLE
            textErrorDetails.text = if (errorMessage.isNullOrBlank()) {
                getString(R.string.network_error_help)
            } else {
                errorMessage
            }
        }
    }

    override fun showRefreshing(refreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = refreshing
    }

    override fun showUserMessage(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) presenter.detach()
        if (::binding.isInitialized) binding.postsList.adapter = null
        super.onDestroy()
    }
}
