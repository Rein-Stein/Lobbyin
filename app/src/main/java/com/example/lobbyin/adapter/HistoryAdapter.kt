package com.example.lobbyin.adapter

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.lobbyin.R
import com.example.lobbyin.model.Booking
import com.example.lobbyin.model.Comment

class HistoryAdapter(
    private val list: MutableList<Booking>,
    private val isDosen: Boolean,
    private val isAdmin: Boolean,
    private val onCancel: (Booking) -> Unit,
    private val onAddComment: (Booking, String) -> Unit,
    private val onDeleteComment: (Booking, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoom: TextView = view.findViewById(R.id.tvHistoryRoom)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvName: TextView = view.findViewById(R.id.tvHistoryName)
        val tvMatkul: TextView = view.findViewById(R.id.tvHistoryMatkul)
        val tvTime: TextView = view.findViewById(R.id.tvHistoryTime)
        val btnCancel: Button = view.findViewById(R.id.btnCancelBooking)
        
        val commentsContainer: LinearLayout = view.findViewById(R.id.commentsContainer)
        val addCommentLayout: View = view.findViewById(R.id.addCommentLayout)
        val etNewComment: EditText = view.findViewById(R.id.etNewComment)
        val btnSendComment: ImageButton = view.findViewById(R.id.btnSendComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = list[position]
        holder.tvRoom.text = booking.room
        holder.tvDate.text = booking.date
        holder.tvName.text = "Nama: ${booking.nama}"
        holder.tvMatkul.text = "Matkul: ${booking.matkul}"
        holder.tvTime.text = booking.time

        // Clear and populate comments
        holder.commentsContainer.removeAllViews()
        booking.comments.forEachIndexed { index, comment ->
            val commentItem = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 4, 0, 4)
            }

            val tvComment = TextView(holder.itemView.context).apply {
                val label = if (comment.role == "Admin") "Komentar Admin: " else "Komentar Dosen: "
                text = "$label${comment.text}"
                textSize = 13f
                setTypeface(null, Typeface.ITALIC)
                setTextColor(context.getColor(R.color.navy_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            commentItem.addView(tvComment)

            // Logic for showing delete button
            // Admin can delete everything. Dosen can only delete "Komentar Dosen".
            val canDelete = isAdmin || (isDosen && comment.role == "Dosen")

            if (canDelete) {
                val btnDelete = ImageButton(holder.itemView.context).apply {
                    setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    background = null
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    layoutParams = LinearLayout.LayoutParams(48, 48)
                    setPadding(8, 8, 8, 8)
                    setColorFilter(context.getColor(R.color.status_red))
                    setOnClickListener {
                        onDeleteComment(booking, index)
                    }
                }
                commentItem.addView(btnDelete)
            }

            holder.commentsContainer.addView(commentItem)
        }

        if (isDosen || isAdmin) {
            holder.btnCancel.visibility = View.VISIBLE
            holder.btnCancel.setOnClickListener { onCancel(booking) }
            
            holder.addCommentLayout.visibility = View.VISIBLE
            holder.btnSendComment.setOnClickListener {
                val text = holder.etNewComment.text.toString().trim()
                if (text.isNotEmpty()) {
                    onAddComment(booking, text)
                    holder.etNewComment.setText("")
                }
            }
        } else {
            holder.btnCancel.visibility = View.GONE
            holder.addCommentLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = list.size

    fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}