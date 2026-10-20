package com.example.ai

import com.example.BuildConfig
import com.example.data.model.Habit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @property:Json(name = "contents") val contents: List<GeminiContent>,
    @property:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @property:Json(name = "parts") val parts: List<GeminiPart>,
    @property:Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @property:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @property:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @property:Json(name = "content") val content: GeminiContent? = null
)

object GeminiCoachClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getCoachResponse(
        userMessage: String,
        activeHabits: List<Habit>,
        completionRate: Int,
        coachingTone: String,
        conversationHistory: List<Pair<String, String>> // role ("user"/"model") to content
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // Habit context summary
        val habitsSummary = if (activeHabits.isEmpty()) {
            "No active habits currently created."
        } else {
            activeHabits.joinToString(separator = "; ") {
                "${it.name} (${it.category}, scheduled: ${it.frequencyType}, target: ${it.targetValue} ${it.targetUnit})"
            }
        }

        val systemPrompt = """
            You are HabitPilot Coach, a friendly, compassionate, non-judgmental wellness and habit formation coach.
            HabitPilot's core motto is: "Build Better Habits. Without Burnout."
            You help busy professionals aged 22-35 maintain sustainable routines.
            Coaching Tone: $coachingTone
            User's active habits: $habitsSummary
            Current 7-day completion rate: $completionRate%
            
            Guiding Principles:
            1. Keep your replies concise (2-4 short paragraphs maximum), actionable, and kind.
            2. Suggest small micro-steps (e.g. 2-5 minutes) rather than overwhelming drastic changes.
            3. Never guilt or shame users for missing days. Rebuilding momentum starts with celebrating showing up.
            4. Do not provide medical diagnosis or psychiatric advice.
            5. When relevant, reference their habits gently.
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // High quality local coaching fallback engine
            return@withContext generateLocalCoachAdvice(userMessage, activeHabits, completionRate, coachingTone)
        }

        try {
            val contentList = mutableListOf<GeminiContent>()
            conversationHistory.takeLast(6).forEach { (role, text) ->
                contentList.add(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = text)),
                        role = if (role == "user") "user" else "model"
                    )
                )
            }
            contentList.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userMessage)),
                    role = "user"
                )
            )

            val requestObj = GeminiRequest(
                contents = contentList,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            )

            val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
            val jsonBody = jsonAdapter.toJson(requestObj)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext generateLocalCoachAdvice(userMessage, activeHabits, completionRate, coachingTone)
            }

            val responseBody = response.body?.string() ?: ""
            val responseAdapter = moshi.adapter(GeminiResponse::class.java)
            val geminiResponse = responseAdapter.fromJson(responseBody)
            val replyText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            replyText ?: generateLocalCoachAdvice(userMessage, activeHabits, completionRate, coachingTone)
        } catch (e: Exception) {
            generateLocalCoachAdvice(userMessage, activeHabits, completionRate, coachingTone)
        }
    }

    private fun generateLocalCoachAdvice(
        prompt: String,
        habits: List<Habit>,
        completionRate: Int,
        tone: String
    ): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("struggle") || lower.contains("evening") || lower.contains("night") || lower.contains("tired") -> {
                "Evening fatigue is completely normal after a long workday. When willpower is low, habits that require high focus often get skipped.\n\n" +
                "Try shrinking the commitment: if your habit is reading 20 minutes, reduce it to just 2 pages or 3 minutes. Alternatively, shift it to right after lunch or your afternoon coffee break. Small steps build consistency without burnout."
            }
            lower.contains("missed") || lower.contains("restart") || lower.contains("guilt") || lower.contains("streak") -> {
                "Missing a few days doesn't erase the neural pathways you've already built! Life happens, and flexibility is part of a sustainable routine.\n\n" +
                "Instead of trying to 'make up' for lost days with a double session, pick just ONE habit to do for 5 minutes today. Once you check that box, you've already restarted your momentum."
            }
            lower.contains("morning") || lower.contains("routine") -> {
                "The secret to a great morning routine is habit stacking—anchoring a new habit to something you already do without thinking, like brewing coffee or brushing your teeth.\n\n" +
                "Keep your morning stack under 15 minutes total. For example: Pour coffee -> 5 min stretch -> Review top 3 priorities for today. Would you like to set up one of these as a gentle reminder?"
            }
            lower.contains("busy") || lower.contains("overwhelm") || lower.contains("time") -> {
                "When your calendar is packed, protect your energy by entering 'Maintenance Mode'. Pick your anchor habit (the single one that gives you the most peace or energy) and allow the others to pause.\n\n" +
                "Doing 2 minutes of your anchor habit keeps the identity intact while preventing burnout."
            }
            else -> {
                val habitName = habits.firstOrNull()?.name ?: "your daily habits"
                "I hear you. Sustainable progress isn't about perfection; it's about consistency without burnout. With $habitName, focus on making the starting step so easy that you can do it even on your lowest energy day.\n\n" +
                "How are your energy levels feeling today, and what is one small win you'd like to target?"
            }
        }
    }
}
