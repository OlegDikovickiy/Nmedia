package ru.netology.nmadia_hw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.databinding.CardPostBinding
import ru.netology.nmadia_hw.dto.Post

interface OnInteractionListener {
    fun like (post: Post)
    fun share (post: Post)
    fun remove (post: Post)
    fun edit (post: Post)
}

//typealias OnItemLikeListener = (post: Post) -> Unit
//typealias OnItemShareListener = (post: Post) -> Unit
//typealias OnRemoveListener = (post: Post) -> Unit
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
    private val onInteractionListener: OnInteractionListener
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
        return PostViewHolder(binding, onInteractionListener)
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
    private val onInteractionListener: OnInteractionListener
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
                onInteractionListener.like(post)
            }

            repostIcon.setOnClickListener {
                onInteractionListener.share(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when( item.itemId){
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
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