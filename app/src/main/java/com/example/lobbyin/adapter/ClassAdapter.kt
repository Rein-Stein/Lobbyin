package com.example.lobbyin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lobbyin.R
import com.example.lobbyin.model.ClassRoom

class ClassAdapter(
    private val list: ArrayList<ClassRoom>,
    private val isAdmin: Boolean = false,
    private val onEditClick: (ClassRoom) -> Unit = {},
    private val onClick: (ClassRoom) -> Unit
) : RecyclerView.Adapter<ClassAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name = v.findViewById<TextView>(R.id.tvName)
        val status = v.findViewById<TextView>(R.id.tvStatus)
        val capacity = v.findViewById<TextView>(R.id.tvCapacity)
        val facilities = v.findViewById<TextView>(R.id.tvFacilities)
        val btnEdit = v.findViewById<ImageView>(R.id.btnEditRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.name.text = data.name
        holder.status.text = data.status
        holder.capacity.text = "Kapasitas: ${data.capacity}"
        holder.facilities.text = data.facilities

        // Warna status
        if (data.status == "Kosong" || data.status == "Tersedia") {
            holder.status.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.status.setTextColor(Color.RED)
        }

        // Tampilkan tombol edit hanya jika Admin
        if (isAdmin) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick(data) }
        } else {
            holder.btnEdit.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onClick(data)
        }
    }
}
