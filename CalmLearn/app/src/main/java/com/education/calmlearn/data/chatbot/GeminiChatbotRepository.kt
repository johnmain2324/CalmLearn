package com.education.calmlearn.data.chatbot

import android.util.Log
import com.education.calmlearn.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class GeminiChatbotRepository(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) : ChatbotRepository {

    override suspend fun sendMessage(history: List<ChatMessage>, userMessage: String): ChatbotResult {
        if (apiKey.isBlank()) return ChatbotResult.Error(ChatbotErrorReason.SERVICE_NOT_CONFIGURED)

        val body = buildRequestBody(history, userMessage)
        return try {
            withContext(Dispatchers.IO) { post(body) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: IOException) {
            ChatbotResult.Error(ChatbotErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ChatbotResult.Error(ChatbotErrorReason.UNKNOWN)
        }
    }

    private fun buildRequestBody(history: List<ChatMessage>, userMessage: String): String {
        val contents = JSONArray()
        history.takeLast(MAX_HISTORY_MESSAGES).forEach { contents.put(content(it.role, it.text)) }
        contents.put(content(ChatMessage.ROLE_USER, userMessage))

        return JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_INSTRUCTION))))
            .put("contents", contents)
            .toString()
    }

    private fun content(role: String, text: String): JSONObject =
        JSONObject().put("role", role).put("parts", JSONArray().put(JSONObject().put("text", text)))

    private fun post(body: String): ChatbotResult {
        val connection = URL(ENDPOINT).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-goog-api-key", apiKey)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return if (code in 200..299) parseReply(text) else mapHttpError(code, text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseReply(responseText: String): ChatbotResult {
        val root = JSONObject(responseText)
        if (root.optJSONObject("promptFeedback")?.has("blockReason") == true) {
            return ChatbotResult.Error(ChatbotErrorReason.EMPTY_REPLY)
        }

        val parts = root.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?: return ChatbotResult.Error(ChatbotErrorReason.EMPTY_REPLY)

        val reply = buildString {
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                if (!part.optBoolean("thought", false)) append(part.optString("text", ""))
            }
        }.trim()

        return if (reply.isEmpty()) {
            ChatbotResult.Error(ChatbotErrorReason.EMPTY_REPLY)
        } else {
            ChatbotResult.Success(reply)
        }
    }

    private fun mapHttpError(code: Int, responseText: String): ChatbotResult {
        val message = runCatching { JSONObject(responseText).optJSONObject("error")?.optString("message") }.getOrNull()
        Log.w(TAG, "Gemini HTTP $code: $message")

        val reason = when {
            code == 401 || code == 403 -> ChatbotErrorReason.API_KEY_REJECTED
            code == 400 && message?.contains("API key", ignoreCase = true) == true -> ChatbotErrorReason.API_KEY_REJECTED
            code == 429 -> ChatbotErrorReason.RATE_LIMITED
            code in 500..599 -> ChatbotErrorReason.SERVER_ERROR
            else -> ChatbotErrorReason.UNKNOWN
        }
        return ChatbotResult.Error(reason)
    }

    private companion object {
        const val TAG = "GeminiChatbot"
        const val MODEL = "gemini-3.5-flash-lite"
        const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 60_000
        const val MAX_HISTORY_MESSAGES = 20

        const val SYSTEM_INSTRUCTION =
            "Bạn là gia sư tiếng Anh thân thiện, trò chuyện bằng tiếng Anh ở trình độ sơ-trung cấp với người học. " +
                "Luôn trả lời ngắn gọn (2-4 câu), tự nhiên như hội thoại đời thường, và nếu người dùng viết sai " +
                "ngữ pháp rõ ràng, nhẹ nhàng chỉ ra cách viết đúng trước khi tiếp tục hội thoại."
    }
}
