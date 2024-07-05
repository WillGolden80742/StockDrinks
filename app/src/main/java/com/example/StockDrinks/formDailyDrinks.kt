package com.example.StockDrinks

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import com.example.StockDrinks.Adapters.DrinksAdapter
import com.example.StockDrinks.Controller.JSON
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.DailyDrinks
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.dailyDrinksList.Companion.setDrinkList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class formDailyDrinks : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listFoodsView: ListView
    private lateinit var nameDrinkLabel: TextView
    private var dailyDrinks: DailyDrinks = DailyDrinks()
    private lateinit var editTextDate: TextView
    private lateinit var seeDrinksButton: Button
    private lateinit var removeDailyDrinksButton: Button
    private lateinit var quantityEditText: EditText
    private lateinit var addDrinkButton: Button
    private lateinit var saveCaloriesButton: Button
    private var currentDrink: Drink? = null
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300
    private var lastClickTime: Long = 0
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_daily_drinks)

        // Inicializar os elementos do layout
        searchEditText = findViewById(R.id.searchDrinkEditText)
        searchButton = findViewById(R.id.searchDrinkButton)
        listFoodsView = findViewById(R.id.listDrinksView)
        nameDrinkLabel = findViewById(R.id.nameDrinkLabel)
        editTextDate = findViewById(R.id.editTextDate)
        quantityEditText = findViewById(R.id.quantityEditText)
        addDrinkButton = findViewById(R.id.addDrinkButton)
        seeDrinksButton = findViewById(R.id.seeDrinksButton)
        removeDailyDrinksButton = findViewById(R.id.removeDailyDrinksButton)
        saveCaloriesButton = findViewById(R.id.saveDrinksButton)

        loading()
        //getInputExtra
        getDailyDrinks()

        searchButton.setOnClickListener {
            searchDrink(searchEditText.text.toString())
            hideKeyboard(this.currentFocus ?: View(this))
        }

        addDrinkButton.setOnClickListener {
            try {
                saveCaloriesButton.isEnabled = true
                addDrinkToDailyList()
            } catch (e: Exception) {
                Toast.makeText(this, "Error adding food to daily list", Toast.LENGTH_SHORT).show()
            }
        }

        saveCaloriesButton.setOnClickListener {
            saveDailyCalories()
        }



        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchDrink(searchEditText.text.toString())
            hideKeyboard(this.currentFocus ?: View(this))
        }

        searchEditText.setOnClickListener {
            searchEditText.setText("")
        }

        seeDrinksButton.setOnClickListener {
            callDailyDrinksList()
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        editTextDate.setOnClickListener {
            // Cria um DatePickerDialog para selecionar a data
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Define a data selecionada no EditText
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear" // Mês é base 0, por isso adicionamos 1
                    if (selectedDate != editTextDate.text) {
                        // do format 00/00/0000
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
                        editTextDate.text = formattedDate
                        // get by cache if selectedDate exists
                        getDailyDrinksByDate(formattedDate.toString())
                    }
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        removeDailyDrinksButton.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Duplo clique detectado
                removeDailyDrinks()
            } else {
                Toast.makeText(this, "Clique duas vez rápido para exclusão", Toast.LENGTH_SHORT).show()
            }

            lastClickTime = clickTime
        }

        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    // Remove o listener temporariamente para evitar loops infinitos
                    quantityEditText.removeTextChangedListener(this)

                    val input = it.toString()
                    val cleanInput = StringBuilder()
                    var hasSpecialChar = false

                    for (char in input) {
                        if (char.isDigit()) {
                            cleanInput.append(char)
                        } else if (!hasSpecialChar && (char == '*' || char == 'x' || char == 'X' || char == '+')) {
                            cleanInput.append(char)
                            hasSpecialChar = true
                        }
                    }

                    // Atualiza o texto com a versão limpa
                    quantityEditText.setText(cleanInput.toString())
                    quantityEditText.setSelection(cleanInput.length)

                    // Reanexa o listener
                    quantityEditText.addTextChangedListener(this)
                }
            }
        })

        quantityEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                val input = quantityEditText.text.toString()
                quantityEditText.setText(calcule(input))
                hideKeyboard(this.currentFocus ?: View(this))
                true  // Indica que o evento foi tratado
            } else {
                false  // Deixa o evento continuar
            }
        }

    }


    override fun onResume() {
        super.onResume()
        setFoodToFoodList()
        if (dailyDrinksList.getDrinkList().size !== dailyDrinks.drinkList.size) {
            dailyDrinks.drinkList = dailyDrinksList.getDrinkList()
            if (dailyDrinks.drinkList.isEmpty()) {
                seeDrinksButton.isEnabled = false
            }
            currentDrink = null
        }
    }

    fun calcule (input: String):String {
        val regexAdd = Regex("\\+")
        val regexMultiply = Regex("[*xX]")
        try {
            val result = when {
                regexAdd.containsMatchIn(input) -> {
                    val parts = input.split(regexAdd)
                    val firstPart = parts.getOrNull(0)?.trim()?.toDouble() ?: 0.0
                    val secondPart = parts.getOrNull(1)?.trim()?.toDouble() ?: 0.0
                    Toast.makeText(
                        this,
                        "Resultado de ${firstPart.toInt()} + ${secondPart.toInt()} = ${(firstPart + secondPart).toInt()}",
                        Toast.LENGTH_LONG
                    ).show()
                    firstPart + secondPart
                }

                regexMultiply.containsMatchIn(input) -> {
                    val parts = input.split(regexMultiply)
                    val firstPart = parts.getOrNull(0)?.trim()?.toDouble()
                        ?: 1.0 // Se não houver, assume 1 para multiplicação
                    val secondPart = parts.getOrNull(1)?.trim()?.toDouble()
                        ?: 1.0 // Se não houver, assume 1 para multiplicação
                    Toast.makeText(
                        this,
                        "Resultado de ${firstPart.toInt()} x ${secondPart.toInt()} = ${(firstPart * secondPart).toInt()}",
                        Toast.LENGTH_LONG
                    ).show()
                    firstPart * secondPart
                }

                else -> throw IllegalArgumentException("Operador não suportado na entrada: $input")
            }
            return result.toInt().toString()
        } catch (e: Exception) {
            return input.replace("[^0-9]".toRegex(), "")
        }

    }

    fun calculeQuantity() {
        quantityEditText.setText(calcule(quantityEditText.text.toString()))
    }
    fun loading() {
        var drinkList = emptyList<Drink>()
        val drink = Drink()
        drinkList = drinkList.plus(drink)
        listFoodsView.adapter = DrinksAdapter(this,drinkList,"loading")
    }
    fun hideKeyboard (view: View): Boolean {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        return true
    }

    fun getDailyDrinksByDate(selectedDate: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            if (cache.hasCache(this@formDailyDrinks, "dailyDrinks")) {
                val dailyCaloriesListJson = cache.getCache(this@formDailyDrinks, "dailyDrinks")
                val jsonUtil = JSON()
                val dailyDrinksList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java).toList()
                val dailyCaloriesListFiltered = dailyDrinksList.filter { it.date == selectedDate }
                withContext(Dispatchers.Main) {
                    if (dailyCaloriesListFiltered.isNotEmpty()) {
                        dailyDrinks = dailyCaloriesListFiltered[0]
                        dailyDrinksList.run { setDrinkList(dailyDrinks.drinkList) }
                    } else {
                        dailyDrinks = DailyDrinks()
                        dailyDrinks.date = selectedDate
                        dailyDrinksList.run { setDrinkList(dailyDrinks.drinkList) }
                    }
                    // se a lista de alimentos estiver vazia, desabilita o botão
                    seeDrinksButton.isEnabled = dailyDrinks.drinkList.isNotEmpty()
                }
            }
        }
    }

    fun getDailyDrinks() {
        if (intent.hasExtra("dailyCaloriesDate")) {
            val selectedDate = intent.getStringExtra("dailyCaloriesDate")
            editTextDate.text = selectedDate
            getDailyDrinksByDate(selectedDate.toString())
        } else {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            editTextDate.text = formattedDate
            getDailyDrinksByDate(formattedDate)
        }
    }

    fun callDailyDrinksList() {
        try {
            var dailyDrinksList = Intent(this, dailyDrinksList::class.java)
            var jsonUtil = JSON()
            dailyDrinksList.putExtra("foodsList", dailyDrinks.drinkList.let { jsonUtil.toJson(it) })
            startActivity(dailyDrinksList)
        } catch (e: Exception) {
            println(RuntimeException("Error calling daily calories foods: $e"))
        }
    }
    fun setFoodToFoodList() {
        searchDrink("")
    }
    fun selectedFood(drink: Drink) {
        try {
            addDrinkButton.isEnabled = true
            quantityEditText.isEnabled = true
            currentDrink = drink
            nameDrinkLabel.text = currentDrink!!.foodDescription
        } catch (e: Exception) {
            println(RuntimeException("Error handling drink click: $e"))
        }
    }
    fun searchDrink(value: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val jsonUtil = JSON()
            val cache = Cache()
            try {
                val drinkNutritionList: List<Drink>
                if (cache.hasCache(this@formDailyDrinks, "Alimentos")) {
                    drinkNutritionList = jsonUtil.fromJson(cache.getCache(this@formDailyDrinks, "Alimentos"), Array<Drink>::class.java).toList()
                } else {
                    val jsonContent = withContext(Dispatchers.IO) {
                        resources.openRawResource(R.raw.nutritional_table).bufferedReader()
                            .use { it.readText() }
                    }
                    drinkNutritionList = jsonUtil.fromJson(jsonContent, Array<Drink>::class.java).toList()
                    cache.setCache(this@formDailyDrinks, "Alimentos", jsonContent)
                }

                val filteredList = if (value.isEmpty()) {
                    drinkNutritionList
                } else {
                    drinkNutritionList.filter { it.foodDescription.contains(value, ignoreCase = true) }
                }

                val adapter = DrinksAdapter(this@formDailyDrinks, filteredList)
                listFoodsView.adapter = adapter
            } catch (e: Exception) {
                println(RuntimeException("Erro ao ler o arquivo JSON: $e"))
            }
        }
    }

    fun addDrinkToDailyList() {
        seeDrinksButton.isEnabled = true
        if (currentDrink !== null) {
            try {
                currentDrink?.let { drink ->
                    calculeQuantity()
                    quantityEditText.text.toString().toDoubleOrNull()?.let { quantity ->
                        drink.quantity = quantity
                        drink.updateTime()
                        dailyDrinks.date = editTextDate.text.toString()
                        dailyDrinks.addFood(drink)
                        currentDrink = null
                        quantityEditText.setText("1")
                        quantityEditText.isEnabled = false
                        Toast.makeText(this,
                            getString(R.string.drink_added_to_daily_list), Toast.LENGTH_SHORT).show()
                        nameDrinkLabel.text = getString(R.string.select_drink)
                    } ?: run {
                        quantityEditText.error = "Invalid input"
                    }
                } ?: run {
                    throw RuntimeException("Current food is null")
                }
            } catch (e: Exception) {
                println(RuntimeException("Error adding food to daily list: $e"))
            }
        }
    }

    fun saveDailyCalories() {
        addDrinkToDailyList()
        val dailyCaloriesFoodsList =
            dailyDrinks.drinkList.filter { it.foodDescription != "NO_DESCRIPTION" }
        if (dailyCaloriesFoodsList.isNotEmpty() || currentDrink != null) {
            val cache = Cache()
            val jsonUtil = JSON()
            try {
                var dailyDrinksLists: List<DailyDrinks> =
                    if (cache.hasCache(this, "dailyDrinks")) {
                        val dailyCaloriesListJson = cache.getCache(this, "dailyDrinks")
                        jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java)
                            .toList()
                    } else {
                        emptyList()
                    }
                val formattedDate = editTextDate.text.toString()
                val dailyCaloriesListFiltered =
                    dailyDrinksLists.filter { it.date == formattedDate }
                dailyDrinksLists = dailyDrinksLists.minus(dailyCaloriesListFiltered)
                dailyDrinks.drinkList = dailyCaloriesFoodsList
                dailyDrinksLists = dailyDrinksLists.plus(dailyDrinks)
                cache.setCache(this, "dailyDrinks", jsonUtil.toJson(dailyDrinksLists))
                Toast.makeText(
                    this,
                    getString(R.string.daily_drinks_saved_successfully), Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                println(RuntimeException("Error saving daily calories: $e"))
            }
            finish()
        } else {
            removeDailyDrinks()
        }
    }

    fun removeDailyDrinks() {
        val currentDate = dailyDrinks.date
        var cache = Cache()
        if (cache.hasCache(this, "dailyDrinks")) {
            val dailyCaloriesListJson = cache.getCache(this, "dailyDrinks")
            val jsonUtil = JSON()
            var dailyDrinksList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java).toList()
            val dailyCaloriesListFiltered = dailyDrinksList.filter { it.date == currentDate }
            if (dailyCaloriesListFiltered.isNotEmpty()) {
                dailyDrinksList = dailyDrinksList.minus(dailyCaloriesListFiltered)
                cache.setCache(this, "dailyDrinks", jsonUtil.toJson(dailyDrinksList))
                Toast.makeText(this,
                    getString(R.string.daily_drinks_removed_successfully), Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

}
