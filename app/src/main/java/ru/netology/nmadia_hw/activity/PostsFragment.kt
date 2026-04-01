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
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.adapter.OnInteractionListener
import ru.netology.nmadia_hw.adapter.PostAdapter
import ru.netology.nmadia_hw.databinding.FragmentPostsBinding
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.fragment.PostDetailsFragment

@AndroidEntryPoint
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
            override fun like(post: Post) = Unit
            override fun share(post: Post) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
            }
            override fun remove(post: Post) = Unit
            override fun edit(post: Post) = Unit

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
                    .replace(R.id.fragment_container, PostDetailsFragment.newInstance(post.id))
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.data.collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { state ->
                val isLoading =
                    state.refresh is LoadState.Loading ||
                            state.append is LoadState.Loading ||
                            state.prepend is LoadState.Loading

                binding.swipeRefresh.isRefreshing = isLoading
                binding.progress.visibility =
                    if (state.refresh is LoadState.Loading) View.VISIBLE else View.GONE

                val refreshError = state.refresh as? LoadState.Error
                binding.errorGroup.visibility =
                    if (refreshError != null) View.VISIBLE else View.GONE
                binding.errorText.text =
                    refreshError?.error?.message ?: getString(R.string.error_loading)

                val isEmpty =
                    state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                binding.emptyGroup.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newerCount.collect { count ->
                if (count > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Новых постов: $count",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.checkNewer()
            viewModel.refreshFeed()
            adapter.refresh()
        }

        binding.retry.setOnClickListener {
            adapter.retry()
        }

        binding.add.visibility = View.GONE
        binding.bottomPanel.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkNewer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}