package ru.netology.nmadia_hw.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.activity.EditPostFragment
import ru.netology.nmadia_hw.databinding.FragmentPostDetailsBinding
import ru.netology.nmadia_hw.dto.Post

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    private var postId: Long = 0L

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(id: Long) = PostDetailsFragment().apply {
            arguments = bundleOf(ARG_POST_ID to id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = requireArguments().getLong(ARG_POST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: return@observe
            bindPost(post)
        }
    }

    private fun bindPost(post: Post) {
        binding.card.apply {
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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), R.string.no_app_for_video, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                videoContainer.visibility = View.GONE
            }

            likeIcon.setOnClickListener { viewModel.like(post.id) }

            repostIcon.setOnClickListener {
                viewModel.share(post.id)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
            }

            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                viewModel.remove(post.id)
                                parentFragmentManager.popBackStack()
                                true
                            }
                            R.id.edit -> {
                                viewModel.edit(post)
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, EditPostFragment.newInstance())
                                    .addToBackStack(null)
                                    .commit()
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}