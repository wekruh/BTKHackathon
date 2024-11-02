package com.wekruh.mybtkhackathonapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import com.wekruh.mybtkhackathonapp.databinding.ActivityScheduleScreenBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ScheduleScreen : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeTimeFilter()
        setupCheckboxListeners()
        updateFreeTimeVisibility()
    }

    fun makeTimeFilter() {
        val timeInputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val currentInput = StringBuilder(dest).replace(dstart, dend, source.subSequence(start, end).toString()).toString()

            if (currentInput.matches(Regex("^([01]?[0-9]|2[0-3])[:]?([0-5]?[0-9])?\$"))) {
                null
            } else if (currentInput.length == 2 && !currentInput.contains(":")) {
                return@InputFilter currentInput + ":"
            } else if (currentInput.length == 5 && currentInput[2] == ':') {
                null
            } else {
                ""
            }
        }

        binding.sessionText.filters = arrayOf(timeInputFilter)
        binding.breakText.filters = arrayOf(timeInputFilter)
        binding.startFreeTimeText.filters = arrayOf(timeInputFilter)
        binding.endFreeTimeText.filters = arrayOf(timeInputFilter)
    }

    fun generateSchedule(view: View) {
        if (validateInputs()) {
            binding.tableLayout.removeAllViews()
            generateText()
            visibility()
            binding.btnBack5.visibility = View.VISIBLE
            binding.btnBack2.visibility = View.GONE
        } else {
            Toast.makeText(this, "Please fill in all required fields correctly.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        return binding.coursesText.text.isNotEmpty() &&
                binding.sessionText.text.isNotEmpty() &&
                binding.breakText.text.isNotEmpty() &&
                binding.startFreeTimeText.text.isNotEmpty() &&
                binding.endFreeTimeText.text.isNotEmpty() &&
                getSelectedDays().isNotEmpty()
    }

    private fun getSelectedDays(): String {
        val selectedDays = mutableListOf<String>()

        if (binding.checkMonday.isChecked) selectedDays.add("Monday")
        if (binding.checkTuesday.isChecked) selectedDays.add("Tuesday")
        if (binding.checkWednesday.isChecked) selectedDays.add("Wednesday")
        if (binding.checkThursday.isChecked) selectedDays.add("Thursday")
        if (binding.checkFriday.isChecked) selectedDays.add("Friday")
        if (binding.checkSaturday.isChecked) selectedDays.add("Saturday")
        if (binding.checkSunday.isChecked) selectedDays.add("Sunday")

        return selectedDays.joinToString(", ")
    }

    private fun generateText() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getApiKey()
        )

        val courses = binding.coursesText.text.toString()
        val session = binding.sessionText.text.toString()
        val breaktime = binding.breakText.text.toString()
        val extra = binding.extraNotesText.text.toString()

        val startEndTimes = mutableListOf<String>()

        if (binding.checkMonday.isChecked) {
            val mondayTime = if (binding.mondayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.mondayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.mondayStartFreeTimeEditText.text} - ${binding.mondayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Monday: $mondayTime")
        }
        if (binding.checkTuesday.isChecked) {
            val tuesdayTime = if (binding.tuesdayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.tuesdayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.tuesdayStartFreeTimeEditText.text} - ${binding.tuesdayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Tuesday: $tuesdayTime")
        }
        if (binding.checkWednesday.isChecked) {
            val wednesdayTime = if (binding.wednesdayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.wednesdayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.wednesdayStartFreeTimeEditText.text} - ${binding.wednesdayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Wednesday: $wednesdayTime")
        }
        if (binding.checkThursday.isChecked) {
            val thursdayTime = if (binding.thursdayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.thursdayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.thursdayStartFreeTimeEditText.text} - ${binding.thursdayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Thursday: $thursdayTime")
        }
        if (binding.checkFriday.isChecked) {
            val fridayTime = if (binding.fridayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.fridayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.fridayStartFreeTimeEditText.text} - ${binding.fridayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Friday: $fridayTime")
        }
        if (binding.checkSaturday.isChecked) {
            val saturdayTime = if (binding.saturdayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.saturdayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.saturdayStartFreeTimeEditText.text} - ${binding.saturdayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Saturday: $saturdayTime")
        }
        if (binding.checkSunday.isChecked) {
            val sundayTime = if (binding.sundayStartFreeTimeEditText.text.isNullOrBlank() ||
                binding.sundayEndFreeTimeEditText.text.isNullOrBlank()
            ) {
                "${binding.startFreeTimeText.text} - ${binding.endFreeTimeText.text}"
            } else {
                "${binding.sundayStartFreeTimeEditText.text} - ${binding.sundayEndFreeTimeEditText.text}"
            }
            startEndTimes.add("Sunday: $sundayTime")
        }

        val availableHours = startEndTimes.joinToString("; ")

        val prompt = """
Generate a detailed weekly course schedule based on the following information:

1. **Courses**: $courses.
2. **Days of the Week with Available Hours**: $availableHours.
3. **Session Duration**: Each session lasts for $session hours.
4. **Break Duration**: A break of $breaktime hours follows each session.
5. **Extra Notes**: Include these notes in the schedule: $extra.

The schedule should:
- Ensure at least one time slot for each selected day.
- Present each day starting with the day's name, followed by the time slots listed on new lines.
- Use the format `time | subject` for each entry.
- Avoid any text not related to the schedule.
- If there's not enough information to fill the schedule, you can tell the user to provide more details and try again.

Example format:
Monday:
9:00 AM | Maths
10:00 AM | Break
10:30 AM | Physics
11:30 AM | Break
12:00 PM | Biology
"""

        MainScope().launch {
            val response = generativeModel.generateContent(prompt)
            runOnUiThread {
                adaptTableFromText(response.text!!)
            }
            print(response.text)
        }
    }

    private fun getApiKey(): String {
        return "AIzaSyBc8Waxob_Muu2vCeUr-ZCF0mA-ZHTmYrU"
    }

    private fun adaptTableFromText(response: String) {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        val lines = response.split("\n")

        var currentDay = ""
        var rowIndex = 0

        for (line in lines) {
            if (line.endsWith(":")) {
                currentDay = line.trimEnd(':')
                val tableRowDay = TableRow(this)
                val dayTextView = TextView(this).apply {
                    text = currentDay
                    textSize = 18f
                    setPadding(16, 16, 16, 16)
                    layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.cell_border)
                    setTextColor(Color.parseColor("#FFFFFF"))
                }
                tableRowDay.addView(dayTextView)
                tableLayout.addView(tableRowDay)
            } else if (line.contains("|")) {
                val parts = line.split("|")
                if (parts.size == 2) {
                    val time = parts[0].trim()
                    val subject = parts[1].trim()

                    val tableRow = TableRow(this)

                    val backgroundColor = if (rowIndex % 2 == 0) {
                        R.color.excel_even_row
                    } else {
                        R.color.excel_odd_row
                    }

                    val timeTextView = TextView(this).apply {
                        text = time
                        textSize = 16f
                        setPadding(16, 16, 16, 16)
                        layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        gravity = Gravity.CENTER
                        setBackgroundResource(R.drawable.cell_border)
                        setBackgroundColor(resources.getColor(backgroundColor))
                        setTextColor(Color.parseColor("#000000"))
                    }
                    tableRow.addView(timeTextView)

                    val subjectTextView = TextView(this).apply {
                        text = subject
                        textSize = 16f
                        setPadding(16, 16, 16, 16)
                        layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        gravity = Gravity.CENTER
                        setBackgroundResource(R.drawable.cell_border)
                        setBackgroundColor(resources.getColor(backgroundColor))
                        setTextColor(Color.parseColor("#000000"))
                    }
                    tableRow.addView(subjectTextView)

                    tableLayout.addView(tableRow)

                    rowIndex++
                }
            }
        }
    }

    fun visibility() {
        binding.btnCreateSchedule.visibility = View.GONE
        binding.coursesText.visibility = View.GONE
        binding.lblComma.visibility = View.GONE
        binding.endFreeTimeText.visibility = View.GONE
        binding.startFreeTimeText.visibility = View.GONE
        binding.extraNotesText.visibility = View.GONE
        binding.sessionText.visibility = View.GONE
        binding.breakText.visibility = View.GONE
        binding.tableLayout.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE
        binding.freeTimeCheckBox.visibility = View.GONE
        binding.lblDays.visibility = View.GONE
        binding.checkSunday.visibility = View.GONE
        binding.checkMonday.visibility = View.GONE
        binding.checkTuesday.visibility = View.GONE
        binding.checkWednesday.visibility = View.GONE
        binding.checkThursday.visibility = View.GONE
        binding.checkFriday.visibility = View.GONE
        binding.checkSaturday.visibility = View.GONE
        binding.fridayEndFreeTimeEditText.visibility = View.GONE
        binding.fridayStartFreeTimeEditText.visibility = View.GONE
        binding.mondayEndFreeTimeEditText.visibility = View.GONE
        binding.mondayStartFreeTimeEditText.visibility = View.GONE
        binding.saturdayEndFreeTimeEditText.visibility = View.GONE
        binding.saturdayStartFreeTimeEditText.visibility = View.GONE
        binding.sundayEndFreeTimeEditText.visibility = View.GONE
        binding.sundayStartFreeTimeEditText.visibility = View.GONE
        binding.thursdayEndFreeTimeEditText.visibility = View.GONE
        binding.thursdayStartFreeTimeEditText.visibility = View.GONE
        binding.tuesdayEndFreeTimeEditText.visibility = View.GONE
        binding.tuesdayStartFreeTimeEditText.visibility = View.GONE
        binding.wednesdayEndFreeTimeEditText.visibility = View.GONE
        binding.wednesdayStartFreeTimeEditText.visibility = View.GONE
    }

    private fun setupCheckboxListeners() {
        binding.checkMonday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkTuesday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkWednesday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkThursday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkFriday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkSaturday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.checkSunday.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
        binding.freeTimeCheckBox.setOnCheckedChangeListener { _, _ -> updateFreeTimeVisibility() }
    }

    private fun updateFreeTimeVisibility() {
        val isFreeTimeChecked = binding.freeTimeCheckBox.isChecked

        binding.startFreeTimeText.visibility = if (isFreeTimeChecked) View.GONE else View.VISIBLE
        binding.endFreeTimeText.visibility = if (isFreeTimeChecked) View.GONE else View.VISIBLE

        updateVisibilityForDay(binding.checkMonday, binding.mondayStartFreeTimeEditText, binding.mondayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkTuesday, binding.tuesdayStartFreeTimeEditText, binding.tuesdayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkWednesday, binding.wednesdayStartFreeTimeEditText, binding.wednesdayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkThursday, binding.thursdayStartFreeTimeEditText, binding.thursdayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkFriday, binding.fridayStartFreeTimeEditText, binding.fridayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkSaturday, binding.saturdayStartFreeTimeEditText, binding.saturdayEndFreeTimeEditText, isFreeTimeChecked)
        updateVisibilityForDay(binding.checkSunday, binding.sundayStartFreeTimeEditText, binding.sundayEndFreeTimeEditText, isFreeTimeChecked)
    }

    private fun updateVisibilityForDay(checkBox: CheckBox, startText: EditText, endText: EditText, isFreeTimeChecked: Boolean) {
        if (checkBox.isChecked && isFreeTimeChecked) {
            startText.visibility = View.VISIBLE
            endText.visibility = View.VISIBLE
        } else {
            startText.visibility = View.GONE
            endText.visibility = View.GONE
        }
    }

    fun goBack(view: View){
        intent = Intent(this@ScheduleScreen, NavigateScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun goBack2(view: View){
        intent = Intent(this@ScheduleScreen, ScheduleScreen::class.java)
        startActivity(intent)
        finish()
    }
}