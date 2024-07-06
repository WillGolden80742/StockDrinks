package com.example.StockDrinks.Adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.StockDrinks.Controller.DailyDrinks
import com.example.StockDrinks.R
import com.example.StockDrinks.formDailyDrinks
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DailyDriksAdapter(context: Context, private val dailyDrinksList: List<DailyDrinks>) : ArrayAdapter<DailyDrinks>(context, 0, dailyDrinksList) {

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

        val drinksListSums = SpannableStringBuilder()
        val drinksMap = mutableMapOf<Pair<String, String>, Int>()

        currentDailyDrinks.drinkList.forEach {
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
                val spannableCategory = SpannableString(categoryText).apply {
                    setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, categoryText.length, 0)
                }
                drinksListSums.append(spannableCategory)
                currentCategory = category
            }
            val quantityText = "${totalQuantity.toString().padStart(4, '0')} ; $description\n"
            drinksListSums.append(quantityText)
        }

        descriptionTextView.text = drinksListSums.toString().replace(";","-")

        copyButton.setOnClickListener {
            val clipboardManager = getSystemService(context, ClipboardManager::class.java)
            val date = dateTextView.text.toString()+";"
            val clipData = ClipData.newPlainText("label",date + "\n" + drinksListSums)
            clipboardManager!!.setPrimaryClip(clipData)
            Toast.makeText(context,
                context.getString(R.string.copied_to_transfer_area), Toast.LENGTH_SHORT).show()
        }

        drinkEdit.setOnClickListener {
            val dailyDrinks = dailyDrinksList[position]
            val intent = Intent(context, formDailyDrinks::class.java)
            try {
                intent.putExtra("dailyCaloriesDate", dailyDrinks.date)
                context.startActivity(intent)
            } catch (e: Exception) {
                println(context.getString(R.string.error_when_calling_the_daily_calorie_screen)+":$e")
            }
        }

        return listItemView
    }
}
