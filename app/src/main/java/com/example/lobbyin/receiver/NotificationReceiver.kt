package com.example.lobbyin.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lobbyin.R
import com.example.lobbyin.activity.HistoryActivity
import com.example.lobbyin.model.Booking
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "lobbyin_reminder_channel"
    private val FIREBASE_URL = "https://lobbyin-f8230-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onReceive(context: Context, intent: Intent) {
        val bookingDate = intent.getStringExtra("booking_date") ?: return
        val isImmediate = intent.getBooleanExtra("is_immediate", false)
        
        if (isImmediate) {
            showImmediateNotification(context, bookingDate)
        } else {
            // Fetch all bookings for this date to show a summary
            fetchAndShowNotification(context, bookingDate)
        }
    }

    private fun showImmediateNotification(context: Context, date: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lobbyin Booking Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi konfirmasi booking"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, HistoryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("IS_DOSEN", false)
            putExtra("IS_ADMIN", false)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Booking Berhasil"
        val contentText = "Anda telah memboking kelas pada tanggal $date"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_lobbyin)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify((date + "_immediate").hashCode(), builder.build())
    }

    private fun fetchAndShowNotification(context: Context, date: String) {
        val database = FirebaseDatabase.getInstance(FIREBASE_URL)
        val bookingsRef = database.getReference("bookings")

        bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todayBookings = mutableListOf<Booking>()
                for (bookingSnapshot in snapshot.children) {
                    val b = bookingSnapshot.getValue(Booking::class.java)
                    if (b != null && b.date == date) {
                        todayBookings.add(b)
                    }
                }

                if (todayBookings.isNotEmpty()) {
                    showNotification(context, date, todayBookings)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showNotification(context: Context, date: String, bookings: List<Booking>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lobbyin Booking Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk jadwal booking hari ini"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, HistoryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("IS_DOSEN", false)
            putExtra("IS_ADMIN", false)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val firstBooking = bookings[0]
        val title = "Jadwal Kelas Hari Ini"
        val contentText = if (bookings.size == 1) {
            "Hari ini ada kelas di ${firstBooking.room} pada jam ${firstBooking.time} - ${firstBooking.matkul}"
        } else {
            "Hari ini ada ${bookings.size} jadwal kelas. Klik untuk detail."
        }

        val bigText = StringBuilder()
        bookings.forEach {
            bigText.append("• ${it.room} (${it.time}): ${it.matkul}\n")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_lobbyin)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText.toString().trim()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Gunakan hashcode tanggal agar notifikasi untuk hari yang sama saling menimpa/update, bukan menumpuk
        notificationManager.notify(date.hashCode(), builder.build())
    }
}
