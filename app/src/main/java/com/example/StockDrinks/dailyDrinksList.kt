package com.example.StockDrinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.StockDrinks.Adapters.DrinksAdapter
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.DailyDrinks
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.Controller.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class dailyDrinksList : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
    private lateinit var searchDrinkEditText: EditText
    private lateinit var searchDrinkButton: Button
    private var dailyCaloriesDate = ""
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
            var dailyCaloriesDate = intent.getStringExtra("dailyCaloriesDate")
            this.dailyCaloriesDate = dailyCaloriesDate.toString()
        } catch (e: Exception) {
            println("Error loading food list: $e")
        }

    }

    private fun searchDrink (drinkName: String) {
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
        saveDailyDrinks()
        if (drinkList.isEmpty()) {
            finish()
        }
    }

    private fun saveDailyDrinks() {
        val cache = Cache()
        val jsonUtil = JSON()
        val dailyDrinks = DailyDrinks()
        dailyDrinks.date = this.dailyCaloriesDate
        dailyDrinks.drinkList = getDrinkList()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val dailyDrinksLists = loadDailyDrinks(cache, jsonUtil)
                val updatedDailyDrinksLists = updateDailyDrinksList(dailyDrinksLists, dailyDrinks)

                cache.setCache(this@dailyDrinksList, "dailyDrinks", jsonUtil.toJson(updatedDailyDrinksLists))
                cache.setCache(this@dailyDrinksList, "dailyDrinksUpdated${dailyDrinks.date}", "")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println(RuntimeException(getString(R.string.error_saving_daily_calories) + ":$e"))
                }
            }
        }
    }

    private fun loadDailyDrinks(cache: Cache, jsonUtil: JSON): List<DailyDrinks> {
        return if (cache.hasCache(this@dailyDrinksList, "dailyDrinks")) {
            val dailyCaloriesListJson = cache.getCache(this@dailyDrinksList, "dailyDrinks")
            jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java).toList()
        } else {
            emptyList()
        }
    }

    private fun updateDailyDrinksList(
        dailyDrinksLists: List<DailyDrinks>,
        dailyDrinks: DailyDrinks
    ): List<DailyDrinks> {
        val existingDailyDrinks = dailyDrinksLists.find { it.date == dailyDrinks.date }
        return if (existingDailyDrinks != null) {
            if (dailyDrinks.drinkList.isEmpty()) {
                dailyDrinksLists - existingDailyDrinks // Remove if empty
            } else {
                dailyDrinksLists - existingDailyDrinks + dailyDrinks // Replace existing
            }
        } else {
            dailyDrinksLists + dailyDrinks // Add new
        }
    }


}