package ru.netology.inmedia.util

import android.annotation.SuppressLint
import android.widget.TextView
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.*

object DateFormatter {

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDate(textView: TextView, millisec: Long): String {
        val SimpleDateFormat = SimpleDateFormat("дд / ММ / гггг")
        val dateString = SimpleDateFormat.format(millisec)
        return dateString
    }

    fun formatDateToDateTimeString(date: Date): String {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
            .format(date)
    }

    fun formatDateToDateString(date: Date): String {
        return SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
            .format(date)
    }

    fun formatDateTimeStringToMillis(dateString: String): Long {
        val sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
        return sdf.parse(dateString)!!.run {
            time
        }
    }

    fun formatDateStringToMillis(dateString: String): Long {
        val sdf = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
        return sdf.parse(dateString)!!.run {
            time
        }
    }

}