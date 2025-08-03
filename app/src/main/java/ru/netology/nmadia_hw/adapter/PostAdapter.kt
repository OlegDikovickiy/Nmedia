package ru.netology.nmadia_hw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.databinding.CardPostBinding
import ru.netology.nmadia_hw.dto.Post

typealias OnItemLikeListener = (post: Post) -> Unit
typealias OnItemShareListener = (post: Post) -> Unit

private fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> String.format("%.1fK", count / 1_000.0).replace(".0", "")
        count < 1_000_000 -> "${count / 1_000}K"
        count < 10_000_000 -> String.format("%.1fM", count / 1_000_000.0).replace(".0", "")
        else -> "${count / 1_000_000}M"
    }
}

class PostAdapter(
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener
) :
    ListAdapter <Post, PostViewHolder>(PostDiffCallback) {

//    var list: List<Post> = emptyList()
//        @SuppressLint("NotifyDataSetChanged")
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onItemLikeListener, onItemShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = list[position]
//        holder.bind(post)
        val post = getItem(position)
        holder.bind(post)
    }

//    override fun getItemCount(): Int = list.size
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = formatCount(post.likes)
            repostCount.text = formatCount(post.shares)
            viewsCount.text = formatCount(post.views)

            likeIcon.setImageResource(
                if (post.likedByMe) R.drawable.ic_liked else R.drawable.ic_like
            )

            likeIcon.setOnClickListener {
                onItemLikeListener(post)
            }

            repostIcon.setOnClickListener {
                onItemShareListener(post)
            }
        }
    }
}

object PostDiffCallback: DiffUtil.ItemCallback<Post>(){
    override fun areItemsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }

}