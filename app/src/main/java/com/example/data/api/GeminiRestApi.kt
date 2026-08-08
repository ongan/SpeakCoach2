package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body requestMap: Map<String, Any>
    ): Map<String, Any>
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateResponse(
        systemInstruction: String,
        userPrompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Return empty to trigger smart local coach engine fallback
            return@withContext ""
        }

        try {
            val contentsList = mutableListOf<Map<String, Any>>()

            // Add conversation history
            for ((speaker, text) in conversationHistory) {
                val role = if (speaker == "USER") "user" else "model"
                contentsList.add(
                    mapOf(
                        "role" to role,
                        "parts" to listOf(mapOf("text" to text))
                    )
                )
            }

            // Add current user prompt
            contentsList.add(
                mapOf(
                    "role" to "user",
                    "parts" to listOf(mapOf("text" to userPrompt))
                )
            )

            val requestMap = mutableMapOf<String, Any>(
                "contents" to contentsList,
                "systemInstruction" to mapOf(
                    "parts" to listOf(mapOf("text" to systemInstruction))
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.7,
                    "topP" to 0.95,
                    "responseMimeType" to "application/json"
                )
            )

            val response = apiService.generateContent(apiKey, requestMap)
            val candidates = response["candidates"] as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            val text = firstPart?.get("text") as? String

            text ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
