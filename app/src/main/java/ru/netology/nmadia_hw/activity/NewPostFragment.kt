package ru.netology.nmadia_hw.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.databinding.FragmentNewPostBinding
import ru.netology.nmadia_hw.util.AndroidUtils

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_INITIAL_CONTENT = "initial_content"

        fun newInstance(initialContent: String? = null) = NewPostFragment().apply {
            arguments = bundleOf(ARG_INITIAL_CONTENT to initialContent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initial = arguments?.getString(ARG_INITIAL_CONTENT).orEmpty()
        if (initial.isNotBlank()) {
            binding.edit.setText(initial)
            binding.edit.setSelection(initial.length)
        }

        binding.ok.setOnClickListener {
            val text = binding.edit.text.toString()
            val trimmed = text.trim()
            if (trimmed.isBlank()) {
                parentFragmentManager.popBackStack()
                return@setOnClickListener
            }
            viewModel.save(trimmed)
            AndroidUtils.hideKeyboard(binding.edit)
            parentFragmentManager.popBackStack()
        }

        binding.edit.requestFocus()
        AndroidUtils.showKeyboard(binding.edit)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}