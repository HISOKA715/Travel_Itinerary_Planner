package com.example.travel_itinerary_planner.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.AddNewPostActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentProfileBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.social.PostDetailsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ProfileFragment : LoggedInFragment(), SocialMediaAdapter.OnItemClickListener  {

    private val binding get() = _binding!!
    private var _binding: FragmentProfileBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var socialMediaAdapter: SocialMediaAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        private const val REQUEST_IMAGE_CAPTURE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        fetchProfilePictureFromFirestore()
        binding.buttonEditProfile.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_drawer -> {
                    val navController = findNavController()
                    navController.navigate(R.id.navigation_drawer)
                    true
                }
                R.id.menu_add_post -> {
                    selectImageFromGalleryOrCamera()
                    true
                }
                else -> false
            }
        }
        socialMediaAdapter = SocialMediaAdapter(this)

        binding.postRecycleView.apply {
            val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.postRecycleView.layoutManager = layoutManager
            binding.postRecycleView.adapter = socialMediaAdapter


        }
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let { fetchUserData(it) }
        retrieveSocialMediaPosts()
    }
    override fun onItemClick(socialMediaPost: SocialMediaPost) {
        val position = socialMediaAdapter.getSocialMediaPosts().indexOf(socialMediaPost)

        val bundle = Bundle()
        bundle.putInt("position", position)


        val postDetailsFragment = PostDetailsFragment()
        postDetailsFragment.arguments = bundle


        val navController = findNavController()
        navController.navigate(R.id.navigation_post_details, bundle)
    }


    private fun retrieveSocialMediaPosts() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("SocialMedia")
            .whereEqualTo("UserID", currentUserUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val socialMediaPosts = mutableListOf<SocialMediaPost>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(SocialMediaPost::class.java)
                    post?.let {
                        socialMediaPosts.add(0,it)
                    }
                }
                socialMediaAdapter.submitList(socialMediaPosts)
            }
            .addOnFailureListener { e ->
            }
    }

    private fun fetchProfilePictureFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val firestore = FirebaseFirestore.getInstance()
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
                                .into(binding.profileImageView)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to fetch profile picture", Toast.LENGTH_SHORT).show()
                }
        }
    }








    private fun selectImageFromGalleryOrCamera() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
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



    private fun navigateToAddNewPost(selectedImageUri: Uri) {
        val intent = Intent(context, AddNewPostActivity::class.java)
        intent.putExtra("selected_image_uri", selectedImageUri.toString())
        startActivity(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    val selectedImageUri = getImageUri(requireContext(), resizedBitmap)
                    navigateToAddNewPost(selectedImageUri!!)
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
                    val inputStream = requireContext().contentResolver.openInputStream(uri) // Using requireContext() in a Fragment
                    inputStream?.use { stream: InputStream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val resizedBitmap = resizeBitmap(bitmap)
                        val resizedImageUri = getImageUri(requireContext(), resizedBitmap)
                        navigateToAddNewPost(resizedImageUri!!)
                    }
                }
            }
        }
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
            val username = userData["Username"].toString()
            usernameText.text = username

        }
    }




}