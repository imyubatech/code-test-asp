package co.id.bankbsi.superapp.testcodeasp.presentation.bookmark

import co.id.bankbsi.superapp.testcodeasp.data.model.Post

interface BookmarkContract {
    interface View {
        fun displayBookmarks(bookmarks: List<Post>)
        fun showLoading(isLoading: Boolean)
        fun showUserMessage(messageRes: Int)
    }
}
