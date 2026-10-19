package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Message exchange in a multi-turn conversation.
 */
data class ChatTurn(
    val role: String, // "user" or "model"
    val text: String
)

/**
 * Gemini model selection enum based on task complexity.
 */
enum class GeminiModel(val modelId: String, val displayName: String, val description: String) {
    COMPLEX_ARGON("gemini-4-argon", "Gemini 4 Argon", "Deep reasoning, complex algorithms & architecture"),
    GENERAL_FLASH("gemini-3.8-flash", "Gemini 3.8 Flash", "General BCA topics & syllabus explanations"),
    FAST_LITE("gemini-3.1-flash-lite", "Gemini 3.1 Flash Lite", "Ultra-fast Q&A & rapid definitions")
}

/**
 * Production-ready Gemini multi-turn chat service.
 *
 * Implements:
 * - Multi-turn conversation history maintenance
 * - Role-based system instruction
 * - Dynamic model selection (gemini-4-argon, gemini-3.8-flash, gemini-3.1-flash-lite)
 * - Safe fallback handling when offline or when API key is unconfigured
 */
class GeminiChatService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    private val systemInstructionText = """
        You are the official Academic AI Teaching Assistant for the Department of BCA (Bachelor of Computer Applications) under the MAKAUT CBCS syllabus, assisting students and faculty.
        Your expertise includes:
        - Data Structures & Algorithms (Trees, Graphs, Sorting, Dynamic Programming)
        - Database Management Systems (SQL, ER Models, Normalization: 1NF to BCNF, ACID transactions)
        - Computer Architecture & Operating Systems (Process Scheduling, Memory Management, Paging)
        - Object-Oriented Programming (Java, C++, Python)
        - Web Development & Software Engineering.

        Guidelines:
        1. Provide accurate, well-structured educational explanations with syntax-highlighted code examples.
        2. Format responses with clear Markdown headings and bullet points.
        3. Be encouraging, supportive, and academically rigorous.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatTurn>,
        userMessage: String,
        model: GeminiModel = GeminiModel.GENERAL_FLASH
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is unconfigured or placeholder. Providing academic offline response.")
            return@withContext Result.success(getSmartLocalResponse(userMessage))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.modelId}:generateContent?key=$apiKey"

            // Construct Multi-turn JSON Payload
            val rootJson = JSONObject()

            // System Instruction
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            }
            rootJson.put("systemInstruction", systemInstructionJson)

            // Contents array (Conversation History + Current Message)
            val contentsArray = JSONArray()

            // Previous turns
            for (turn in history) {
                val turnJson = JSONObject().apply {
                    put("role", turn.role)
                    put("parts", JSONArray().put(JSONObject().put("text", turn.text)))
                }
                contentsArray.put(turnJson)
            }

            // Current user turn
            val currentUserJson = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            }
            contentsArray.put(currentUserJson)

            rootJson.put("contents", contentsArray)

            // Generation config
            val configJson = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            }
            rootJson.put("generationConfig", configJson)

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.w(TAG, "Gemini API request failed with code ${response.code}: $responseBody")
                return@withContext Result.success(getSmartLocalResponse(userMessage))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }

            Result.success(getSmartLocalResponse(userMessage))
        } catch (e: Exception) {
            Log.w(TAG, "Network error during Gemini API call", e)
            Result.success(getSmartLocalResponse(userMessage))
        }
    }

    private fun getSmartLocalResponse(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("bcnf") || lower.contains("normal") -> """
                ### Boyce-Codd Normal Form (BCNF)
                A relation schema R is in **BCNF** with respect to a set F of functional dependencies if, for all functional dependencies of the form X -> Y:
                1. X -> Y is a trivial functional dependency (Y is subset of X).
                2. X is a **superkey** for schema R.

                **Key takeaway for MAKAUT exams:**
                BCNF is strictly stronger than 3NF. While every BCNF schema is in 3NF and eliminates all redundancy based on FDs, decomposing into BCNF may not always preserve all functional dependencies.
            """.trimIndent()

            lower.contains("sql") || lower.contains("join") || lower.contains("dbms") -> """
                ### SQL Joins & MAKAUT Syllabus Summary
                - **INNER JOIN**: Returns rows with matching values in both tables.
                - **LEFT OUTER JOIN**: Returns all rows from the left table and matched rows from the right table.
                - **GROUP BY with HAVING**: Use `HAVING` to filter aggregated results, while `WHERE` filters rows prior to aggregation.

                ```sql
                SELECT s.name, COUNT(a.id) AS total_present
                FROM students s
                INNER JOIN attendance a ON s.id = a.student_id
                WHERE a.status = 'PRESENT'
                GROUP BY s.name
                HAVING COUNT(a.id) >= 15;
                ```
            """.trimIndent()

            lower.contains("tree") || lower.contains("graph") || lower.contains("dsa") || lower.contains("algorithm") -> """
                ### Binary Search Tree (BST) & Traversal
                In a Binary Search Tree:
                - Left subtree nodes < Root node
                - Right subtree nodes > Root node
                - **Inorder Traversal** (Left -> Root -> Right) always visits BST nodes in **strictly ascending sorted order**.

                Time complexity for search/insertion:
                - Average case: O(log n)
                - Worst case (skewed tree): O(n)
            """.trimIndent()

            else -> """
                ### Prof. Sengupta's BCA Academic Assistant
                Regarding your question: "$query"

                In the MAKAUT BCA curriculum, this relates to core Computer Applications fundamentals. 
                - Ensure you review the theoretical proofs in your semester syllabus lecture notes.
                - Practice the laboratory implementation in Python/C++ for practical sessions on Saturdays.
                - Feel free to post further follow-up questions or attach a code snippet for step-by-step debugging!
            """.trimIndent()
        }
    }

    companion object {
        private const val TAG = "GeminiChatService"
    }
}
