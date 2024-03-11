import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentSocialCommentBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.example.travel_itinerary_planner.social.SocialCommentAdapter
import com.example.travel_itinerary_planner.social.UserData
import com.example.travel_itinerary_planner.social.UserSocial
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class SocialCommentBottomSheetFragment : BottomSheetDialogFragment(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SocialCommentAdapter
    private lateinit var binding: FragmentSocialCommentBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var selectedImageUri: Uri? = null
    private var postId: String? = null
    private var commentEnabled = true
    private val commentCooldownDuration = 3000L
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSocialCommentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        postId = arguments?.getString("postId")
        updateCommentCounts(postId?:"")
        adapter = SocialCommentAdapter { comment ->

            deleteComment(comment)
        }

        recyclerView = view.findViewById(R.id.commentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        binding.sendBtn.setOnClickListener {
            val comment = binding.commentsEdit.text.toString().trim()
            if (comment.isNotEmpty()) {

                if (selectedImageUri != null) {

                    saveCommentWithImageToFirestore(comment, selectedImageUri!!)
                } else {

                    saveCommentToFirestore(comment)
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
            disableCommentWriting()
        }
        binding.attachBtn.setOnClickListener {
            selectImageFromGalleryOrCamera()
        }
        binding.crossIcon.setOnClickListener {
            selectedImageUri = null
            binding.attachedImage.visibility = View.GONE
            binding.crossIcon.visibility = View.GONE
        }
        fetchComments(postId)


    }
    private fun disableCommentWriting() {
        commentEnabled = false
        binding.commentsEdit.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            commentEnabled = true
            binding.commentsEdit.isEnabled = true
        }, commentCooldownDuration)
    }

    private fun deleteComment(comment: UserSocial) {

        val postId = comment.SocialID

        val commentsRef = firestore.collection("UserSocial")
        val query = commentsRef.whereEqualTo("SocialID", postId).whereEqualTo("CommentImage",comment.CommentImage)
            .whereEqualTo("UserComment",comment.UserComment).whereEqualTo("CreateDate",comment.CreateDate)

        query.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val commentId = document.id
                firestore.collection("UserSocial").document(commentId)
                    .delete()
                    .addOnSuccessListener {
                        postId?.let { updateSocialMediaCommentCounts(it)
                            updateCommentCounts(it)
                            fetchComments(it) }
                        Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete comment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to query comments: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateSocialMediaCommentCounts(socialId: String) {
        val userSocialRef = firestore.collection("UserSocial")
        val query = userSocialRef.whereEqualTo("SocialID", socialId)

        query.get().addOnSuccessListener { querySnapshot ->
            val commentCount = querySnapshot.size().toString()
            val socialMediaRef = firestore.collection("SocialMedia").document(socialId)

            socialMediaRef.update("SocialCommentCounts", commentCount)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->

                }
        }.addOnFailureListener { e ->

        }
    }
    private fun fetchComments(postId: String?) {
        postId?.let { postId ->
            val commentsRef = firestore.collection("UserSocial")
            val query = commentsRef.whereEqualTo("SocialID", postId)

            query.get().addOnSuccessListener { querySnapshot ->
                val commentsList = mutableListOf<UserSocial>()
                for (document in querySnapshot.documents) {
                    val comment = document.toObject(UserSocial::class.java)
                    comment?.let {
                        commentsList.add(comment)
                    }
                }
                adapter.submitList(commentsList)

            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch comments: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateCommentCounts(socialId: String) {
        val userSocialRef = firestore.collection("UserSocial")
        val query = userSocialRef.whereEqualTo("SocialID", socialId)

        query.get().addOnSuccessListener { querySnapshot ->
            val commentCount = querySnapshot.size().toString()
            val socialMediaRef = firestore.collection("SocialMedia").document(socialId)

            socialMediaRef.update("SocialCommentCounts", commentCount)
                .addOnSuccessListener {
                    binding.commentsTotal.text = "$commentCount Comments"
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update SocialMedia comment count: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to fetch UserSocial documents: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCommentToFirestore(comment: String) {
        val currentUserUid = auth.currentUser?.uid
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = Calendar.getInstance().time
        val formattedDate = dateFormat.format(currentDateAndTime).toString()
        postId = arguments?.getString("postId")
        val commentData = hashMapOf(
            "SocialID" to postId,
            "UserID" to currentUserUid,
            "UserComment" to comment,
            "CommentImage" to null,
            "CreateDate" to formattedDate
        )

        val commentsRef = firestore.collection("UserSocial")
        commentsRef.add(commentData)
            .addOnSuccessListener {

                Toast.makeText(requireContext(), "Comment added successfully", Toast.LENGTH_SHORT).show()

                postId?.let { updateSocialMediaCommentCounts(it)
                    updateCommentCounts(it)
                    fetchComments(it)
                    selectedImageUri = null
                    binding.attachedImage.visibility = View.GONE
                    binding.crossIcon.visibility = View.GONE
                    binding.commentsEdit.setText("")}
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add comment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCommentWithImageToFirestore(comment: String, imageUri: Uri) {
        val currentUserUid = auth.currentUser?.uid
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = Calendar.getInstance().time
        val formattedDate = dateFormat.format(currentDateAndTime).toString()
        postId = arguments?.getString("postId")


        val imageName = UUID.randomUUID().toString()


        val storageRef = FirebaseStorage.getInstance().reference.child("UserSocial/$currentUserUid/$imageName.jpg")


        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->

                storageRef.downloadUrl.addOnSuccessListener { uri ->

                    val commentData = hashMapOf(
                        "SocialID" to postId,
                        "UserID" to currentUserUid,
                        "UserComment" to comment,
                        "CommentImage" to uri.toString(),
                        "CreateDate" to formattedDate
                    )

                    val commentsRef = firestore.collection("UserSocial")
                    commentsRef.add(commentData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Comment with image added successfully", Toast.LENGTH_SHORT).show()

                            postId?.let {
                                updateSocialMediaCommentCounts(it)
                                updateCommentCounts(it)
                                fetchComments(it)
                                selectedImageUri = null
                                binding.attachedImage.visibility = View.GONE
                                binding.crossIcon.visibility = View.GONE
                                binding.commentsEdit.setText("")
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to add comment with image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun selectImageFromGalleryOrCamera() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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



    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    selectedImageUri = getImageUri(requireContext(), resizedBitmap)
                    binding.attachedImage.setImageBitmap(resizedBitmap)
                    binding.attachedImage.visibility = View.VISIBLE
                    binding.crossIcon.visibility = View.VISIBLE
                }

            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let { data: Intent ->
                val uri = data.data
                uri?.let { selectedImageUri = it
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.use { stream: InputStream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val resizedBitmap = resizeBitmap(bitmap)
                        selectedImageUri = getImageUri(requireContext(), resizedBitmap)
                        binding.attachedImage.setImageBitmap(resizedBitmap)
                        binding.attachedImage.visibility = View.VISIBLE
                        binding.crossIcon.visibility = View.VISIBLE
                    }
                }
            }
        }
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





}
