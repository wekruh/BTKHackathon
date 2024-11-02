package com.wekruh.mybtkhackathonapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wekruh.mybtkhackathonapp.databinding.ActivityNavigateScreenBinding

class NavigateScreen : AppCompatActivity() {
    private lateinit var binding: ActivityNavigateScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigate_screen)
        binding = ActivityNavigateScreenBinding.inflate(layoutInflater)
    }

    fun schedule(view : View){
        intent = Intent(this@NavigateScreen, ScheduleScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun lessonCoach(view : View){
        intent = Intent(this@NavigateScreen, LessonCoachScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun pomodoro(view : View){
        intent = Intent(this@NavigateScreen, PomodoroScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun questionSolver(view : View){
        intent = Intent(this@NavigateScreen, QuestionSolverScreen::class.java)
        startActivity(intent)
        finish()
    }

}