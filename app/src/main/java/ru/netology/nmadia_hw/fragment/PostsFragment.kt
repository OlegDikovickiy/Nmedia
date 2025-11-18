package ru.netology.nmadia_hw.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.adapter.OnInteractionListener
import ru.netology.nmadia_hw.adapter.PostAdapter
import ru.netology.nmadia_hw.databinding.FragmentPostsBinding
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.util.AndroidUtils

class PostsFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = PostsFragment()
        private const val ARG_POST_ID = "POST_ID"
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
            override fun like(post: Post) = viewModel.like(post.id)

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
                findNavController().navigate(R.id.action_feedFragment_to_editPostFragment)
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
                // переход к деталям поста через nav_graph
                val args = Bundle().apply {
                    putLong(ARG_POST_ID, post.id)
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_postDetailsFragment,
                    args
                )
            }
        })

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts.toList())
        }

        // скрывать/показывать FAB при редактировании
        viewModel.isEditing.observe(viewLifecycleOwner) { isEditing ->
            binding.add.visibility = if (isEditing) View.GONE else View.VISIBLE
        }

        // кнопка добавления поста — просто навигация
        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        // блок редактирования снизу (inline-редактор)
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

        binding.cancelEdit.setOnClickListener {
            viewModel.cancelEdit()
            AndroidUtils.hideKeyboard(binding.content)
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            viewModel.save(text)
            AndroidUtils.hideKeyboard(binding.content)
        }

        viewModel.emptyShareError.observe(viewLifecycleOwner) { show ->
            if (show == true) {
                Toast.makeText(
                    requireContext(),
                    R.string.error_empty_content,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearEmptyShareError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
