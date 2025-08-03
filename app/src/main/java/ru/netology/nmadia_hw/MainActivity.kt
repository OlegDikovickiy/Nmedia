package ru.netology.nmadia_hw

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmadia_hw.adapter.PostAdapter
import ru.netology.nmadia_hw.databinding.ActivityMainBinding
import ru.netology.nmadia_hw.databinding.CardPostBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(
            onItemLikeListener = { post ->
                viewModel.like(post.id)
            },
            onItemShareListener = { post ->
                viewModel.share(post.id)
            })


        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts -> adapter.submitList(posts) }
    }
}

