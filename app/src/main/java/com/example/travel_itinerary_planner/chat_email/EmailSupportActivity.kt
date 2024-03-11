package com.example.travel_itinerary_planner.chat_email

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.travel_itinerary_planner.HelpCenterActivity
import com.example.travel_itinerary_planner.databinding.ActivityEmailSupportBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class EmailSupportActivity : LoggedInActivity() {
    private lateinit var binding: ActivityEmailSupportBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.toolbarEmail.setNavigationOnClickListener {
            val intent = Intent(this, HelpCenterActivity::class.java)
            startActivity(intent)
        }

        binding.buttonEmailSubmit.setOnClickListener {

            sendEmail()
        }

        val currentUser = auth.currentUser
        val userEmail = currentUser?.email
        binding.emailText.text = userEmail

        binding.imageButton.setOnClickListener {
            selectImageFromGalleryOrCamera()
        }
    }

    private fun sendEmail() {
        val issueDescription = binding.editTexMultiLine.text.toString()
        val selectedImageUri = binding.imageButton.text.toString()

        if (issueDescription.isBlank()) {
            binding.editTexMultiLine.error = "Please enter issue description"
            return
        }

        if (selectedImageUri.contains("Submit Proof")) {
            binding.imageButton.error = "Please select an image"
            return
        }
        if (selectedImageUri.contains("Submit Proof Again")) {
            binding.imageButton.error = "Please select an image"
            return
        }
        val recipientEmails = mutableListOf<String>()

        getUsersEmailsByCategory("Customer Support Admin") { emails ->
            recipientEmails.addAll(emails)
            getUsersEmailsByCategory("Owner") { emails ->
                recipientEmails.addAll(emails)
                getUsersEmailsByCategory("Admin") { emails ->
                    recipientEmails.addAll(emails)

                    if (recipientEmails.isNotEmpty()) {
                        val subject = "Support Request"
                        val body = getEmailBody()

                        val emailIntent = Intent(Intent.ACTION_SEND)
                        emailIntent.type = "message/rfc822"
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipientEmails.toTypedArray())
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                        emailIntent.setPackage("com.google.android.gm")

                        selectedBitmap?.let { bitmap ->
                            val uri = getImageUri(this, bitmap)
                            emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
                            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send email"))
                        } catch (ex: ActivityNotFoundException) {

                        }
                    } else {

                    }
                }
            }
        }
    }
    private fun getUsersEmailsByCategory(category: String, onComplete: (List<String>) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("UserCategory", category)
            .get()
            .addOnSuccessListener { documents ->
                val emails = mutableListOf<String>()
                for (document in documents) {
                    val email = document.getString("Email")
                    if (!email.isNullOrEmpty()) {
                        emails.add(email)
                    }
                }
                onComplete(emails)
            }
            .addOnFailureListener { exception ->

            }
    }


    private fun getEmailBody(): String {
        val issueDescription = binding.editTexMultiLine.text.toString()
        return "Issue Description:\n$issueDescription"
    }

    private fun selectImageFromGalleryOrCamera() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> openCamera()
                options[item] == "Choose from Gallery" -> openGallery()
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Image.jpg")
            val outputStream = FileOutputStream(imagesDir)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imagesDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun updateSelectedImage(uri: Uri?) {
        val uriText = uri?.toString() ?: "Submit Proof Again"
        binding.imageButton.text = uriText
    }


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    selectedBitmap = resizedBitmap
                    val selectedImageUri = getImageUri(this, resizedBitmap)
                    updateSelectedImage(selectedImageUri)
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let { data: Intent ->
                val selectedImageUri = data.data
                selectedImageUri?.let { uri ->
                    val inputStream = this.contentResolver.openInputStream(uri)
                    inputStream?.use { stream: InputStream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val resizedBitmap = resizeBitmap(bitmap)
                        selectedBitmap = resizedBitmap

                        updateSelectedImage(selectedImageUri)

                    }
                }
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1024
        val maxHeight = 1024

        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio: Float = width.toFloat() / height.toFloat()

        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = minOf(width, maxWidth)
            finalHeight = (finalWidth / ratio).toInt()
        } else {
            finalHeight = minOf(height, maxHeight)
            finalWidth = (finalHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}
