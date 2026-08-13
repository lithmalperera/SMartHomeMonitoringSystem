package com.smarthome.monitor.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthWeek(val label: String, val keys: Set<String>)

object DateUtils {

    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun todayKey(): String = keyFormat.format(Date())

    fun lastNDays(n: Int): List<String> {
        val calendar = Calendar.getInstance()
        val keys = mutableListOf<String>()
        repeat(n) { offset ->
            val date = calendar.time
            keys.add(keyFormat.format(date))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return keys.reversed()
    }

    fun shortDayLabel(dateKey: String): String {
        val date = keyFormat.parse(dateKey) ?: return dateKey
        return dayLabelFormat.format(date)
    }

    fun monthWeekGroups(): List<MonthWeek> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val keys = mutableListOf<String>()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        while (calendar.get(Calendar.MONTH) == currentMonth) {
            keys.add(keyFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val groups = mutableListOf<MonthWeek>()
        keys.chunked(7).forEachIndexed { index, chunk ->
            groups.add(MonthWeek(label = "W${index + 1}", keys = chunk.toSet()))
        }
        return groups
    }

    fun formatDuration(minutes: Long): String = when {
        minutes <= 0 -> "0m"
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0L -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }

    fun formatEnergy(wh: Double): String = when {
        wh <= 0 -> "0 Wh"
        wh < 1000 -> "${String.format(Locale.US, "%.1f", wh)} Wh"
        else -> "${String.format(Locale.US, "%.2f", wh / 1000)} kWh"
    }

    fun timeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val difference = now - timestamp
        return when {
            difference < 0 -> "Just now"
            difference < 60000 -> "Just now"
            difference < 3600000 -> "${difference / 60000}m ago"
            difference < 86400000 -> "${difference / 3600000}h ago"
            else -> "${difference / 86400000}d ago"
        }
    }

    fun fullTime(timestamp: Long): String {
        val format = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
