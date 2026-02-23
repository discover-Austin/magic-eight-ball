package com.example.magiceightball

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiceightball.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var eightBall: EightBall
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eightBall = EightBall()

        setupSensor()
        setupInput()
        setupHistory()

        // Show hint text on launch
        binding.answerText.text = getString(R.string.hint_ask)
        binding.answerText.alpha = 0.6f
    }

    private fun setupSensor() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(sensorManager) {
            runOnUiThread {
                val response = eightBall.shake()
                revealAnswer(response)
                addHistoryEntry("", response)
            }
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            binding.shakeHint.text = getString(R.string.hint_no_shake)
        }
    }

    private fun setupInput() {
        binding.askButton.setOnClickListener {
            dismissKeyboard()
            askQuestion()
        }

        binding.questionInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dismissKeyboard()
                askQuestion()
                true
            } else {
                false
            }
        }

        binding.ballContainer.setOnClickListener {
            askQuestion()
        }
    }

    private fun setupHistory() {
        historyAdapter = HistoryAdapter { entry -> shareResponse(entry) }
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }

        binding.clearHistoryButton.setOnClickListener {
            historyAdapter.clear()
            binding.clearHistoryButton.visibility = View.GONE
        }
    }

    private fun askQuestion() {
        val question = binding.questionInput.text?.toString().orEmpty().trim()
        val response = eightBall.ask(question)
        revealAnswer(response)
        addHistoryEntry(question, response)
        binding.questionInput.text?.clear()
    }

    private fun addHistoryEntry(question: String, response: EightBall.Response) {
        historyAdapter.addEntry(HistoryEntry(question, response))
        binding.historyList.scrollToPosition(0)
        binding.clearHistoryButton.visibility = View.VISIBLE
    }

    private fun shareResponse(entry: HistoryEntry) {
        val shareText = if (entry.question.isNotBlank()) {
            "I asked the Magic 8-Ball: \"${entry.question}\"\nAnswer: ${entry.response.text}"
        } else {
            "I shook the Magic 8-Ball and got: ${entry.response.text}"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }

    private fun revealAnswer(response: EightBall.Response) {
        val answerText = binding.answerText

        // Haptic feedback on answer reveal
        binding.ballContainer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        // Colour-code by sentiment
        val colorRes = when (response.sentiment) {
            EightBall.Sentiment.POSITIVE -> R.color.response_positive
            EightBall.Sentiment.NEUTRAL  -> R.color.response_neutral
            EightBall.Sentiment.NEGATIVE -> R.color.response_negative
        }
        answerText.setTextColor(getColor(colorRes))
        answerText.alpha = 1f

        // Flip animation: scale down then up while swapping text
        val scaleDownX = ObjectAnimator.ofFloat(answerText, View.SCALE_X, 1f, 0f)
        val scaleDownY = ObjectAnimator.ofFloat(answerText, View.SCALE_Y, 1f, 0f)
        val scaleUpX   = ObjectAnimator.ofFloat(answerText, View.SCALE_X, 0f, 1f)
        val scaleUpY   = ObjectAnimator.ofFloat(answerText, View.SCALE_Y, 0f, 1f)

        scaleDownX.duration = 150
        scaleDownY.duration = 150
        scaleUpX.duration   = 150
        scaleUpY.duration   = 150

        val down = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            interpolator = AccelerateDecelerateInterpolator()
        }
        val up = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            interpolator = AccelerateDecelerateInterpolator()
        }

        down.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                answerText.text = response.text
                up.start()
            }
        })

        down.start()

        // Pulse the ball
        val pulseX = ObjectAnimator.ofFloat(binding.ballContainer, View.SCALE_X, 1f, 0.95f, 1f)
        val pulseY = ObjectAnimator.ofFloat(binding.ballContainer, View.SCALE_Y, 1f, 0.95f, 1f)
        val pulse = AnimatorSet().apply {
            playTogether(pulseX, pulseY)
            duration = 300
        }
        pulse.start()
    }

    private fun dismissKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
