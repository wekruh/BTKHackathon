package com.wekruh.mybtkhackathonapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.wekruh.mybtkhackathonapp.databinding.ActivityLessonCoachScreenBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Button
import android.widget.Toast
import androidx.core.view.marginTop


class LessonCoachScreen : AppCompatActivity() {
    private var isFirstInteraction = true
    private var context: String = ""
    private lateinit var binding: ActivityLessonCoachScreenBinding

    private val PICK_IMAGE_REQUEST = 1001
    private val conversationHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonCoachScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        addMessageToChat(
            "Hello! I'm AI Helper. I'm here to help you with your studies, provide guidance on projects, and offer motivation. Feel free to ask me anything!",
            isUser = false
        )

    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PICK_IMAGE_REQUEST)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Log.e("LessonCoachScreen", "Permission denied to read media images or external storage")
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("LessonCoachScreen", "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == PICK_IMAGE_REQUEST) {
            when (resultCode) {
                RESULT_OK -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        Log.d("LessonCoachScreen", "Selected image URI: $imageUri")
                        addImageToChat(imageUri)
                        commentPicture(imageUri)
                        saveImageToInternalStorage(imageUri)
                    } else {
                        Log.e("LessonCoachScreen", "Error: Unable to get the selected image. URI is null.")
                        addMessageToChat("Error: Unable to get the selected image.", isUser = false)
                    }
                }
                RESULT_CANCELED -> {
                    Log.e("LessonCoachScreen", "Image selection canceled by the user.")
                }
                else -> {
                    Log.e("LessonCoachScreen", "Unexpected result code: $resultCode")
                    addMessageToChat("Error: Unexpected result from image selection.", isUser = false)
                }
            }
        } else {
            Log.w("LessonCoachScreen", "Ignored requestCode: $requestCode")
        }
    }

    private fun addMessageToChat(message: String, isUser: Boolean) {
        val messageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (isUser) Gravity.END else Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
        }

        val containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        val messageTextView = TextView(this).apply {
            text = message
            textSize = 16f
            setBackgroundResource(if (isUser) R.drawable.buttons else R.drawable.chat_ai_background)
            typeface = ResourcesCompat.getFont(context, R.font.coolvetica)
            setTextColor(0xFFFFFFFF.toInt())
            maxWidth = resources.getDimensionPixelSize(R.dimen.chat_message_max_width)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setPadding(24, 16, 24, 16)
        }

        containerLayout.addView(messageTextView)

        if (!isUser) {
            val copyButton = Button(this).apply {
                background = ResourcesCompat.getDrawable(resources, R.drawable.copy_icon, null)
                layoutParams = LinearLayout.LayoutParams(
                    48,
                    48
                ).apply {
                    gravity = Gravity.END
                    setMargins(0, 4, 0, 0)
                }

                setOnClickListener {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AI Message", message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Message copied to clipboard.", Toast.LENGTH_SHORT).show()
                }
            }

            containerLayout.addView(copyButton)
        }

        messageLayout.addView(containerLayout)

        binding.chatLayout.addView(messageLayout)
        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun commentPicture(imageUri: Uri) {
        val visionModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getApiKey()
        )
        MainScope().launch {
            val bitmap = getBitmapFromUri(imageUri)
            if (bitmap != null) {
                val response = visionModel.generateContent(bitmap)
                runOnUiThread {
                    val responseText = response.text ?: "Sorry, I couldn't generate a comment on the image."
                    conversationHistory.add("$responseText")
                    addMessageToChat("$responseText", isUser = false)
                    binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
                }
            } else {
                addMessageToChat("Error: Unable to load image for comment.", isUser = false)
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("LessonCoachScreen", "Error loading bitmap from URI: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(filesDir, "selected_image.png")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            inputStream?.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getApiKey(): String {
        return "AIzaSyBc8Waxob_Muu2vCeUr-ZCF0mA-ZHTmYrU"
    }

    private fun generateText(userPrompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getApiKey()
        )

        conversationHistory.add("You: $userPrompt")

        if (isFirstInteraction) {
            context = """
                You are a knowledgeable academic coach and a supportive guide...
                You are helpful, motivational, and empathetic. If a student is struggling...
            """.trimIndent()
            isFirstInteraction = false
        }

        val finalPrompt = "$context\n\nConversation:\n${conversationHistory.joinToString("\n")}\nUser's question or request:\n$userPrompt"
        addMessageToChat("$userPrompt", isUser = true)

        MainScope().launch {
            val response = generativeModel.generateContent(finalPrompt)
            runOnUiThread {
                conversationHistory.add("${response.text}")
                addMessageToChat("${response.text}", isUser = false)
                binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    fun send(view: View) {
        val userPrompt = binding.userInput.text.toString()
        if (userPrompt.isNotEmpty()) {
            generateText(userPrompt)
            binding.userInput.text.clear()
        }
    }


    fun selectImage(view: View) {
        checkAndRequestPermissions()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }

    fun goBack(view: View) {
        startActivity(Intent(this@LessonCoachScreen, NavigateScreen::class.java))
        finish()
    }

    private fun addImageToChat(imageUri: Uri) {
        try {
            val bitmap = getBitmapFromUri(imageUri)
            if (bitmap != null) {
                val imageView = ImageView(this).apply {
                    setImageBitmap(bitmap)
                    val maxWidth = resources.getDimensionPixelSize(R.dimen.chat_message_max_width)
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

                    val width: Int
                    val height: Int

                    if (bitmap.width > maxWidth) {
                        width = maxWidth
                        height = (maxWidth / aspectRatio).toInt()
                    } else {
                        width = bitmap.width
                        height = bitmap.height
                    }

                    layoutParams = LinearLayout.LayoutParams(width, height).apply {
                        setMargins(16, 8, 16, 8)
                    }
                }

                val messageLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                    setBackgroundResource(R.drawable.buttons)
                    setPadding(10, 5, 10, 5)
                }

                messageLayout.addView(imageView)
                binding.chatLayout.addView(messageLayout)
                binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
            } else {
                addMessageToChat("Error: Unable to load image.", isUser = false)
            }
        } catch (e: Exception) {
            Log.e("LessonCoachScreen", "Error adding image to chat: ${e.message}")
            addMessageToChat("Error: Unable to add image to chat.", isUser = false)
        }
    }
}