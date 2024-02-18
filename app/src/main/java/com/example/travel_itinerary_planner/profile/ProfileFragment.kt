package com.example.travel_itinerary_planner.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.AddNewPostActivity
import com.example.travel_itinerary_planner.EditProfileActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentProfileBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import java.io.ByteArrayOutputStream

class ProfileFragment : LoggedInFragment(), SocialMediaAdapter.OnItemClickListener  {

    private val binding get() = _binding!!
    private var _binding: FragmentProfileBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var socialMediaAdapter: SocialMediaAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        private const val REQUEST_IMAGE_CAPTURE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val profileViewModel =
            ViewModelProvider(this)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        binding.buttonEditProfile.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_drawer -> {
                    val navController = findNavController()
                    navController.navigate(R.id.drawerFragment)
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
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = socialMediaAdapter
        }

        retrieveSocialMediaPosts()
    }
    override fun onItemClick(socialMediaPost: SocialMediaPost) {
        val intent = Intent(context, PostDetailsActivity::class.java)
        startActivity(intent)
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
                        socialMediaPosts.add(it)
                    }
                }
                socialMediaAdapter.submitList(socialMediaPosts)
            }
            .addOnFailureListener { e ->
                // Handle failure
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

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
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

                val photo: Bitmap = it.extras?.get("data") as Bitmap
                val selectedImageUri = getImageUri(requireContext(), photo)
                navigateToAddNewPost(selectedImageUri)
            }

        }
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {

                val selectedImageUri = it.data
                navigateToAddNewPost(selectedImageUri!!)
            }

        }
    }




}