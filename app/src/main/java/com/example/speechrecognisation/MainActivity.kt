package com.example.speechrecognisation

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.strictmode.Violation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.speechrecognisation.databinding.ActivityMainBinding
import com.example.speechrecognisation.viewmodel.SpeechViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var textToSpeech: TextToSpeech

    //    private lateinit var viewModel: SpeechViewModel
    private lateinit var viewModel: SpeechViewModel
    var selectedLanguage = "Hindi"
    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }
    val languages = listOf(
        "English",
        "Hindi",
        "Tamil",
        "Telugu",
        "Kannada"
    )
    private fun getLocale(language: String): Locale {

        return when (language) {

            "English" -> Locale.US

            "Hindi" -> Locale("hi", "IN")

            "Tamil" -> Locale("ta", "IN")

            "Telugu" -> Locale("te", "IN")

            "Bengali" -> Locale("bn", "IN")

            "Marathi" -> Locale("mr", "IN")

            "Gujarati" -> Locale("gu", "IN")

            "Kannada" -> Locale("kn", "IN")

            "Malayalam" -> Locale("ml", "IN")

            "Punjabi" -> Locale("pa", "IN")
            else -> {
                    Locale.US
            }
        }
    }

    private val permissionAllowed =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = SpeechViewModel()
        textToSpeech = TextToSpeech(this) { status ->

            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
                val voices = textToSpeech.voices.find {
                    it.name == "en-in-x-end-local"
                }
                if (voices != null) {
                    textToSpeech.voice = voices
                }
//                check how many language is available for speek in gemini
//                voices.forEach {
//                    Log.d("TTS_VOICE", "Voice: ${it.name}")
//                }
            }
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.simple_dropdown_item_1line,
            languages
        )
        binding.userSpeak.setOnClickListener {

            textToSpeech.language = Locale.US

            textToSpeech.speak(
                binding.userText.text.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "original_text"
            )
        }
        binding.aiSpeak.setOnClickListener {

            val locale = getLocale(selectedLanguage)
            val result = textToSpeech.setLanguage(locale)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(
                    this,
                    "This language is not supported",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            textToSpeech.speak(
                binding.translatedText.text.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "translated_text"
            )
        }

        binding.languageAutoComplete.setAdapter(adapter)

        binding.languageAutoComplete.setOnItemClickListener { _, _, position, _ ->

            selectedLanguage = languages[position]

            Toast.makeText(
                this,
                selectedLanguage,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.floatingBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    binding.micAnimation.visibility = View.GONE
                    binding.floatingBtn.visibility = View.VISIBLE
                    binding.micAnimation.stopAnimation()
                    speechRecognizer.stopListening()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_DOWN -> {
                    permissionGranted(this) {
                        binding.micAnimation.startAnimation()
                        binding.micAnimation.visibility = View.VISIBLE
                        binding.floatingBtn.visibility = View.GONE
                        startListening(selectedLanguage)
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener true
                }
            }
        }
        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.translatedText.collect { translatedText ->

                    binding.translatedText.text = translatedText
                }
            }
        }
    }

    fun startListening(selectedLanguage: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onBeginningOfSpeech() {
                    binding.userText.text = "Listening..."
                    binding.micAnimation.startAnimation()
                }

                override fun onBufferReceived(p0: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(p0: Int) {}

                override fun onEvent(p0: Int, p1: Bundle?) {}

                override fun onPartialResults(p0: Bundle?) {}

                override fun onReadyForSpeech(p0: Bundle?) {}

                override fun onResults(results: Bundle?) {

                    val text = results
                        ?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        ?.firstOrNull()

                    if (!text.isNullOrBlank()) {

                        binding.userText.text = text

                        // Send recognized text to Gemini
                        viewModel.translate(
                            text = text,
                            targetLanguage = selectedLanguage
//                            targetLanguage = "Hindi"
                        )
                    } else {
                        binding.userText.text = "No result"
                    }
                }

                override fun onRmsChanged(p0: Float) {
                }

            })
        speechRecognizer.startListening(intent)
    }

    fun permissionGranted(context: Context, call: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            call.invoke()
        } else {
            permissionAllowed.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }
}