package ru.netology.nmadia_hw.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.activity.MainActivity
import ru.netology.nmadia_hw.repository.PushRepository

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "netology_pushes"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val TAG = "FCMService"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        // action из data
        val action = Action.from(data["action"])

        // данные о посте (для NEW_POST)
        val author = data["author"].orEmpty()
        val content = data["content"].orEmpty()

        // если ещё присылаешь post_id — достаём и используем для навигации
        val postIdFromData = data["post_id"]?.toLongOrNull()

        // title/body из notification-пэйлоада (fallback, если нет data-ключей)
        val titleFromNotification = message.notification?.title
        val bodyFromNotification = message.notification?.body

        Log.d(TAG, "onMessageReceived: data=$data, action=$action")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (postIdFromData != null) {
                putExtra(EXTRA_POST_ID, postIdFromData)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        when (action) {
            Action.NEW_POST -> {
                // Формат:
                // "<имя пользователя> опубликовал новый пост:"
                // "Текст поста... (на несколько строк)"
                val title = if (author.isNotBlank()) {
                    getString(R.string.new_post_title_format, author)
                } else {
                    titleFromNotification ?: getString(R.string.app_name)
                }

                // Текст поста может быть длинным: показываем BigTextStyle
                builder
                    .setContentTitle(title)
                    .setContentText(content.take(40)) // короткий превью‑текст в свернутом виде
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(content)
                    )
            }

            Action.LIKE -> {
                // Простой пример для лайка (можно доработать)
                val title = titleFromNotification ?: getString(R.string.app_name)
                val body = bodyFromNotification ?: content
                builder
                    .setContentTitle(title)
                    .setContentText(body)
            }

            null -> {
                // Неизвестное или не заданное действие: безопасная дефолтная ветка
                Log.w(TAG, "Unknown action: ${data["action"]}")
                val title = titleFromNotification ?: getString(R.string.app_name)
                val body = bodyFromNotification ?: content
                builder
                    .setContentTitle(title)
                    .setContentText(body)
            }
        }

        notificationManager.notify(1, builder.build())

        // Toast при notification-пэйлоаде в форграунде (как в лекции)
        if (message.notification?.body != null) {
            Toast.makeText(this, message.notification?.body, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewToken(token: String) {
        println("New FCM token: $token")
        PushRepository.sendPushToken(token)
    }
}

/**
 * Enum действий + безопасный парсинг для задачи Exceptions.
 */
enum class Action {
    LIKE,
    NEW_POST;

    companion object {
        fun from(value: String?): Action? =
            try {
                if (value == null) null else valueOf(value)
            } catch (e: IllegalArgumentException) {
                Log.w("Action", "Unknown action value: $value", e)
                null
            }
    }
}
