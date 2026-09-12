package com.example.speechrecognisation.data.gemini

import android.util.Log
import com.example.speechrecognisation.BuildConfig
import retrofit2.HttpException

class GeminiRepository {

    suspend fun translateText(
        text: String,
        targetLanguage: String
    ): Result<String> {

        return try {

            val prompt = """
                Translate the following text into $targetLanguage.
                
                Return only the translated text.
                Do not add explanations.
                
                Text:
                $text
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = prompt
                            )
                        )
                    )
                )
            )

            val response =
                GeminiRetrofit.api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

            val translatedText =
                response
                    .candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

            if (!translatedText.isNullOrBlank()) {

                Result.success(
                    translatedText.trim()
                )

            } else {

                Result.failure(
                    Exception("Empty response from Gemini")
                )
            }

        } catch (e: HttpException) {
            Log.e("GeminiAPI", "HTTP CODE: ${e.code()}")
            Log.e("GeminiAPI", "ERROR BODY: ${e.response()?.errorBody()?.string()}")
            if (e.code() == 429) {

                Result.failure(
                    Exception(
                        "Gemini rate limit reached. Please try again later."
                    )
                )

            } else {

                Result.failure(
                    Exception(
                        "Gemini API error: ${e.code()}"
                    )
                )
            }

        }catch (e: Exception) {

            Result.failure(e)
        }
    }
}