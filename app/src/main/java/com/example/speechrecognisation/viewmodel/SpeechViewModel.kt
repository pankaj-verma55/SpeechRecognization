package com.example.speechrecognisation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechrecognisation.data.gemini.GeminiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpeechViewModel : ViewModel() {

    private val repository =
        GeminiRepository()

    private var lastText: String? = null

    private val _translatedText =
        MutableStateFlow("")

    val translatedText =
        _translatedText.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading =
        _isLoading.asStateFlow()

    private var translationJob: Job? = null

    fun translate(
        text: String,
        targetLanguage: String
    ) {
// Cancel previous translation request
        if (text.isBlank()) return

        if (text == lastText) {
            return
        }

        lastText = text

        translationJob?.cancel()

        translationJob = viewModelScope.launch {
//        viewModelScope.launch {

            _isLoading.value = true

            repository
                .translateText(
                    text = text,
                    targetLanguage = targetLanguage
                )
                .onSuccess { result ->

                    _translatedText.value = result
                }
                .onFailure { error ->

                    _translatedText.value =
                        "Error: ${error.message}"
                }

            _isLoading.value = false
        }
    }
}