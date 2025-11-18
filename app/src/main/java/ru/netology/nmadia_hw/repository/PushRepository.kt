package ru.netology.nmadia_hw.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object PushRepository {
    private val client = OkHttpClient()

    // сюда поставь адрес учебного/фейкового backend'а, если есть
    private const val BACKEND_URL = "https://example.com/api/push-tokens"

    fun sendPushToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("token", token)
                }
                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(BACKEND_URL)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    println("Push token sent, code=${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
