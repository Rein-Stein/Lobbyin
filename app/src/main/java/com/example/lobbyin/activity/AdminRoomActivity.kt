package com.example.lobbyin.activity

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lobbyin.R
import com.example.lobbyin.model.ClassRoom
import com.google.firebase.database.FirebaseDatabase

class AdminRoomActivity : AppCompatActivity() {
    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_room)

        val etName = findViewById<EditText>(R.id.etRoomName)
        val etCapacity = findViewById<EditText>(R.id.etCapacity)
        val etFacilities = findViewById<EditText>(R.id.etFacilities)
        val spFloor = findViewById<Spinner>(R.id.spFloor)
        val btnSave = findViewById<Button>(R.id.btnSaveRoom)
        val btnDelete = findViewById<Button>(R.id.btnDeleteRoom)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        val gedungCode = intent.getStringExtra("GEDUNG_CODE") ?: "GF"

        if (roomId.isNotEmpty()) {
            findViewById<TextView>(R.id.tvAdminTitle).text = "Edit Ruangan"
            etName.setText(intent.getStringExtra("NAME"))
            etCapacity.setText(intent.getStringExtra("CAPACITY"))
            etFacilities.setText(intent.getStringExtra("FACILITIES"))
            val floor = intent.getIntExtra("FLOOR", 1)
            spFloor.setSelection(floor - 1)
            btnDelete.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val cap = etCapacity.text.toString()
            val fac = etFacilities.text.toString()
            val floorStr = spFloor.selectedItem.toString()
            val floor = floorStr.filter { it.isDigit() }.toIntOrNull() ?: 1

            if (name.isEmpty() || cap.isEmpty() || fac.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbRef = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("rooms").child(gedungCode)
            val id = if (roomId.isEmpty()) dbRef.push().key ?: "" else roomId
            
            val room = ClassRoom(id, name, floor, "Kosong", "", cap, fac)
            dbRef.child(id).setValue(room).addOnCompleteListener {
                Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnDelete.setOnClickListener {
            FirebaseDatabase.getInstance(FIREBASE_URL).getReference("rooms")
                .child(gedungCode).child(roomId).removeValue()
                .addOnCompleteListener {
                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}