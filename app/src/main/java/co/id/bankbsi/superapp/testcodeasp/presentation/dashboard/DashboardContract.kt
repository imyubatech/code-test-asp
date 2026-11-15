package co.id.bankbsi.superapp.testcodeasp.presentation.dashboard

import co.id.bankbsi.superapp.testcodeasp.data.model.Post

interface DashboardContract {
    interface View {
        fun displayPosts(posts: List<Post>, bookmarkedIds: Set<Int>)
        fun showFeedState(isLoading: Boolean, isEmpty: Boolean, errorMessage: String?)
        fun showRefreshing(refreshing: Boolean)
        fun showUserMessage(messageRes: Int)
    }
}
