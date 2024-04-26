package com.example.travel_itinerary_planner
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.travel_itinerary_planner.notification.NotificationDetailActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

const val TAG = "MyFirebaseMessagingService"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")


        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {

        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val title = message.notification?.title ?: "Title"
        val body = message.notification?.body ?: "Message Body"
        val imageUrl = message.notification?.imageUrl?.toString() ?: ""
        val imageBitmap = if (!imageUrl.isNullOrEmpty()) getBitmapFromURL(imageUrl) else null
        displayNotification(title, body, imageBitmap, imageUrl)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            addNotificationToFirestore(it, title, body, imageUrl)
        }

    }

    private fun addNotificationToFirestore(userId: String, title: String, body: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "title" to title,
            "body" to body,
            "imageUrl" to imageUrl,
            "date" to FieldValue.serverTimestamp(),
            "read" to "false"
        )
        db.collection("users").document(userId).collection("notifications").add(notificationData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun displayNotification(title: String, body: String, image: Bitmap?, imageUrl: String?) {
        val intent = Intent(this, NotificationDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_title", title)
            putExtra("notification_body", body)
            putExtra("image_url", imageUrl)
        }

        Log.d(TAG, "Intent Created - Title: $title, Body: $body, Image URL: $imageUrl")
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (image != null) {
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(image).bigLargeIcon(null as Bitmap?))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        Log.d(TAG, "Notification displayed with Request Code: $requestCode")
        notificationManager.notify(requestCode, notificationBuilder.build())
    }
    private fun getBitmapFromURL(src: String): Bitmap? {
        try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {

        fun getCurrentToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d(TAG, "Current FCM Token: $token")
            }
        }
    }
}