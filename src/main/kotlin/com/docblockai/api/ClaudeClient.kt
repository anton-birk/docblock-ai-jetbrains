package com.docblockai.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object ClaudeClient {

    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()
    private val gson = Gson()

    fun generate(apiKey: String, model: String, prompt: String): String? {
        if (apiKey.isBlank()) throw IllegalStateException(
            "Anthropic API key is not configured. Go to Settings → Tools → DocBlock AI."
        )

        val params = mutableMapOf<String, Any>(
            "model" to model,
            // Newer models always think, and thinking tokens count against max_tokens.
            "max_tokens" to 16000,
            "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
        )
        // Doc comments are a simple task; low effort keeps them fast and cheap. Haiku 4.5 rejects effort.
        if (!model.startsWith("claude-haiku")) {
            params["output_config"] = mapOf("effort" to "low")
        }
        // Retry classifier refusals on Anthropic's recommended fallback model.
        val useFallbacks = model in FALLBACK_MODELS
        if (useFallbacks) {
            params["fallbacks"] = "default"
        }

        val builder = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
        if (useFallbacks) {
            builder.header("anthropic-beta", "server-side-fallback-2026-07-01")
        }
        val body = gson.toJson(params)

        val request = builder
            .timeout(Duration.ofSeconds(90))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            val msg = try {
                gson.fromJson(response.body(), JsonObject::class.java)
                    .getAsJsonObject("error")?.get("message")?.asString
            } catch (_: Exception) { null } ?: response.body()
            throw RuntimeException("Anthropic error (${response.statusCode()}): $msg")
        }

        val json = gson.fromJson(response.body(), JsonObject::class.java)
        if (json.get("stop_reason")?.asString == "refusal") {
            throw RuntimeException("Claude declined to document this code. Try a different model.")
        }

        // Thinking blocks may precede the answer, so pick the first text block rather than content[0].
        return json.getAsJsonArray("content")
            ?.map { it.asJsonObject }
            ?.firstOrNull { it.get("type")?.asString == "text" }
            ?.get("text")?.asString
            ?.trim()
    }

    private val FALLBACK_MODELS = setOf("claude-sonnet-5-5", "claude-opus-5-5", "claude-fable-5-1")
}