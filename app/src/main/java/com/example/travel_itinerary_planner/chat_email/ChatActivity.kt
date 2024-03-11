package com.example.travel_itinerary_planner.chat_email

import ChatViewModel
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.HelpCenterActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivityChatBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.social.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatActivity : LoggedInActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ChatViewModel
    private var selectedImageUri: Uri? = null

    private val messagesLiveData = MutableLiveData<List<Message>?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarChat.setNavigationOnClickListener {

            val intent = Intent(this, HelpCenterActivity::class.java)
            startActivity(intent)
        }
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        chatRecyclerView = binding.chatRecyclerView
        adapter = ChatAdapter(this, ArrayList())
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        setupSendMessageButton()
        viewModel.getMessages().observe(this, Observer { messages ->
            messages?.let {
                adapter.messages = messages.toMutableList()
                adapter.notifyDataSetChanged()
            }
        })
        binding.attachButton.setOnClickListener {
            selectImageFromGalleryOrCamera()
        }
        binding.cross.setOnClickListener {
            selectedImageUri = null
            binding.attachImage.visibility = View.GONE
            binding.cross.visibility = View.GONE
        }
    }





    private fun setupSendMessageButton() {
        binding.sendButton.setOnClickListener {
            val messageContent = binding.commentsEdit.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                binding.commentsEdit.text.clear()
            }
        }
    }

    private fun sendMessage(messageContent: String) {
        generateMessageId { messageId ->
            val currentUserUid = auth.currentUser?.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateAndTime = dateFormat.format(Date())
            if (selectedImageUri != null) {
                val imageName = UUID.randomUUID().toString()
                val storageRef = FirebaseStorage.getInstance().reference.child("Message/$imageName.jpg")
                storageRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener { taskSnapshot ->

                        storageRef.downloadUrl.addOnSuccessListener { imageUri ->


                            val message = hashMapOf(
                                "MessageID" to messageId,
                                "UserID" to currentUserUid,
                                "RecipientID" to null,
                                "MessageText" to messageContent,
                                "MessageDate" to currentDateAndTime,
                                "MessageImage" to imageUri.toString(),
                                "MessageChannel" to "Customer Service"
                            )

                            firestore.collection("Message").document(messageId)
                                .set(message)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener { e ->

                                }
                        }
                    }
                    .addOnFailureListener { e ->


                    }
            } else {


                val message = hashMapOf(
                    "MessageID" to messageId,
                    "UserID" to currentUserUid,
                    "RecipientID" to null,
                    "MessageText" to messageContent,
                    "MessageDate" to currentDateAndTime,
                    "MessageImage" to null,
                    "MessageChannel" to "Customer Service"
                )

                firestore.collection("Message").document(messageId)
                    .set(message)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->

                    }
            }


            selectedImageUri = null
            binding.attachImage.visibility = View.GONE
            binding.cross.visibility = View.GONE
        }
    }


    private fun generateMessageId(callback: (String) -> Unit) {
        firestore.collection("Message")
            .orderBy("MessageID", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val newMessageId = if (!querySnapshot.isEmpty) {
                    val latestMessageId = querySnapshot.documents[0].getString("MessageID")
                    val latestMessageNumber = latestMessageId?.substring(1)?.toInt() ?: 0
                    val newMessageNumber = latestMessageNumber + 1
                    "M${String.format("%09d", newMessageNumber)}"
                } else {
                    "M000000001"
                }
                callback(newMessageId)
            }
            .addOnFailureListener { exception ->
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



    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    selectedImageUri = getImageUri(this, resizedBitmap)
                    binding.attachImage.setImageBitmap(resizedBitmap)
                    binding.attachImage.visibility = View.VISIBLE
                    binding.cross.visibility = View.VISIBLE
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
                    val inputStream = this.contentResolver.openInputStream(uri)
                    inputStream?.use { stream: InputStream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val resizedBitmap = resizeBitmap(bitmap)
                        selectedImageUri = getImageUri(this, resizedBitmap)
                        binding.attachImage.setImageBitmap(resizedBitmap)
                        binding.attachImage.visibility = View.VISIBLE
                        binding.cross.visibility = View.VISIBLE
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
