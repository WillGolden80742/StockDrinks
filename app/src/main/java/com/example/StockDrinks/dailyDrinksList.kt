package com.example.StockDrinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.StockDrinks.Adapters.DrinksAdapter
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.Controller.JSON

class dailyDrinksList : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
    private lateinit var searchDrinkEditText: EditText
    private lateinit var searchDrinkButton: Button

    companion object {
        private var drinkList: List<Drink> = emptyList()

        fun getDrinkList(): List<Drink> {
            return drinkList
        }

        fun setDrinkList(list: List<Drink>) {
            drinkList = list
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_drinks_list)

        // Initialize views from layout
        listView = findViewById(R.id.listDrinksView)
        searchDrinkEditText = findViewById(R.id.searchDrinkEditText)
        searchDrinkButton = findViewById(R.id.searchDrinkButton)

        // Set click listener for search button
        searchDrinkButton.setOnClickListener {
            val drinkName = searchDrinkEditText.text.toString()
            searchDrink(drinkName)
        }

        searchDrinkEditText.setOnEditorActionListener { _, _, _ ->
            val drinkName = searchDrinkEditText.text.toString()
            searchDrink(drinkName)
            true
        }

        searchDrinkEditText.setOnClickListener {
            searchDrinkEditText.setText("")
        }

        try {
            // Get the food list from the intent
            searchDrink("")
        } catch (e: Exception) {
            println("Error loading food list: $e")
        }

    }

    fun searchDrink (drinkName: String) {
        if (intent.hasExtra("foodsList")) {
            val jsonUtil = JSON()
            setDrinkList(intent.getStringExtra("foodsList")?.let { jsonUtil.fromJson(it, Array<Drink>::class.java) }?.toList()!!)
            val filteredList =
                getDrinkList().filter { it.foodDescription.contains(drinkName, ignoreCase = true) }
            val adapter = DrinksAdapter(this, filteredList, "dailyDrinksList")
            listView.adapter = adapter
        }
    }

    fun removeFood(drink: Drink) {
        // get the selected drink and remove it from the list
        drinkList = drinkList.minus(drink)
        // update the list view
        val adapter = DrinksAdapter(this, drinkList,"dailyDrinksList")
        listView.adapter = adapter
        if (drinkList.isEmpty()) {
            finish()
        }
    }
}
