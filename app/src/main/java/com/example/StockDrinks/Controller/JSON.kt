package com.example.StockDrinks.Controller

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JSON {

    // Método para desserializar um JSON para um objeto específico
    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método fromJson do Gson para converter o JSON para o objeto desejado
        return gson.fromJson(json, classOfT)
    }

    // Método para desserializar um JSON para uma lista de objetos de um tipo genérico
    inline fun <reified T> fromJsonArray(json: String): List<T> {
        // Cria um TypeToken para representar uma lista do tipo genérico T
        val listType = object : TypeToken<List<T>>() {}.type
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método fromJson do Gson para converter o JSON para a lista de objetos do tipo T
        return gson.fromJson(json, listType)
    }

    // Método para serializar um objeto para JSON
    fun toJson(obj: Any): String {
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método toJson do Gson para converter o objeto para JSON
        return gson.toJson(obj)
    }

    // Método para pesquisar dentro de um JSON
    inline fun <reified T> searchJson(json: String, attribute: String, value: String): T {
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método fromJson do Gson para converter o JSON para o objeto desejado
        val list = gson.fromJson(json, Array<T>::class.java)
        // Pesquisa o objeto desejado na lista
        for (item in list) {
            val json = gson.toJson(item)
            if (json.contains("\"$attribute\":\"$value\"")) {
                return item
            }
        }
        throw RuntimeException("Item not found")
    }

}
