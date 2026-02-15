package ru.netology.nmadia_hw.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.api.ApiConfig
import ru.netology.nmadia_hw.databinding.CardPostBinding
import ru.netology.nmadia_hw.dto.AttachmentType
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
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) = with(binding) {
        author.text = post.author
        published.text = post.published
        content.text = post.content

        likeIcon.isChecked = post.likedByMe
        likeIcon.text = post.likes.toString()
        repostIcon.text = post.shares.toString()
        viewsIcon.text = post.views.toString()

        // --- AVATAR через Glide ---
        val avatarUrl = post.authorAvatar
            ?.takeIf { it.isNotBlank() }
            ?.let { "${ApiConfig.BASE_URL}avatars/$it" }

        Glide.with(avatar)
            .load(avatarUrl)
            .placeholder(R.drawable.outline_downloading_24)
            .error(R.drawable.outline_error_48)
            .circleCrop()
            .dontAnimate()
            .timeout(10_000)
            .into(avatar)

        // --- ATTACHMENT (IMAGE) через Glide ---
        val att = post.attachment
        if (att != null && att.type == AttachmentType.IMAGE) {
            attachmentContainer.visibility = View.VISIBLE

            val imageUrl = "${ApiConfig.BASE_URL}images/${att.url}"

            Glide.with(attachmentImage)
                .load(imageUrl)
                .placeholder(R.drawable.outline_downloading_24)
                .error(R.drawable.outline_error_48)
                .centerCrop()
                .dontAnimate()
                .timeout(10_000)
                .into(attachmentImage)
        } else {
            attachmentContainer.visibility = View.GONE
            attachmentImage.setImageDrawable(null)
        }

        // Видео
        if (!post.video.isNullOrBlank()) {
            videoContainer.visibility = View.VISIBLE
            videoContainer.setOnClickListener { onInteractionListener.openVideo(post.video!!) }
        } else {
            videoContainer.visibility = View.GONE
            videoContainer.setOnClickListener(null)
        }

        likeIcon.setOnClickListener { onInteractionListener.like(post) }
        repostIcon.setOnClickListener { onInteractionListener.share(post) }
        menu.setOnClickListener { onInteractionListener.openPost(post) }

        root.setOnClickListener { onInteractionListener.openPost(post) }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem == newItem
}
