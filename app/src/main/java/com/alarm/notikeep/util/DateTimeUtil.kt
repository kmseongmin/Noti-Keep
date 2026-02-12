package com.alarm.notikeep.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtil {
    private const val DEFAULT_PATTERN = "yyyy.MM.dd HH:mm"

    fun formatTimestamp(timestamp: Long, pattern: String = DEFAULT_PATTERN): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
