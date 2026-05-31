package com.example.lobbyin.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lobbyin.R
import com.example.lobbyin.model.Booking
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TimeSelectionActivity : AppCompatActivity() {

    private lateinit var tvRoomName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvCapacity: TextView
    private lateinit var tvFacilities: TextView
    private lateinit var tvFloor: TextView
    private lateinit var rvTime: RecyclerView
    private lateinit var btnContinue: MaterialButton
    private lateinit var btnBack: ImageButton
    
    private var selectedTime: String? = null
    private var roomName: String = ""
    private var date: String = ""
    private var capacity: String = ""
    private var facilities: String = ""
    private var floor: Int = 1
    
    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_selection)

        roomName = intent.getStringExtra("room") ?: "GF-1.01"
        date = intent.getStringExtra("tanggal") ?: ""
        capacity = intent.getStringExtra("capacity") ?: "40 Orang"
        facilities = intent.getStringExtra("facilities") ?: "AC, LCD"
        floor = intent.getIntExtra("floor", 1)

        tvRoomName = findViewById(R.id.tvRoomName)
        tvDate = findViewById(R.id.tvDate)
        tvCapacity = findViewById(R.id.tvCapacity)
        tvFacilities = findViewById(R.id.tvFacilities)
        tvFloor = findViewById(R.id.tvFloor)
        rvTime = findViewById(R.id.rvTime)
        btnContinue = findViewById(R.id.btnContinue)
        btnBack = findViewById(R.id.btnBack)

        tvRoomName.text = roomName
        tvDate.text = date
        tvCapacity.text = capacity
        tvFacilities.text = facilities
        tvFloor.text = "Lantai $floor"

        rvTime.layoutManager = LinearLayoutManager(this)
        rvTime.isNestedScrollingEnabled = false
        
        loadTimeSlots()

        btnBack.setOnClickListener { finish() }

        btnContinue.setOnClickListener {
            if (selectedTime != null) {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("room", roomName)
                intent.putExtra("tanggal", date)
                intent.putExtra("waktu", selectedTime)
                intent.putExtra("capacity", capacity)
                intent.putExtra("facilities", facilities)
                intent.putExtra("floor", floor)
                startActivity(intent)
            }
        }
    }

    private fun loadTimeSlots() {
        val timeSlots = listOf(
            "07:30 - 09:30",
            "09:40 - 11:40",
            "13:00 - 15:00",
            "15:10 - 17:10"
        )

        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val bookingsRef = database.getReference("bookings")

        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookedTimes = mutableListOf<String>()
                for (bookingSnapshot in snapshot.children) {
                    val b = bookingSnapshot.getValue(Booking::class.java)
                    if (b != null && b.room == roomName && b.date == date) {
                        bookedTimes.add(b.time)
                    }
                }

                val slotStates = timeSlots.map { time ->
                    time to !bookedTimes.contains(time)
                }

                rvTime.adapter = TimeAdapter(slotStates) { time ->
                    selectedTime = time
                    btnContinue.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TimeSelectionActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class TimeAdapter(
        private val slots: List<Pair<String, Boolean>>,
        private val onTimeSelected: (String) -> Unit
    ) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

        private var selectedPosition: Int = -1

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTimeRange: TextView = view.findViewById(R.id.tvTimeRange)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val cardTimeSlot: MaterialCardView = view.findViewById(R.id.cardTimeSlot)
            val statusBadge: MaterialCardView = view.findViewById(R.id.statusBadge)
            val ivCheck: ImageView = view.findViewById(R.id.ivCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (time, isAvailable) = slots[position]
            holder.tvTimeRange.text = time
            
            if (isAvailable) {
                holder.tvStatus.text = "Kosong"
                holder.statusBadge.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.status_green))
                holder.cardTimeSlot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                
                if (selectedPosition == position) {
                    holder.cardTimeSlot.strokeWidth = 4
                    holder.cardTimeSlot.cardElevation = 8f
                    holder.ivCheck.visibility = View.VISIBLE
                    holder.statusBadge.visibility = View.GONE
                } else {
                    holder.cardTimeSlot.strokeWidth = 0
                    holder.cardTimeSlot.cardElevation = 2f
                    holder.ivCheck.visibility = View.GONE
                    holder.statusBadge.visibility = View.VISIBLE
                }

                holder.itemView.setOnClickListener {
                    val oldPosition = selectedPosition
                    selectedPosition = holder.adapterPosition
                    onTimeSelected(time)
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                }
            } else {
                holder.tvStatus.text = "Terisi"
                holder.statusBadge.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.status_red))
                holder.ivCheck.visibility = View.GONE
                holder.statusBadge.visibility = View.VISIBLE
                holder.cardTimeSlot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_border))
                holder.itemView.setOnClickListener(null)
            }
        }

        override fun getItemCount() = slots.size
    }
}
