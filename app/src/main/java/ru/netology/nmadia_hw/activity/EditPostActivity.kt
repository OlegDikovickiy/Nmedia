package ru.netology.nmadia_hw.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmadia_hw.databinding.ActivityEditPostBinding
import ru.netology.nmadia_hw.util.AndroidUtils

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding

    companion object {
        const val EXTRA_POST_CONTENT = "ru.netology.nmadia_hw.edit_post_content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val existingContent = intent?.getStringExtra(EXTRA_POST_CONTENT) ?: ""
        binding.edit.setText(existingContent)

        binding.ok.setOnClickListener {
            val editedContent = binding.edit.text.toString()
            if (editedContent.isBlank()) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                val intent = Intent().apply {
                    putExtra(EXTRA_POST_CONTENT, editedContent)
                }
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
    }
}