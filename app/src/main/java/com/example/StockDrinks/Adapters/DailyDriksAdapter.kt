package com.example.StockDrinks.Adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.StockDrinks.Controller.Cache
import com.example.StockDrinks.Controller.DailyDrinks
import com.example.StockDrinks.R
import com.example.StockDrinks.formDailyDrinks
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DailyDriksAdapter(context: Context, private val dailyDrinksList: List<DailyDrinks>) : ArrayAdapter<DailyDrinks>(context, 0, dailyDrinksList) {

    private val cache = Cache()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
        }

        val currentDailyDrinks = dailyDrinksList[position]

        val dateTextView = listItemView!!.findViewById<TextView>(R.id.titleTextViewItem)
        dateTextView.text = currentDailyDrinks.date

        val descriptionTextView = listItemView.findViewById<TextView>(R.id.textViewItem)

        val copyButton = listItemView.findViewById<FloatingActionButton>(R.id.copyButton)
        val drinkEdit = listItemView.findViewById<FloatingActionButton>(R.id.drinkEdit)
        var label:String = if (cache.hasCache(context,"dailyDrinksUpdated${dateTextView.text}")) {
            cache.getCache(context, "dailyDrinksUpdated${dateTextView.text}")
        } else {
            generateDrinksListSums(currentDailyDrinks)
        }

        descriptionTextView.text = label

        copyButton.setOnClickListener {
            val clipboardManager = getSystemService(context, ClipboardManager::class.java)
            val date = dateTextView.text.toString() + ";"
            val clipData = ClipData.newPlainText("label", date + "\n" + label.replace("-", ";"))
            clipboardManager!!.setPrimaryClip(clipData)
            Toast.makeText(context, context.getString(R.string.copied_to_transfer_area), Toast.LENGTH_SHORT).show()
        }

        drinkEdit.setOnClickListener {
            val dailyDrinks = dailyDrinksList[position]
            val intent = Intent(context, formDailyDrinks::class.java)
            try {
                intent.putExtra("dailyCaloriesDate", dailyDrinks.date)
                context.startActivity(intent)
            } catch (e: Exception) {
                println(context.getString(R.string.error_when_calling_the_daily_calorie_screen) + ":$e")
            }
        }

        return listItemView
    }

    private fun generateDrinksListSums(dailyDrinks: DailyDrinks): String {
        val drinksMap = mutableMapOf<Pair<String, String>, Int>()
        val drinksListSums = SpannableStringBuilder()

        dailyDrinks.drinkList.forEach {
            val parts = it.foodDescription.split(",")
            val category = it.category
            val description = parts[0]
            val quantity = it.quantity.toInt()
            val key = category.uppercase() to description.uppercase()
            drinksMap[key] = drinksMap.getOrDefault(key, 0) + quantity
        }

        val sortedDrinksMap = drinksMap
            .toSortedMap(compareBy({ it.first }, { it.second }))

        var currentCategory: String? = null
        sortedDrinksMap.forEach { (key, totalQuantity) ->
            val (category, description) = key
            if (category != currentCategory) {
                val categoryText = "${category.uppercase()}\n"
                drinksListSums.append(categoryText)
                currentCategory = category
            }
            val formattedDescription =
                description.split(" ").joinToString(" ") { it.lowercase().capitalize() }
            val quantityText = "${totalQuantity.toString().padStart(4, '0')} - $formattedDescription\n"
            drinksListSums.append(quantityText)
        }
        cache.setCache(context, "dailyDrinksUpdated${dailyDrinks.date}",drinksListSums.toString())
        return drinksListSums.toString()
    }
}
