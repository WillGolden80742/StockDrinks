package com.example.StockDrinks.Controller

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.StockDrinks.R
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Drink (
    var foodNumber: String = "0",
    var quantity: Double = 1.0,
    var calcArray: List<String> = listOf(),
    var foodDescription: String = "NO_DESCRIPTION",
    var category: String = "NO_CATEGORY",
    var timeStamp: String = "00:00"
) {
    init {
        if (quantity == null) {
            quantity = 1.0
        }
    }

    @SuppressLint("NewApi")
    fun updateTime () {
        val currentTime = LocalTime.now()
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        timeStamp = formattedTime
    }
    fun toString(context: Context): String {
        val decimalFormat = DecimalFormat("#.##")
        val quantityLabel = context.getString(R.string.quantity)
        var calcLabel = "Calcs:\n"+calcArray.joinToString(",\n")+",\n"
        if (calcArray.isEmpty()) {
            calcLabel = ""
        }
        return context.getString(R.string.category_label)+": "+category+",\n"+quantityLabel + ": " + decimalFormat.format(quantity)+",\n"+calcLabel+"Time: "+timeStamp
    }
}