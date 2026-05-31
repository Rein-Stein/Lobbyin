package com.example.lobbyin.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.lobbyin.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private var isDosen = false
    private var isAdmin = false
    private var username = ""

    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        isDosen = intent.getBooleanExtra("IS_DOSEN", false)
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
        username = intent.getStringExtra("USERNAME") ?: "User"

        findViewById<TextView>(R.id.tvUsername).text = username

        findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToLogin()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToLogin()
            }
        })

        findViewById<View>(R.id.btnHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("IS_DOSEN", isDosen)
            intent.putExtra("IS_ADMIN", isAdmin)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnSeeClasses).setOnClickListener {
            navigateToDatePicker()
        }

        findViewById<View>(R.id.btnGedungF).setOnClickListener {
            navigateToDatePicker()
        }

        cleanupOldBookings()
        updateRoomCount()
    }

    private fun cleanupOldBookings() {
        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val bookingsRef = database.getReference("bookings")
        
        // Format harus sama persis dengan yang disimpan di database
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        
        // Ambil waktu hari ini pada jam 00:00:00 untuk perbandingan tanggal saja
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayDate = calendar.time

        bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookingSnapshot in snapshot.children) {
                    val dateStr = bookingSnapshot.child("date").getValue(String::class.java)
                    if (dateStr != null) {
                        try {
                            val bookingDate = sdf.parse(dateStr)
                            // Jika tanggal booking sebelum hari ini, hapus dari database
                            if (bookingDate != null && bookingDate.before(todayDate)) {
                                bookingSnapshot.ref.removeValue()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        setCurrentDate()
    }

    private fun updateRoomCount() {
        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val roomsRef = database.getReference("rooms")
        val bookingsRef = database.getReference("bookings")
        
        val queryFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val todayDate = queryFormat.format(Date())
        
        var totalRoomsCount = 0
        val allRoomNames = mutableSetOf<String>()
        val bookedRoomsToday = mutableSetOf<String>()

        val updateUI = {
            findViewById<TextView>(R.id.tvRoomsCount).text = "$totalRoomsCount Kelas"
            // Only count booked rooms that still exist
            val currentlyBooked = bookedRoomsToday.intersect(allRoomNames).size
            val availableRooms = totalRoomsCount - currentlyBooked
            findViewById<TextView>(R.id.tvRoomsAvailable).text = "$availableRooms Tersedia"
        }

        roomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalRoomsCount = 0
                allRoomNames.clear()
                for (gedungSnapshot in snapshot.children) {
                    totalRoomsCount += gedungSnapshot.childrenCount.toInt()
                    for (roomSnapshot in gedungSnapshot.children) {
                        val roomName = roomSnapshot.child("name").getValue(String::class.java)
                        if (roomName != null) allRoomNames.add(roomName)
                    }
                }
                updateUI()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookedRoomsToday.clear()
                for (bSnap in snapshot.children) {
                    val b = bSnap.child("room").getValue(String::class.java)
                    val d = bSnap.child("date").getValue(String::class.java)
                    if (b != null && d == todayDate) {
                        bookedRoomsToday.add(b)
                    }
                }
                updateUI()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setCurrentDate() {
        val tvDateToday = findViewById<TextView>(R.id.tvDateToday)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val currentDate = dateFormat.format(Date())
        tvDateToday.text = currentDate
    }

    private fun navigateToDatePicker() {
        val intent = Intent(this, DatePickerActivity::class.java)
        intent.putExtra("nama_gedung", "Gedung F")
        intent.putExtra("IS_DOSEN", isDosen)
        intent.putExtra("IS_ADMIN", isAdmin)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
