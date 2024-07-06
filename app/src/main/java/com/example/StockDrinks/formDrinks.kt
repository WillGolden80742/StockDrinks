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
    fun saveFood(action: Int) {
        try {
            val foodDescription = editTextName.text.toString()
            val category = categorySpinner.selectedItem.toString()
            // Verificar se todos os campos estão preenchidos
            if (foodDescription.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.all_fields_are_required),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val drink = Drink().apply {
                if (action == 1) { // Atualizar
                    foodNumber = currentDrink.foodNumber
                } else { // Criar
                    val random = Random().nextInt(100)
                    foodNumber = (System.currentTimeMillis()).toString()+random
                }
                if (foodDescription.isEmpty()) {
                    this.foodDescription = getString(R.string.drink_name)
                } else {
                    this.foodDescription = foodDescription
                }
                this.category = category
                this.quantity = 1.0
            }

            if (action == 1) { // Atualizar
                drinkNutritionList =
                    jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList().map {
                        if (it.foodNumber == currentDrink.foodNumber) {
                            drink
                        } else {
                            it
                        }
                    }
                Toast.makeText(
                    this,
                    getString(R.string.drink_saves_with_success),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (action == 0) { // Criar
                drinkNutritionList =
                    jsonUtil.fromJson(foodCache, Array<Drink>::class.java).toList() + drink
                // Concluir e enviar a lista de alimentos para a próxima atividade
                val intent = Intent(this, dailyDrinks::class.java)
                startActivity(intent)
                Toast.makeText(
                    this,
                    getString(R.string.drink_saves_with_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Salvar no cache
            cache.setCache(this, "Alimentos", jsonUtil.toJson(drinkNutritionList))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar o alimento", Toast.LENGTH_SHORT).show()
        }
    }


}
