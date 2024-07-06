package com.example.StockDrinks

import com.example.StockDrinks.Controller.JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.StockDrinks.Adapters.DailyDriksAdapter
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.DailyDrinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class dailyDrinks : AppCompatActivity() {
    private lateinit var drinkList: ListView
    private lateinit var addCaloriesButton: Button
    private lateinit var addNewFoodButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_drinks)
        drinkList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addDrinkFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        setDailyDrinkList()
        addCaloriesButton.setOnClickListener {
            callFormDailyDrinks()
        }

        addNewFoodButton.setOnClickListener {
            callDrinkForm()
        }
    }
    // onResume
    override fun onResume() {
        super.onResume()
        setDailyDrinkList()
    }

    fun callFormDailyDrinks() {
        try {
            startActivity(Intent(this, formDailyDrinks::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    fun callDrinkForm() {
        try {
            startActivity(Intent(this, formDrinks::class.java))
        } catch (e: Exception) {
            println(getString(R.string.error_calling_drinks_screen)+": $e")
        }
    }

    fun setDailyDrinkList() {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            val jsonUtil = JSON()
            var dailyDrinksList: List<DailyDrinks> = emptyList() // Inicialize a lista como vazia
            try {
                // Verifique se o cache não é nulo antes de acessá-lo
                if (cache != null && cache.hasCache(this@dailyDrinks, "dailyDrinks")) {
                    // Obtenha a lista de calorias diárias do cache
                    val dailyDrinksListJson = cache.getCache(this@dailyDrinks, "dailyDrinks")
                    println("Lista de calorias diárias: $dailyDrinksListJson")
                    dailyDrinksList = jsonUtil.fromJson(dailyDrinksListJson, Array<DailyDrinks>::class.java).toList()
                } else {
                    val dailyDrinksListJson = cache.getCache(this@dailyDrinks, "emptyDailyCalories")
                    println("Lista de calorias diárias: $dailyDrinksListJson")
                    dailyDrinksList = jsonUtil.fromJson(dailyDrinksListJson, Array<DailyDrinks>::class.java).toList()
                }
                // Ordenar a lista por data (ano -> mês -> dia)
                dailyDrinksList = dailyDrinksList.sortedByDescending { dailyCalories ->
                    val dateParts = dailyCalories.date.split("/")
                    // Converter a data para o formato YYYYMMDD para ordenação correta
                    "${dateParts[2]}${dateParts[1]}${dateParts[0]}".toInt()
                }
                // Atualize a UI na thread principal
                launch(Dispatchers.Main) {
                    // Crie um adapter para a lista de objetos DailyDrinks
                    val adapter = DailyDriksAdapter(this@dailyDrinks, dailyDrinksList)
                    // Atribua o adapter à lista de alimentos
                    drinkList.adapter = adapter
                }
            } catch (e: Exception) {
                println("Erro ao carregar a lista de calorias diárias: $e")
            }
        }
    }


}