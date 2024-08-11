package com.example.StockDrinks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.Controller.JSON
import java.util.Random

class formDrinks : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var addFoodFormButton: Button
    private lateinit var removeFoodFormButton: Button
    private lateinit var drinkNutritionList: List<Drink>
    private lateinit var currentDrink: Drink
    private var jsonUtil = JSON()
    private var cache = Cache()
    private var foodCache: String = ""
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_drinks)

        // Inicializando os componentes
        editTextName = findViewById(R.id.editTextName)
        categorySpinner = findViewById(R.id.categorySpinner)
        addFoodFormButton = findViewById(R.id.addDrinkFormButton)
        removeFoodFormButton = findViewById(R.id.removeDrinkFormButton)

        // if has extra, load the food
        if (intent.hasExtra("foodID")) {
            val foodID = intent.getStringExtra("foodID")
            addFoodFormButton.text = getString(R.string.update_drink)
            try {
                foodCache = cache.getCache(this, "Alimentos")
                if (foodCache != "NOT_FOUND") {
                    drinkNutritionList = jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList()
                } else {
                    // get R.raw.nutritional_table and add to cache
                    foodCache = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
                    cache.setCache(this, "Alimentos", foodCache)
                    drinkNutritionList = jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList()
                }
                currentDrink = drinkNutritionList.find { it.foodNumber == foodID.toString() }!!
                editTextName.setText(currentDrink.foodDescription)
                for (i in 0 until categorySpinner.adapter.count) {
                    // ignorecase
                    if (categorySpinner.adapter.getItem(i).toString().equals(currentDrink.category, ignoreCase = true)) {
                        categorySpinner.setSelection(i)
                        break
                    }
                }
            } catch (e: Exception) {
                // Toast para indicar que não foi possível carregar o alimento
                Toast.makeText(this,
                    getString(R.string.it_was_not_possible_to_load_the_drink), Toast.LENGTH_SHORT).show()
                System.out.println("Erro food: "+e)
            }
        } else {
            removeFoodFormButton.isVisible = false
            //if hash no cache create a cache to
            foodCache = cache.getCache(this, "Alimentos")
            if (foodCache == "NOT_FOUND") {
                // get R.raw.nutritional_table and add to cache
                foodCache = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
                cache.setCache(this, "Alimentos", foodCache)
            }
        }
        // Listener para o botão de adicionar food
        addFoodFormButton.setOnClickListener {
            if (intent.hasExtra("foodID")) {
                saveFood(1)
            } else {
                saveFood(0)
            }
        }
        // Listener para o botão de remove food
        removeFoodFormButton.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Duplo clique detectado
                if (intent.hasExtra("foodID")) {
                    removeFood()
                } else {
                    val intent = Intent(this, dailyDrinks::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Clique duas vez rápido para exclusão", Toast.LENGTH_SHORT).show()
            }
            lastClickTime = clickTime
        }
    }




    fun removeFood() {
        drinkNutritionList = jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList().filter { it.foodNumber != currentDrink.foodNumber }
        cache.setCache(this, "Alimentos", jsonUtil.toJson(drinkNutritionList))
        finish()
        Toast.makeText(this, getString(R.string.drink_removed_successively), Toast.LENGTH_SHORT).show()
    }

    // edit food
    private fun saveFood(action: Int) {
        try {
            val foodDescription = editTextName.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (foodDescription.isBlank()) {
                showToast(getString(R.string.all_fields_are_required))
                return
            }

            val drink = createDrink(action, foodDescription, category)

            when (action) {
                1 -> updateDrink(drink)
                0 -> createDrink(drink)
                else -> throw IllegalArgumentException("Invalid action: $action")
            }

            saveToCache(drinkNutritionList)
            finish()
        } catch (e: Exception) {
                showToast(getString(R.string.save_drink_error))
        }
    }

    private fun createDrink(action: Int, foodDescription: String, category: String): Drink {
        return Drink().apply {
            foodNumber = if (action == 1) {
                currentDrink.foodNumber
            } else {
                generateUniqueFoodNumber()
            }
            this.foodDescription = foodDescription.ifBlank { getString(R.string.drink_name) }
            this.category = category
            this.quantity = 1.0
        }
    }

    private fun generateUniqueFoodNumber(): String {
        val random = Random().nextInt(100)
        return "${System.currentTimeMillis()}$random"
    }

    private fun updateDrink(drink: Drink) {
        drinkNutritionList = jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList().map {
            if (it.foodNumber == currentDrink.foodNumber) drink else it
        }
        showToast(getString(R.string.drink_saves_with_success))
    }

    private fun createDrink(drink: Drink) {
        drinkNutritionList = jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList() + drink
        startActivity(Intent(this, dailyDrinks::class.java))
        showToast(getString(R.string.drink_saves_with_success))
    }

    private fun saveToCache(drinkNutritionList: List<Drink>) {
        cache.setCache(this, "Alimentos", jsonUtil.toJson(drinkNutritionList))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



}
