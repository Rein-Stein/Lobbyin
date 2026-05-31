package com.example.lobbyin.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lobbyin.R
import com.example.lobbyin.adapter.HistoryAdapter
import com.example.lobbyin.model.Booking
import com.example.lobbyin.model.Comment
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val allBookings = mutableListOf<Booking>()
    private val filteredList = mutableListOf<Booking>()
    private lateinit var emptyState: LinearLayout
    private var currentFilter = "TODAY"
    private var currentUsername: String? = null
    private var isDosen = false
    private var isAdmin = false
    
    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        isDosen = intent.getBooleanExtra("IS_DOSEN", false)
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
        currentUsername = intent.getStringExtra("USERNAME")

        val toolbar = findViewById<Toolbar>(R.id.toolbarHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        emptyState = findViewById(R.id.emptyState)
        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilter)
        
        // Set chipToday as checked by default
        findViewById<com.google.android.material.chip.Chip>(R.id.chipToday).isChecked = true

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                R.id.chipToday -> "TODAY"
                R.id.chipTomorrow -> "TOMORROW"
                else -> "ALL"
            }
            applyFilter()
        }

        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val bookingsRef = database.getReference("bookings")

        adapter = HistoryAdapter(
            list = filteredList, 
            isDosen = isDosen,
            isAdmin = isAdmin,
            onCancel = { booking: Booking ->
                bookingsRef.child(booking.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this@HistoryActivity, "Booking berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                        // Hapus notifikasi dari tray jika sedang muncul
                        com.example.lobbyin.util.NotificationHelper.cancelNotification(this@HistoryActivity, booking.date)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@HistoryActivity, "Gagal membatalkan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            onAddComment = { booking: Booking, commentText: String ->
                val role = if (isAdmin) "Admin" else "Dosen"
                val newComment = Comment(role, commentText)
                val updatedComments = booking.comments.toMutableList()
                updatedComments.add(newComment)
                
                bookingsRef.child(booking.id).child("comments").setValue(updatedComments)
                    .addOnSuccessListener {
                        Toast.makeText(this@HistoryActivity, "Komentar berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@HistoryActivity, "Gagal menambahkan komentar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            onDeleteComment = { booking: Booking, commentIndex: Int ->
                val updatedComments = booking.comments.toMutableList()
                if (commentIndex in updatedComments.indices) {
                    updatedComments.removeAt(commentIndex)
                    bookingsRef.child(booking.id).child("comments").setValue(updatedComments)
                        .addOnSuccessListener {
                            Toast.makeText(this@HistoryActivity, "Komentar berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@HistoryActivity, "Gagal menghapus komentar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        )
        rvHistory.adapter = adapter

        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allBookings.clear()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    if (booking != null) {
                        allBookings.add(booking)
                    }
                }
                allBookings.reverse()
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilter() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val todayStr = sdf.format(Date())
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = sdf.format(calendar.time)

        filteredList.clear()
        
        val baseList = allBookings // Menampilkan semua data terlebih dahulu untuk pengecekan

        when (currentFilter) {
            "TODAY" -> {
                filteredList.addAll(baseList.filter { it.date == todayStr })
                updateChipsText("Hari Ini ($todayStr)", "Besok")
            }
            "TOMORROW" -> {
                filteredList.addAll(baseList.filter { it.date == tomorrowStr })
                updateChipsText("Hari Ini", "Besok ($tomorrowStr)")
            }
            else -> {
                filteredList.addAll(baseList)
                updateChipsText("Hari Ini", "Besok")
            }
        }

        adapter.notifyDataSetChanged()
        emptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateChipsText(todayText: String, tomorrowText: String) {
        findViewById<com.google.android.material.chip.Chip>(R.id.chipToday).text = todayText
        findViewById<com.google.android.material.chip.Chip>(R.id.chipTomorrow).text = tomorrowText
    }
}
