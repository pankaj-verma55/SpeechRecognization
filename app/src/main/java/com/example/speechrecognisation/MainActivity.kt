package com.example.speechrecognisation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.strictmode.Violation
import com.example.speechrecognisation.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
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
                        startListening()
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener true
                }
            }
        }
    }

    fun startListening() {
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

                override fun onResults(build: Bundle?) {
                    val result = build?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    binding.userText.text = result?.get(0) ?: "No result"
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