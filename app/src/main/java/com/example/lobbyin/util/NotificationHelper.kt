package com.example.lobbyin.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lobbyin.receiver.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    fun scheduleBookingNotification(context: Context, dateString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("booking_date", dateString)
        }

        // Use dateString as hashCode for unique PendingIntent per day
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dateString.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = parseDate(dateString)
        if (calendar != null) {
            // Set to 07:00 AM on the day of the booking
            calendar.set(Calendar.HOUR_OF_DAY, 7)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            // If the time has already passed for today, schedule it for 10 seconds later (for testing/demo)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                val sdfToday = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                if (dateString == sdfToday.format(Date())) {
                    // Only for today's booking, show it in 10 seconds
                    calendar.timeInMillis = System.currentTimeMillis() + 10000 
                    Log.d("NotificationHelper", "Today's time passed. Testing notification in 10s")
                } else {
                    return // It's a past date, don't schedule
                }
            }

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                Log.d("NotificationHelper", "Scheduled alarm for $dateString at 07:00 AM")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Failed to schedule alarm", e)
            }
        }
    }

    fun showImmediateBookingNotification(context: Context, dateString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("booking_date", dateString)
            putExtra("is_immediate", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (dateString + "_immediate").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5000 // 5 seconds later

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("NotificationHelper", "Scheduled immediate notification for $dateString in 5s")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to schedule immediate notification", e)
        }
    }

    fun cancelNotification(context: Context, dateString: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(dateString.hashCode())
    }

    private fun parseDate(dateString: String): Calendar? {
        // Format: EEEE, dd MMMM yyyy (e.g., Senin, 20 April 2026)
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        return try {
            val date = sdf.parse(dateString)
            if (date != null) {
                Calendar.getInstance().apply {
                    time = date
                }
            } else null
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error parsing date: $dateString", e)
            null
        }
    }
}
