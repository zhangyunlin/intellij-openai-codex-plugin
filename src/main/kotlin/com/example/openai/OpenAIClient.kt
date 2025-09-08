package com.example.openai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.intellij.openapi.diagnostic.Logger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Duration

class OpenAIClient(
    private val apiKey: String?,
    private val model: String = System.getenv("OPENAI_MODEL") ?: DEFAULT_MODEL,
) {
    private val logger = Logger.getInstance(OpenAIClient::class.java)
    private val gson = Gson()
    private val http = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(20))
        .readTimeout(Duration.ofSeconds(120))
        .build()

    fun chat(userPrompt: String, systemPrompt: String? = DEFAULT_SYSTEM): String {
        val key = apiKey ?: System.getenv("OPENAI_API_KEY")
        require(!key.isNullOrBlank()) { "OPENAI_API_KEY is not set" }

        val endpoint = System.getenv("OPENAI_BASE_URL") ?: "https://api.openai.com/v1/chat/completions"
        val payload = ChatRequest(
            model = model,
            messages = buildList {
                if (!systemPrompt.isNullOrBlank()) add(Message("system", systemPrompt))
                add(Message("user", userPrompt))
            },
            temperature = 0.2,
            maxTokens = 800
        )
        val json = gson.toJson(payload)
        val media = "application/json; charset=utf-8".toMediaType()
        val body: RequestBody = json.toRequestBody(media)

        val req = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        http.newCall(req).execute().use { resp ->
            val respBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                logger.warn("OpenAI error ${'$'}{resp.code}: ${'$'}respBody")
                throw IllegalStateException("OpenAI HTTP ${'$'}{resp.code}: ${'$'}respBody")
            }
            val parsed = gson.fromJson(respBody, ChatResponse::class.java)
            val content = parsed.choices.firstOrNull()?.message?.content
            return content?.trim().orEmpty()
        }
    }

    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.2,
        @SerializedName("max_tokens") val maxTokens: Int = 800,
    )

    data class Message(val role: String, val content: String)

    data class ChatResponse(val choices: List<Choice>) {
        data class Choice(val index: Int, val message: Message)
        data class Message(val role: String, val content: String)
    }

    companion object {
        const val DEFAULT_MODEL = "gpt-4o-mini"
        const val DEFAULT_SYSTEM = "You are a concise Java coding assistant for IntelliJ IDEA. Prefer short, correct code snippets."
    }
}

