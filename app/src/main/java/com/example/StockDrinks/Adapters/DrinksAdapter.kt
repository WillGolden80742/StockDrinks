package com.example.StockDrinks.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.StockDrinks.Controller.Drink
import com.example.StockDrinks.R
import com.example.StockDrinks.dailyDrinksList
import com.example.StockDrinks.formDailyDrinks
import com.example.StockDrinks.formDrinks
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DrinksAdapter(context: Context, drinkList: List<Drink>, activity:String="formDailyDrinks") : ArrayAdapter<Drink>(context, 0, drinkList) {

    private val activity = activity

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.drinks_list_item, parent, false)
        }

        val currentItem = getItem(position)

        val descriptionTextView = listItemView!!.findViewById<TextView>(R.id.foodTitleTextViewItem)
        val detailsTextView = listItemView.findViewById<TextView>(R.id.foodTextViewItem)
        val editButton = listItemView.findViewById<FloatingActionButton>(R.id.foodFloatingEditActionButton)
        val addButton = listItemView.findViewById<FloatingActionButton>(R.id.foodFloatingAddActionButton)

        descriptionTextView.text = currentItem?.foodDescription

        when (activity) {
            "formDailyDrinks" -> {
                detailsTextView.text = currentItem!!.category.uppercase()

                editButton.setOnClickListener {
                    val intent = Intent(context, formDrinks::class.java).apply {
                        putExtra("foodID", currentItem?.foodNumber)
                    }
                    context.startActivity(intent)
                }

                addButton.setOnClickListener {
                    try {
                        Toast.makeText(
                            context,
                            context.getString(R.string.selected)+" \"${currentItem?.foodDescription}\"",
                            Toast.LENGTH_SHORT
                        ).show()
                        var formDailyDrinks = context as formDailyDrinks
                        currentItem?.let { it1 -> formDailyDrinks.selectedDrink(it1) }
                    } catch (e: Exception) {
                        Toast.makeText(context,
                            context.getString(R.string.add_drink_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "dailyDrinksList" -> {
                addButton.isVisible = false
                detailsTextView.text = currentItem?.toString(context)
                editButton.setImageResource(R.drawable.ic_fluent_delete_24_regular)
                editButton.setOnClickListener {
                    try {
                        Toast.makeText(
                            context,
                            "${currentItem?.foodDescription} removed",
                            Toast.LENGTH_SHORT
                        ).show()
                        var dailyDrinksList = context as dailyDrinksList
                        currentItem?.let { it1 -> dailyDrinksList.removeFood(it1) }
                    } catch (e: Exception) {
                        Toast.makeText(context,
                            context.getString(R.string.remove_drink_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "loading" -> {
                descriptionTextView.text = context.getString(R.string.loading)
                detailsTextView.text = context.getString(R.string.please_wait)
                editButton.isVisible = false
                addButton.isVisible = false
            }
        }



        return listItemView
    }


}
