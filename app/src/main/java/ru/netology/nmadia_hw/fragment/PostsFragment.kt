package ru.netology.nmadia_hw.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.adapter.OnInteractionListener
import ru.netology.nmadia_hw.adapter.PostAdapter
import ru.netology.nmadia_hw.databinding.FragmentPostsBinding
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.fragment.PostDetailsFragment
import ru.netology.nmadia_hw.util.AndroidUtils

class PostsFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = PostsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                viewModel.like(post.id)
            }

            override fun share(post: Post) {
                viewModel.share(post.id)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.chooser_share_post)
                    )
                )
            }

            override fun remove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
            }

            override fun openVideo(url: String) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.no_app_for_video,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun openPost(post: Post) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        PostDetailsFragment.newInstance(post.id)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter

        binding.retry.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
            binding.emptyGroup.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            binding.errorGroup.visibility = if (state.error) View.VISIBLE else View.GONE
            binding.errorText.text = state.errorMessage ?: getString(R.string.error_loading)
            binding.swipeRefresh.isRefreshing = state.refreshing
        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            val isEditing = post.id != 0L
            binding.editBlock.visibility = if (isEditing) View.VISIBLE else View.GONE
            binding.originalPreview.text = post.content

            if (isEditing) {
                binding.content.setText(post.content)
                binding.content.requestFocus()
                AndroidUtils.showKeyboard(binding.content)
            } else {
                binding.content.text = null
            }
        }

        viewModel.isEditing.observe(viewLifecycleOwner) { isEditing ->
            binding.add.visibility = if (isEditing) View.GONE else View.VISIBLE
        }

        binding.add.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewPostFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        binding.cancelEdit.setOnClickListener {
            viewModel.cancelEdit()
            AndroidUtils.hideKeyboard(binding.content)
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            viewModel.save(text)
            AndroidUtils.hideKeyboard(binding.content)
        }

        viewModel.emptyShareErrorEvent.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                R.string.error_empty_content,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
