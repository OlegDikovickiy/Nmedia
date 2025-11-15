package ru.netology.nmadia_hw.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.databinding.FragmentEditPostBinding
import ru.netology.nmadia_hw.util.AndroidUtils

class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = EditPostFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val current = viewModel.edited.value
        val startContent = current?.content.orEmpty()
        binding.edit.setText(startContent)
        binding.edit.setSelection(startContent.length)

        binding.ok.setOnClickListener {
            val text = binding.edit.text.toString()
            viewModel.save(text)
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
