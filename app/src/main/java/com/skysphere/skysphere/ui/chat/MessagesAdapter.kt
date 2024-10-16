package com.skysphere.skysphere.ui.chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.skysphere.skysphere.R

class MessagesAdapter(messagesList: MutableList<Messages>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private val messagesList = messagesList
    private lateinit var firebaseAuth: FirebaseAuth

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderMsg = itemView.findViewById<TextView>(R.id.senderMsg)
        val receiverMsg = itemView.findViewById<TextView>(R.id.receiverMsg)
        val msgProfileImage = itemView.findViewById<ImageView>(R.id.msgProfileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_format, parent, false)

        firebaseAuth = FirebaseAuth.getInstance()

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentUserId = firebaseAuth.currentUser?.uid
        val message : Messages = messagesList.get(position)
        val fromUserID = message.from
        val fromMessageType = message.type

        if(fromMessageType.equals("text")){
            holder.receiverMsg.visibility = View.INVISIBLE
            holder.msgProfileImage.visibility = View.INVISIBLE

            if (fromUserID.equals(currentUserId)) {
                holder.senderMsg.setBackgroundResource(R.drawable.msg_sender_text_background)
                holder.senderMsg.setTextColor(Color.WHITE)
                holder.senderMsg.gravity = Gravity.LEFT
                holder.senderMsg.text = message.message
            }else{
                holder.senderMsg.visibility = View.INVISIBLE
                holder.receiverMsg.visibility = View.VISIBLE
                holder.msgProfileImage.visibility = View.VISIBLE

                holder.receiverMsg.setBackgroundResource(R.drawable.msg_receiver_text_background)
                holder.receiverMsg.setTextColor(Color.WHITE)
                holder.receiverMsg.gravity = Gravity.LEFT
                holder.receiverMsg.text = message.message
            }
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}