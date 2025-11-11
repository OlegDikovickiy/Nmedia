package ru.netology.nmadia_hw.activity

import android.content.Intent
import android.net.Uri
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

    private val editPostLauncher =
        registerForActivityResult(EditPostResultContract) { editedContent ->
            editedContent?.let { viewModel.save(it) } ?: viewModel.cancelEdit()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) = viewModel.like(post.id)

            override fun share(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
            }

            override fun remove(post: Post) = viewModel.remove(post.id)
            override fun edit(post: Post) {
                viewModel.edit(post)
                editPostLauncher.launch(post.content)
            }
            override fun openVideo(url: String) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.no_app_for_video,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts.toList())
        }


        viewModel.isEditing.observe(this) {
            binding.editBlock.visibility = View.GONE
            binding.inputRow.visibility = View.GONE
        }

        binding.add.setOnClickListener {
            newPostLauncher.launch()
        }

    }
}