package ru.netology.nmadia_hw.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object EditPostResultContract : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent =
        Intent(context, EditPostActivity::class.java).apply {
            putExtra(EditPostActivity.EXTRA_POST_CONTENT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        if (resultCode == android.app.Activity.RESULT_OK)
            intent?.getStringExtra(EditPostActivity.EXTRA_POST_CONTENT)
        else
            null
}
