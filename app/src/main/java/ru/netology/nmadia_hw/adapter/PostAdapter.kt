package ru.netology.nmadia_hw.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.databinding.CardPostBinding
import ru.netology.nmadia_hw.dto.Post

interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun openVideo(url: String)
    fun openPost(post: Post)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
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

            likeIcon.isChecked = post.likedByMe
            likeIcon.text = post.likes.toString()
            repostIcon.text = post.shares.toString()
            viewsIcon.text = post.views.toString()

            if (!post.video.isNullOrBlank()) {
                videoContainer.visibility = View.VISIBLE
                videoContainer.setOnClickListener {
                    onInteractionListener.openVideo(post.video!!)
                }
            } else {
                videoContainer.visibility = View.GONE
                videoContainer.setOnClickListener(null)
            }

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
                        when (item.itemId) {
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

            // Клик по карточке (кроме кнопок) — открытие фрагмента поста
            root.setOnClickListener {
                onInteractionListener.openPost(post)
            }
        }
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem == newItem
}
