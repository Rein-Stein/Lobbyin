package com.example.lobbyin.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.lobbyin.R
import java.text.SimpleDateFormat
import java.util.*

class DatePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        val namaGedung = intent.getStringExtra("nama_gedung") ?: "Gedung F"
        val isDosen = intent.getBooleanExtra("IS_DOSEN", false)
        val isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val btn = findViewById<View>(R.id.btnNext)
        val btnBack = findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btn.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val tanggal = format.format(calendar.time)

            val intent = Intent(this, ClassListActivity::class.java)
            intent.putExtra("tanggal", tanggal)
            intent.putExtra("nama_gedung", namaGedung)
            intent.putExtra("IS_DOSEN", isDosen)
            intent.putExtra("IS_ADMIN", isAdmin)
            startActivity(intent)
        }
    }
}
