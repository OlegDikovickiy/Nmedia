package ru.netology.nmadia_hw.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.adapter.OnInteractionListener
import ru.netology.nmadia_hw.adapter.PostAdapter
import ru.netology.nmadia_hw.databinding.ActivityMainBinding
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.util.AndroidUtils

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val newPostLauncher = registerForActivityResult(NewPostResultContract) { result ->
        result?.let { viewModel.save(it) }
    }

    private val editPostLauncher = registerForActivityResult(EditPostResultContract) { editedContent ->
        editedContent?.let { viewModel.save(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) = viewModel.like(post.id)

            override fun share(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
//                val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
            }

            override fun remove(post: Post) = viewModel.remove(post.id)
            override fun edit(post: Post) {
                viewModel.edit(post)
                editPostLauncher.launch(post.content)
            }
        })

        binding.list.adapter = adapter

//        viewModel.data.observe(this) { posts ->
//            val newPostAdded = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
//            adapter.submitList(posts) {
//                if (newPostAdded) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        }

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts.toList()) // новая копия списка для DiffUtil
        }

//        viewModel.edited.observe(this) { post ->
//            if (post.id != 0L) {
//                binding.content.setText(post.content)
//                binding.originalPreview.text = post.content
//            } else {
//                binding.content.setText("")
//                binding.originalPreview.text = ""
//            }
//        }

//        viewModel.isEditing.observe(this) { editing ->
//            binding.editBlock.visibility = if (editing) View.VISIBLE else View.GONE
//
//            if (editing) {
//                binding.content.requestFocus()
//                AndroidUtils.showKeyboard(binding.content)
//            } else {
//                binding.content.clearFocus()
//                AndroidUtils.hideKeyboard(binding.content)
//            }
//        }

        viewModel.isEditing.observe(this) {
            binding.editBlock.visibility = View.GONE
            binding.inputRow.visibility = View.GONE
        }

        binding.add.setOnClickListener {
            newPostLauncher.launch()
        }

//        binding.save.setOnClickListener {
//            val text = binding.content.text.toString()
//            if (text.isBlank()) {
//                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//            viewModel.save(text)
//        }
//
//        binding.cancelEdit.setOnClickListener {
//            viewModel.cancelEdit()
//        }



    }
}