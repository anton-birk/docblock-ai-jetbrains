package com.docblockai.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object ClaudeClient {

    private val http: HttpClient = HttpClient.newHttpClient()
    private val gson = Gson()

    fun generate(apiKey: String, model: String, prompt: String): String? {
        if (apiKey.isBlank()) throw IllegalStateException(
            "Anthropic API key is not configured. Go to Settings → Tools → DocBlock AI."
        )

        val body = gson.toJson(
            mapOf(
                "model" to model,
                "max_tokens" to 1024,
                "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
            )
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
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

        return gson.fromJson(response.body(), JsonObject::class.java)
            .getAsJsonArray("content")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString
            ?.trim()
    }
}