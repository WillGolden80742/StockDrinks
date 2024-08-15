package com.example.StockDrinks

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import com.example.StockDrinks.Adapters.DrinksAdapter
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.DailyDrinks
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.Controller.JSON
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
    private lateinit var saveDrinkButton: Button
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
        saveDrinkButton = findViewById(R.id.saveDrinkButton)
        seeDrinksButton = findViewById(R.id.seeDrinksButton)
        removeDailyDrinksButton = findViewById(R.id.removeDailyDrinksButton)

        loading()
        //getInputExtra
        getDailyDrinks()

        searchButton.setOnClickListener {
            searchDrink(searchEditText.text.toString())
            hideKeyboard(this.currentFocus ?: View(this))
        }

        saveDrinkButton.setOnClickListener {
            searchDrink(searchEditText.text.toString())
            saveDailyDrinks()
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
                    var cleanInput = StringBuilder()
                    var hasSpecialChar = false

                    for (char in input) {
                        if (char.isDigit()) {
                            cleanInput.append(char)
                        } else if (!hasSpecialChar && (char == '*' || char == 'x' || char == 'X' || char == '+' || char == '×')) {
                            cleanInput.append(char)
                            hasSpecialChar = true
                        } else if (hasSpecialChar && (char == '*' || char == 'x' || char == 'X' || char == '+' || char == '×')) {
                            cleanInput = StringBuilder().append(calcule(cleanInput.toString())).append(char)
                            hasSpecialChar = false
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
        loadDrinkToDrinkList()
        if (dailyDrinksList.getDrinkList().size !== dailyDrinks.drinkList.size) {
            dailyDrinks.drinkList = dailyDrinksList.getDrinkList()
            if (dailyDrinks.drinkList.isEmpty()) {
                seeDrinksButton.isEnabled = false
            }
        }
    }

    private fun calcule(input: String): String {
        return try {
            val (operator, firstOperand, secondOperand) = parseInput(input)
            val result = performCalculation(operator, firstOperand, secondOperand)
            updateCalculationHistory(firstOperand, secondOperand, operator, result)
            result.toString()
        } catch (e: Exception) {
            input.replace("[^0-9]".toRegex(), "") // Handle invalid input gracefully
        }
    }

    private fun parseInput(input: String): Triple<String, Double, Double> {
        val regexAdd = Regex("\\+")
        val regexMultiply = Regex("[*xX×]")

        return when {
            regexAdd.containsMatchIn(input) -> Triple("+", extractOperand(input, 0), extractOperand(input, 1))
            regexMultiply.containsMatchIn(input) -> Triple("*", extractOperand(input, 0, 1.0), extractOperand(input, 1, 1.0))
            else -> throw IllegalArgumentException("Operador não suportado na entrada: $input")
        }
    }

    private fun extractOperand(input: String, index: Int, defaultValue: Double = 0.0): Double {
        return input.split(Regex("[+xX×]")).getOrNull(index)?.trim()?.toDouble() ?: defaultValue
    }

    private fun performCalculation(operator: String, firstOperand: Double, secondOperand: Double): Int {
        val result = when (operator) {
            "+" -> firstOperand + secondOperand
            "*" -> firstOperand * secondOperand
            else -> throw IllegalArgumentException("Operador inválido: $operator")
        }
        showToast(firstOperand, secondOperand, operator, result.toInt())
        return result.toInt()
    }

    private fun updateCalculationHistory(firstOperand: Double, secondOperand: Double, operator: String, result: Int) {
        val calculationString = "${firstOperand.toInt()} $operator ${secondOperand.toInt()} = $result"
        currentDrink!!.calcArray = currentDrink!!.calcArray.plus(calculationString)
    }

    private fun showToast(firstOperand: Double, secondOperand: Double, operator: String, result: Int) {
        val message = "Resultado de ${firstOperand.toInt()} $operator ${secondOperand.toInt()} = $result"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun calculeQuantity() {
        quantityEditText.setText(calcule(quantityEditText.text.toString()))
    }
    private fun loading() {
        var drinkList = emptyList<Drink>()
        val drink = Drink()
        drinkList = drinkList.plus(drink)
        listFoodsView.adapter = DrinksAdapter(this,drinkList,"loading")
    }
    private fun hideKeyboard (view: View): Boolean {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        return true
    }

    private fun getDailyDrinksByDate(selectedDate: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            if (cache.hasCache(this@formDailyDrinks, "dailyDrinks")) {
                val dailyDrinksList = loadDailyDrinksFromCache(cache)
                val filteredDailyDrinks = dailyDrinksList.find { it.date == selectedDate }

                withContext(Dispatchers.Main) {
                    updateDailyDrinksAndUI(filteredDailyDrinks, selectedDate)
                }
            }
        }
    }

    private fun loadDailyDrinksFromCache(cache: Cache): List<DailyDrinks> {
        val dailyCaloriesListJson = cache.getCache(this@formDailyDrinks, "dailyDrinks")
        val jsonUtil = JSON()
        return jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java).toList()
    }

    private fun updateDailyDrinksAndUI(filteredDailyDrinks: DailyDrinks?, selectedDate: String) {
        dailyDrinks = filteredDailyDrinks ?: DailyDrinks().apply { date = selectedDate }
        setDrinkList(dailyDrinks.drinkList)
        seeDrinksButton.isEnabled = dailyDrinks.drinkList.isNotEmpty()
    }


    private fun getDailyDrinks() {
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

    private fun callDailyDrinksList() {
        try {
            var dailyDrinksList = Intent(this, dailyDrinksList::class.java)
            var jsonUtil = JSON()
            dailyDrinksList.putExtra("foodsList", dailyDrinks.drinkList.let { jsonUtil.toJson(it) })
            dailyDrinksList.putExtra("dailyCaloriesDate", editTextDate.text.toString())
            startActivity(dailyDrinksList)
        } catch (e: Exception) {
            println(RuntimeException(getString(R.string.error_calling_daily_drinks)+": $e"))
        }
    }
    private fun loadDrinkToDrinkList() {
        searchDrink("")
    }
    fun selectedDrink(drink: Drink) {
        try {
            saveDrinkButton.isEnabled = true
            quantityEditText.isEnabled = true
            currentDrink = drink
            nameDrinkLabel.text = currentDrink!!.foodDescription
        } catch (e: Exception) {
            println(RuntimeException("Error handling drink click: $e"))
        }
    }
    private fun searchDrink(value: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val drinkNutritionList = loadDrinkNutritionList()
                val filteredList = filterDrinks(drinkNutritionList, value)

                val adapter = DrinksAdapter(this@formDailyDrinks, filteredList)
                listFoodsView.adapter = adapter
            } catch (e: Exception) {
                println(RuntimeException("Erro ao ler o arquivo JSON: $e")) // Consider using a more robust logging mechanism
            }
        }
    }

    private suspend fun loadDrinkNutritionList(): List<Drink> {
        val cache = Cache()
        val jsonUtil = JSON()

        return if (cache.hasCache(this@formDailyDrinks, "Alimentos")) {
            jsonUtil.fromJson(
                cache.getCache(this@formDailyDrinks, "Alimentos"),
                Array<Drink>::class.java
            ).toList()
        } else {
            val jsonContent = withContext(Dispatchers.IO) {
                resources.openRawResource(R.raw.nutritional_table).bufferedReader()
                    .use { it.readText() }
            }
            val drinkNutritionList =
                jsonUtil.fromJson(jsonContent, Array<Drink>::class.java).toList()
            cache.setCache(this@formDailyDrinks, "Alimentos", jsonContent)
            drinkNutritionList
        }
    }

    private fun filterDrinks(drinkNutritionList: List<Drink>, value: String): List<Drink> {
        return if (value.isEmpty()) {
            drinkNutritionList
        } else {
            drinkNutritionList.filter { it.foodDescription.contains(value, ignoreCase = true) }
        }
    }

    private fun addDrinkToDailyList() {
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
                        seeDrinksButton.isEnabled = true
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

    private fun saveDailyDrinks() {
        addDrinkToDailyList()

        val filteredDailyDrinksList =
            dailyDrinks.drinkList.filter { it.foodDescription != "NO_DESCRIPTION" }

        if (filteredDailyDrinksList.isNotEmpty() || currentDrink != null) {
            saveNonEmptyDailyDrinks()
        } else {
            removeDailyDrinks()
        }
    }

    private fun saveNonEmptyDailyDrinks() {
        val cache = Cache()
        val jsonUtil = JSON()

        try {
            val existingDailyDrinksLists = loadDailyDrinksFromCache(cache, jsonUtil)
            val updatedDailyDrinksLists =
                updateDailyDrinksList(existingDailyDrinksLists, dailyDrinks)

            cache.setCache(this, "dailyDrinks", jsonUtil.toJson(updatedDailyDrinksLists))
            setDrinkList(dailyDrinks.drinkList) // Assuming this function exists in your formDailyDrinks context
            cache.setCache(this, "dailyDrinksUpdated${dailyDrinks.date}", "")
        } catch (e: Exception) {
            println(RuntimeException(getString(R.string.error_saving_daily_calories) + ":$e"))
        }
    }

    private fun loadDailyDrinksFromCache(cache: Cache, jsonUtil: JSON): List<DailyDrinks> {
        return if (cache.hasCache(this, "dailyDrinks")) {
            val dailyCaloriesListJson = cache.getCache(this, "dailyDrinks")
            jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyDrinks>::class.java).toList()
        } else {
            emptyList()
        }
    }

    private fun updateDailyDrinksList(
        existingDailyDrinksLists: List<DailyDrinks>,
        newDailyDrinks: DailyDrinks
    ): List<DailyDrinks> {
        val formattedDate = editTextDate.text.toString()
        val existingDailyDrinksForDate =
            existingDailyDrinksLists.filter { it.date == formattedDate }

        return (existingDailyDrinksLists - existingDailyDrinksForDate.toSet()) + newDailyDrinks
    }


    private fun removeDailyDrinks() {
        val cache = Cache()
        val jsonUtil = JSON()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val existingDailyDrinksLists = loadDailyDrinksFromCache(cache, jsonUtil)
                val updatedDailyDrinksLists = removeDailyDrinksForDate(existingDailyDrinksLists)

                if (updatedDailyDrinksLists != existingDailyDrinksLists) { // Check if any drinks were removed
                    cache.setCache(
                        this@formDailyDrinks,
                        "dailyDrinks",
                        jsonUtil.toJson(updatedDailyDrinksLists)
                    )

                    withContext(Dispatchers.Main) {
                        resetDailyDrinksAndUI()
                        showSuccessToast()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleRemoveError(e)
                }
            } finally {
                finish() // Ensure finish() is called even if an exception occurs
            }
        }
    }

    private fun removeDailyDrinksForDate(dailyDrinksList: List<DailyDrinks>): List<DailyDrinks> {
        val formattedDate = editTextDate.text.toString()
        return dailyDrinksList.filterNot { it.date == formattedDate }
    }

    private fun resetDailyDrinksAndUI() {
        dailyDrinks = DailyDrinks()
        setDrinkList(dailyDrinks.drinkList)
        seeDrinksButton.isEnabled = false
    }

    private fun showSuccessToast() {
        Toast.makeText(
            this@formDailyDrinks,
            getString(R.string.successfully_excluded_record),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleRemoveError(e: Exception) {
        println(RuntimeException("Erro ao remover registro de bebidas diárias: $e"))
        Toast.makeText(
            this@formDailyDrinks,
            getString(R.string.delete_record_error),
            Toast.LENGTH_SHORT
        ).show()
    }


}
