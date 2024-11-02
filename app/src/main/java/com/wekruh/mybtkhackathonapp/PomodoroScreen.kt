package com.wekruh.mybtkhackathonapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wekruh.mybtkhackathonapp.databinding.ActivityPomodoroScreenBinding

class PomodoroScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPomodoroScreenBinding
    private var studyTime: Long? = null
    private var breakTime: Long? = null
    private var timeRemaining: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var isStudySession: Boolean = true
    private var isTimerRunning: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var sessionCount: Int = 1
    private var isMuted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartTimer.setOnClickListener { btnStartTimer() }
        binding.btnPause.setOnClickListener { pauseTimer() }
        binding.btnReset.setOnClickListener { resetTimer() }
        binding.btnStart.setOnClickListener { startFun(it) }

        binding.lblSession.text = "Session Count: $sessionCount"
        mediaPlayer = MediaPlayer.create(this, R.raw.short_alarm)
    }

    private fun btnStartTimer() {
        if (initializeTimer()) {
            visibility1()
            startTimer()
        }
    }

    private fun initializeTimer(): Boolean {
        val studyTimeInput = binding.studyTimeText.text.toString()
        val breakTimeInput = binding.breakTimeText.text.toString()

        if (studyTimeInput.isBlank() || breakTimeInput.isBlank()) {
            Toast.makeText(this, "Please enter both study and break times.", Toast.LENGTH_SHORT).show()
            return false
        }

        studyTime = parseTimeInput(studyTimeInput)
        breakTime = parseTimeInput(breakTimeInput)

        timeRemaining = studyTime ?: 0
        return true
    }

    private fun parseTimeInput(timeInput: String): Long {
        val parts = timeInput.split(":")
        return if (parts.size == 2) {
            val minutes = parts[0].toLongOrNull() ?: 0L
            val seconds = parts[1].toLongOrNull() ?: 0L
            (minutes * 60 + seconds) * 1000
        } else {
            0L
        }
    }

    private fun startTimer() {
        if (isTimerRunning) return
        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimer()
                updateProgressBar()
            }

            override fun onFinish() {
                isTimerRunning = false
                if (!isMuted) {
                    mediaPlayer?.start()
                }
                if (isStudySession) {
                    sessionCounter()
                }
                toggleSession()
            }
        }.start()
        isTimerRunning = true
    }

    private fun toggleSession() {
        isStudySession = !isStudySession
        timeRemaining = if (isStudySession) studyTime ?: 0 else breakTime ?: 0
        binding.lblTimer.text = if (isStudySession) "Study Time" else "Break Time"
        if (!isMuted) {
            mediaPlayer?.start()
        }
        startTimer()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun startFun(view: View) {
        startTimer()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isStudySession = true
        timeRemaining = studyTime ?: 0
        updateTimer()
        binding.progressBar.progress = 0
    }

    private fun updateTimer() {
        val minutes = (timeRemaining / 1000) / 60
        val seconds = (timeRemaining / 1000) % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        binding.lblTimer.text = formattedTime
    }

    private fun updateProgressBar() {
        val sessionTime = if (isStudySession) studyTime ?: 0 else breakTime ?: 0

        val second = if (sessionTime > 0) {
            ((100.0 / (sessionTime / 1000))).toInt()
        } else {
            0
        }

        val progressPercentage = if (sessionTime > 0 && timeRemaining > 0) {
            (100 * (sessionTime - timeRemaining) / sessionTime).toInt() + second
        } else {
            100
        }

        binding.progressBar.progress = progressPercentage.coerceIn(0, 100)
    }

    private fun visibility1() {
        binding.lblTimer.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.linearLayout2.visibility = View.VISIBLE
        binding.btnStartTimer.visibility = View.GONE
        binding.lblBreak.visibility = View.GONE
        binding.lblStudy.visibility = View.GONE
        binding.lblSessionBreakTime.visibility = View.GONE
        binding.studyTimeText.visibility = View.GONE
        binding.breakTimeText.visibility = View.GONE
        binding.lblSession.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun goBack(view: View) {
        intent = Intent(this@PomodoroScreen, NavigateScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun sessionCounter() {
        sessionCount = sessionCount + 1
        binding.lblSession.text = "Session Count: $sessionCount"
    }

    fun mute(view: View) {
        isMuted = !isMuted
        binding.btnMute.setBackgroundResource(if (isMuted) R.drawable.volume_on else R.drawable.volume_off) // Change button icon
    }
}