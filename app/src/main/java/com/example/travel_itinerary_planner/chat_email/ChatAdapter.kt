package com.example.travel_itinerary_planner.chat_email

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.social.UserData
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val context: Context, var messages: MutableList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        const val SENT_MESSAGE_TYPE = 0
        const val RECEIVED_MESSAGE_TYPE = 1
        const val DEFAULT_MESSAGE_TYPE = 2

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutResId = if (viewType == SENT_MESSAGE_TYPE) {
            R.layout.sent
        } else if (viewType == RECEIVED_MESSAGE_TYPE) {
            R.layout.receive
        }else{
            R.layout.default_layout
        }
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        return if (message.UserID == currentUserID) {
            SENT_MESSAGE_TYPE
        } else if (message.RecipientID == currentUserID) {
            RECEIVED_MESSAGE_TYPE
        } else {
            DEFAULT_MESSAGE_TYPE
        }
    }



    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.txt_message)
        private val messageDateTextView: TextView = itemView.findViewById(R.id.txt_message_date)
        private val imageMessageImageView: ImageView = itemView.findViewById(R.id.imageMessage)

        fun bind(message: Message) {
            messageTextView.text = message.MessageText
            messageDateTextView.text = message.MessageDate

            if (message.MessageImage != null) {

                imageMessageImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.MessageImage)
                    .error(R.drawable.travel_main)
                    .override(300, 300)
                    .centerCrop()
                    .into(imageMessageImageView)

            } else {

                imageMessageImageView.visibility = View.GONE
            }
        }

    }


}
