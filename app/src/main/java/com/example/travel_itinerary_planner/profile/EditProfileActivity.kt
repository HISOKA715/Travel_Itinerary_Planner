package com.example.travel_itinerary_planner.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivityEditProfileBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class EditProfileActivity : LoggedInActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fetchProfilePictureFromFirestore()
        binding.toolbarEditProfile.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
        }
        binding.changeProfilePhoto.setOnClickListener {
            selectImageFromGalleryOrCamera()
        }
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let { fetchUserData(it) }

        setEditTextListeners()
    }

    private fun showGenderSelectionDialog() {
        val options = arrayOf("Male", "Female")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Select Gender")
        builder.setItems(options) { dialog, which ->
            val selectedGender = options[which]
            binding.genderEdit.setText(selectedGender)
            dialog.dismiss()

            val currentUser = auth.currentUser
            val userId = currentUser?.uid

            userId?.let { updateUserData(it, "Gender", selectedGender) }
        }
        builder.show()
    }




    private fun fetchUserData(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userData = document.data
                    userData?.let { populateEditTextFields(it) }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun populateEditTextFields(userData: Map<String, Any>) {
        binding.apply {
            usernameEdit.setText(userData["Username"].toString())
            genderEdit.setText(userData["Gender"].toString())
        }
    }

    private fun setEditTextListeners() {
        binding.apply {
            usernameEdit.setOnClickListener { showUpdateDialog(usernameEdit) }
            genderEdit.setOnClickListener { showGenderSelectionDialog() }

        }
    }

    private fun showUpdateDialog(editText: EditText) {
        val currentValue = editText.text?.toString() ?: ""

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update")

        val input = EditText(this)
        input.setText(currentValue)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val newValue = input.text.toString()
            editText.setText(newValue)
            dialog.dismiss()

            val currentUser = auth.currentUser
            val userId = currentUser?.uid

            userId?.let { updateUserData(it, editText.tag.toString(), newValue) }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
    private fun updateUserData(userId: String, field: String, value: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.update(field, value)
            .addOnSuccessListener {

                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->

                Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProfilePictureFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->

            val userRef = firestore.collection("users").document(currentUser.uid)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val profilePictureUri = documentSnapshot.getString("ProfileImage")
                        profilePictureUri?.let { uri ->

                            Glide.with(this)
                                .load(uri)
                                .override(300, 300)
                                .error(R.drawable.travel_main)
                                .centerCrop()
                                .into(binding.imageProfile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch profile picture", Toast.LENGTH_SHORT).show()
                }
        }
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
            val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile("Title", ".jpg", imagesDir)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun navigate(selectedImageUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(currentUser.uid)

            val profilePictureUri = selectedImageUri.toString()

            userRef.update("ProfileImage", profilePictureUri)
                .addOnSuccessListener {
                    binding.imageProfile.setImageURI(selectedImageUri)
                    Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    val selectedImageUri = getImageUri(this, resizedBitmap)
                    navigate(selectedImageUri!!)
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
                        val resizedImageUri = getImageUri(this, resizedBitmap)
                        navigate(resizedImageUri!!)
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