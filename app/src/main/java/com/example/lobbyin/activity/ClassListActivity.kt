package com.example.lobbyin.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lobbyin.R
import com.example.lobbyin.adapter.ClassAdapter
import com.example.lobbyin.model.Booking
import com.example.lobbyin.model.ClassRoom
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ClassListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClassAdapter
    private val list = ArrayList<ClassRoom>()
    private var selectedDate = ""
    private var namaGedung = ""
    private var kodeGedung = ""
    private var currentFloor = 1
    private var isAdmin = false
    private var loadJob: Job? = null
    
    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_list)

        selectedDate = intent.getStringExtra("tanggal") ?: ""
        namaGedung = intent.getStringExtra("nama_gedung") ?: "Gedung F"
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
        
        kodeGedung = if (namaGedung.contains(" ")) {
            "G" + namaGedung.split(" ")[1].uppercase()
        } else {
            "GF"
        }

        findViewById<TextView>(R.id.tvTitle).text = namaGedung.uppercase()
        findViewById<TextView>(R.id.tvDate).text = selectedDate

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = ClassAdapter(
            list,
            isAdmin = isAdmin,
            onEditClick = { room ->
                val intent = Intent(this, AdminRoomActivity::class.java)
                intent.putExtra("ROOM_ID", room.id)
                intent.putExtra("GEDUNG_CODE", kodeGedung)
                intent.putExtra("NAME", room.name)
                intent.putExtra("CAPACITY", room.capacity)
                intent.putExtra("FACILITIES", room.facilities)
                intent.putExtra("FLOOR", room.floor)
                startActivity(intent)
            },
            onClick = {
                val intent = Intent(this, TimeSelectionActivity::class.java)
                intent.putExtra("room", it.name)
                intent.putExtra("tanggal", selectedDate)
                intent.putExtra("capacity", it.capacity)
                intent.putExtra("facilities", it.facilities)
                intent.putExtra("floor", it.floor)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        val fabAddRoom = findViewById<FloatingActionButton>(R.id.fabAddRoom)
        if (isAdmin) {
            fabAddRoom.visibility = View.VISIBLE
            fabAddRoom.setOnClickListener {
                val intent = Intent(this, AdminRoomActivity::class.java)
                intent.putExtra("GEDUNG_CODE", kodeGedung)
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToHome()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })

        findViewById<MaterialButton>(R.id.btnL1).setOnClickListener { 
            currentFloor = 1
            loadFloor(1) 
        }
        findViewById<MaterialButton>(R.id.btnL2).setOnClickListener { 
            currentFloor = 2
            loadFloor(2) 
        }
        findViewById<MaterialButton>(R.id.btnL3).setOnClickListener { 
            currentFloor = 3
            loadFloor(3) 
        }

        loadFloor(1)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadFloor(currentFloor) 
    }

    private fun updateFloorButtons(floor: Int) {
        val btn1 = findViewById<MaterialButton>(R.id.btnL1)
        val btn2 = findViewById<MaterialButton>(R.id.btnL2)
        val btn3 = findViewById<MaterialButton>(R.id.btnL3)

        val buttons = listOf(btn1, btn2, btn3)
        buttons.forEachIndexed { index, button ->
            val isSelected = (index + 1) == floor
            if (isSelected) {
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy_primary))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.strokeWidth = 0
            } else {
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))
                button.setTextColor(ContextCompat.getColor(this, R.color.navy_primary))
                button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy_primary))
                button.strokeWidth = dpToPx(1)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun loadFloor(floor: Int) {
        updateFloorButtons(floor)
        loadJob?.cancel()
        
        loadJob = lifecycleScope.launch {
            val database = FirebaseDatabase.getInstance(FIREBASE_URL)
            val roomsRef = database.getReference("rooms").child(kodeGedung)
            val bookingsRef = database.getReference("bookings")

            roomsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomsFromDb = mutableListOf<ClassRoom>()
                    for (roomSnapshot in snapshot.children) {
                        val room = roomSnapshot.getValue(ClassRoom::class.java)
                        if (room != null && room.floor == floor) {
                            roomsFromDb.add(room)
                        }
                    }

                    bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(bookingSnapshot: DataSnapshot) {
                            val bookedRoomsToday = mutableSetOf<String>()
                            for (bSnap in bookingSnapshot.children) {
                                val b = bSnap.child("room").getValue(String::class.java)
                                val d = bSnap.child("date").getValue(String::class.java)
                                if (b != null && d == selectedDate) {
                                    bookedRoomsToday.add(b)
                                }
                            }

                            for (room in roomsFromDb) {
                                room.status = if (bookedRoomsToday.contains(room.name)) "Terisi" else "Kosong"
                            }
                            
                            list.clear()
                            list.addAll(roomsFromDb)
                            adapter.notifyDataSetChanged()
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}