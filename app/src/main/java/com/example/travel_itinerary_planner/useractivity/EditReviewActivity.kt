package com.example.travel_itinerary_planner.useractivity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.EditReviewBinding
import com.example.travel_itinerary_planner.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class EditReviewActivity:LoggedInActivity() {
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: EditReviewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val documentId = intent.getStringExtra("documentId")
        val rateId = intent.getStringExtra("REVIEW_ID")
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding.imageButton8.setOnClickListener { finish() }
        fetchTourismAttraction(documentId!!)
        fetchAndCalculateReviews(documentId)
        setupListeners()
        validateInput()
        if (documentId != null && rateId != null) {
            fetchTourismAttraction(documentId)
            fetchReviewDetails(documentId, rateId)
        }
        binding.imageViewSelectedImage.setOnClickListener {
            it.visibility = View.GONE
            binding.imageViewSelectedImage.setImageURI(null)
        }

        binding.addimage.setOnClickListener {
            openGallery()
        }
        binding.addcamera.setOnClickListener {
            openCamera()
        }
        binding.deleterecord.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo: Bitmap? = result.data?.extras?.get("data") as? Bitmap
            photo?.let { bitmap ->
                val uri = bitmapToUri(bitmap)
                selectedImageUri = uri
                binding.imageViewSelectedImage.setImageBitmap(bitmap)
                binding.imageViewSelectedImage.visibility = View.VISIBLE
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data as Uri
            binding.imageViewSelectedImage.setImageURI(selectedImageUri)
            binding.imageViewSelectedImage.visibility = View.VISIBLE
        }
    }

    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return Uri.fromFile(file)
    }

    private fun setupListeners() {
        binding.imageButton8.setOnClickListener { finish() }

        binding.editTextReview.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textViewCharCount1.text = "${s?.length ?: 0}"
                validateInput()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            validateInput()
        }

        binding.button.setOnClickListener {
            if (validateInput()) {
                submitReview()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Yes") { dialog, which ->
                deleteReview()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
    private fun deleteReview() {
        val documentId = intent.getStringExtra("documentId")
        val rateId = intent.getStringExtra("REVIEW_ID")
        if (documentId != null && rateId != null) {
            firestore.collection("Tourism Attractions").document(documentId)
                .collection("Review").document(rateId).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    Log.e("EditReviewActivity", "Error deleting review", e)
                    Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: Document ID or Review ID is missing", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeFragment::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun submitReview() {
        val reviewText = binding.editTextReview.text.toString().trim()
        val rating = binding.ratingBar.rating.toString()
        val userId = auth.currentUser?.uid ?: ""
        val documentId = intent.getStringExtra("documentId")
        val rateId = intent.getStringExtra("REVIEW_ID")
        val reviewMap: HashMap<String, Any> = hashMapOf(
            "RateNo" to rating,
            "UserID" to userId,
            "RateDesc" to reviewText,
            "RateDate" to com.google.firebase.Timestamp.now()
        )


        if (::selectedImageUri.isInitialized) {
            uploadImage { imageUrl ->
                reviewMap["RateUrl"] = imageUrl
                updateOrCreateReview(documentId, rateId, reviewMap)
            }
        } else {
            updateOrCreateReview(documentId, rateId, reviewMap)
        }
    }
    private fun updateOrCreateReview(documentId: String?, rateId: String?, reviewData: HashMap<String, Any>) {
        if (documentId.isNullOrEmpty()) {
            Toast.makeText(this, "Document ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }
        val reviewCollection = firestore.collection("Tourism Attractions").document(documentId).collection("Review")

        if (rateId.isNullOrEmpty()) {
            reviewCollection.add(reviewData).addOnSuccessListener {
                Toast.makeText(this, "Review added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add review: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            reviewCollection.document(rateId).set(reviewData).addOnSuccessListener {
                Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update review: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImage(onUploadComplete: (String) -> Unit) {
        val ref = storage.reference.child("rate/${UUID.randomUUID()}.jpg")
        ref.putFile(selectedImageUri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    onUploadComplete(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddReview", "Failed to upload image: ${e.message}", e)
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateReviewInFirestore(documentId: String, rateId: String, reviewData: HashMap<String, Any>) {
        firestore.collection("Tourism Attractions").document(documentId)
            .collection("Review").document(rateId)
            .set(reviewData)
            .addOnSuccessListener {
                Log.d("EditReview", "Review updated successfully")
                Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditReview", "Error updating review: ${e.message}", e)
                Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchReviewDetails(documentId: String, rateId: String) {
        firestore.collection("Tourism Attractions").document(documentId)
            .collection("Review").document(rateId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rating = document.getString("RateNo")?.toFloat() ?: 0f
                    val reviewText = document.getString("RateDesc") ?: ""
                    val imageUrl = document.getString("RateUrl")
                    binding.ratingBar.rating = rating
                    binding.editTextReview.setText(reviewText)
                    imageUrl?.let {
                        Glide.with(this).load(it).into(binding.imageViewSelectedImage)
                        binding.imageViewSelectedImage.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditReviewActivity", "Error fetching review details", e)
            }
    }


    private fun validateInput(): Boolean {
        val rating = binding.ratingBar.rating
        val reviewText = binding.editTextReview.text.toString().trim()

        val isValid = rating > 0 && reviewText.isNotEmpty()
        binding.button.isEnabled = isValid

        Log.d("AddReview", "Validating: rating=$rating, reviewText='$reviewText', isValid=$isValid")

        return isValid
    }
    private fun fetchTourismAttraction(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("TourismName")
                    val imageUrl = document.getString("TourismImage")
                    binding.textView24.text = name
                    binding.textView25.text = document.getString("TourismState")
                    Glide.with(this).load(imageUrl).into(binding.imageButton11)
                } else {
                }
            }
            .addOnFailureListener { e -> }
    }
    private fun fetchAndCalculateReviews(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).collection("Review")
            .get()
            .addOnSuccessListener { result ->
                val starCounts = IntArray(5)
                var reviewCount = 0
                for (document in result) {
                    val ratingString = document.getString("RateNo")
                    val rating = ratingString?.toDoubleOrNull()?.let { Math.round(it).toInt() }
                    if (rating != null && rating in 1..5) {
                        starCounts[rating - 1]++
                        reviewCount++
                    }
                }
                if (reviewCount > 0) {
                    val percentages = starCounts.map { it * 100 / reviewCount.toDouble() }
                    updateUIWithRatingAndPercentages(percentages)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TourismActivity", "Error fetching reviews", e)
            }
    }
    private fun updateUIWithRatingAndPercentages(percentages: List<Double>) {
        runOnUiThread {
            val totalRating = percentages.indices.sumOf { (it + 1) * percentages[it] }
            val averageRating = totalRating / 100.0
            val formattedAverageRating = String.format("%.1f", averageRating)
            binding.textView26.text = formattedAverageRating
        }
    }


}