package co.id.bankbsi.superapp.testcodeasp.presentation.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.data.model.ImageUrls
import co.id.bankbsi.superapp.testcodeasp.data.model.Post
import co.id.bankbsi.superapp.testcodeasp.databinding.ItemPostBinding
import com.bumptech.glide.Glide

class PostAdapter(
    private val onBookmarkClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DIFF) {

    private var bookmarkedIds: Set<Int> = emptySet()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    fun setBookmarkedIds(newIds: Set<Int>) {
        if (bookmarkedIds == newIds) return
        val changedIds = (bookmarkedIds - newIds) + (newIds - bookmarkedIds)
        bookmarkedIds = newIds.toSet()
        currentList.forEachIndexed { index, post ->
            if (post.id in changedIds) notifyItemChanged(index, BOOKMARK_PAYLOAD)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), bookmarkedIds.contains(getItem(position).id))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(BOOKMARK_PAYLOAD)) {
            holder.bindBookmark(getItem(position), bookmarkedIds.contains(getItem(position).id))
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, saved: Boolean) {
            binding.apply {
                textTitle.text = post.title.replaceFirstChar { it.titlecase() }
                textBody.text = post.body
                textPostId.text = binding.root.context.getString(R.string.post_number, post.id)
                imagePost.contentDescription = binding.root.context.getString(R.string.post_image, post.id)
            }

            bindBookmark(post, saved)

            val glide = Glide.with(binding.imagePost)
            glide.load(ImageUrls.primary(post.id))
                .centerCrop()
                .placeholder(R.drawable.ic_image_fallback)
                .error(
                    glide.load(ImageUrls.secondary(post.id))
                        .centerCrop()
                        .error(R.drawable.ic_image_fallback)
                )
                .into(binding.imagePost)
        }

        fun bindBookmark(post: Post, saved: Boolean) {
            binding.apply {
                buttonBookmark.setImageResource(
                    if (saved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
                )
                buttonBookmark.contentDescription = binding.root.context.getString(
                    if (saved) R.string.remove_bookmark else R.string.save_bookmark
                )
                buttonBookmark.setOnClickListener { onBookmarkClick(post) }
            }

        }
    }

    companion object {
        private const val BOOKMARK_PAYLOAD = "bookmark_only"
        private val DIFF = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
        }
    }
}
