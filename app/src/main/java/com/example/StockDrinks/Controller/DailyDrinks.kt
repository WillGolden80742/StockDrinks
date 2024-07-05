package com.example.StockDrinks.Controller

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyDrinks {
    var date: String = ""
    var drinkList: List<Drink> = listOf()
    fun addFood(food: Drink) {
        drinkList = drinkList.plus(food)
    }
    init {
        if (date == "") {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            date = formattedDate
        }
    }

}
