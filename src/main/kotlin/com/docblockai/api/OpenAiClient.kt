package com.docblockai.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object OpenAiClient {

    private val http: HttpClient = HttpClient.newHttpClient()
    private val gson = Gson()

    fun generate(apiKey: String, model: String, prompt: String): String? {
        if (apiKey.isBlank()) throw IllegalStateException(
            "OpenAI API key is not configured. Go to Settings → Tools → DocBlock AI."
        )

        val body = gson.toJson(
            mapOf(
                "model" to model,
                "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
            )
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            val msg = try {
                gson.fromJson(response.body(), JsonObject::class.java)
                    .getAsJsonObject("error")?.get("message")?.asString
            } catch (_: Exception) { null } ?: response.body()
            throw RuntimeException("OpenAI error (${response.statusCode()}): $msg")
        }

        return gson.fromJson(response.body(), JsonObject::class.java)
            .getAsJsonArray("choices")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString
            ?.trim()
    }
}