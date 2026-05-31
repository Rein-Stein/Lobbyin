package com.example.lobbyin.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lobbyin.R
import com.example.lobbyin.model.Booking
import com.example.lobbyin.util.NotificationHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookingActivity : AppCompatActivity() {

    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Izin notifikasi ditolak. Anda tidak akan menerima pengingat.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        checkNotificationPermission()

        val room = intent.getStringExtra("room") ?: "GF-1.01"
        val date = intent.getStringExtra("tanggal") ?: "Senin, 20 April 2026"
        val time = intent.getStringExtra("waktu") ?: "08:00 - 10:00"
        val capacity = intent.getStringExtra("capacity") ?: "40 Orang"
        val facilities = intent.getStringExtra("facilities") ?: "AC, LCD"
        val floor = intent.getIntExtra("floor", 1)
        
        findViewById<TextView>(R.id.tvRoom).text = room
        findViewById<TextView>(R.id.tvDate).text = date
        findViewById<TextView>(R.id.tvTimeInfo).text = time
        findViewById<TextView>(R.id.tvCapacity).text = capacity
        findViewById<TextView>(R.id.tvFacilities).text = facilities
        findViewById<TextView>(R.id.tvFloor).text = "Lantai $floor"

        val etNama = findViewById<EditText>(R.id.etNama)
        val etNim = findViewById<EditText>(R.id.etNim)
        val etMatkul = findViewById<EditText>(R.id.etMatkul)
        val btnBooking = findViewById<View>(R.id.btnBooking)
        val btnBack = findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val bookingsRef = database.getReference("bookings")

        btnBooking.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val nim = etNim.text.toString().trim()
            val matkul = etMatkul.text.toString().trim()

            if (nama.isEmpty() || nim.isEmpty() || matkul.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Memproses booking...", Toast.LENGTH_SHORT).show()
            btnBooking.isEnabled = false

            bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isAlreadyBooked = false
                    for (bookingSnapshot in snapshot.children) {
                        val b = bookingSnapshot.getValue(Booking::class.java)
                        if (b != null && b.room == room && b.date == date && b.time == time) {
                            isAlreadyBooked = true
                            break
                        }
                    }

                    if (isAlreadyBooked) {
                        Toast.makeText(this@BookingActivity, "Sudah dibooking! Silakan pilih waktu lain.", Toast.LENGTH_LONG).show()
                        btnBooking.isEnabled = true
                    } else {
                        val bookingId = bookingsRef.push().key ?: ""
                        val newBooking = Booking(
                            id = bookingId,
                            room = room,
                            date = date,
                            time = time,
                            nama = nama,
                            nim = nim,
                            matkul = matkul
                        )

                        bookingsRef.child(bookingId).setValue(newBooking)
                            .addOnSuccessListener {
                                Toast.makeText(this@BookingActivity, "Booking berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                
                                // Schedule notifications
                                NotificationHelper.scheduleBookingNotification(this@BookingActivity, date)
                                NotificationHelper.showImmediateBookingNotification(this@BookingActivity, date)
                                
                                val intent = Intent(this@BookingActivity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@BookingActivity, "Gagal booking: ${it.message}", Toast.LENGTH_SHORT).show()
                                btnBooking.isEnabled = true
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BookingActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    btnBooking.isEnabled = true
                }
            })
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
