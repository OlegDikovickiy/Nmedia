package ru.netology.nmadia_hw.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmadia_hw.PostViewModel
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostsFragment.newInstance())
                .commit()
        }

        handleIncomingShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        val it = intent ?: return
        if (it.action != Intent.ACTION_SEND) return

        val text = it.getStringExtra(Intent.EXTRA_TEXT)
        if (text.isNullOrBlank()) {
            viewModel.showEmptyShareError()
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                NewPostFragment.newInstance(initialContent = text)
            )
            .addToBackStack(null)
            .commit()
    }
}