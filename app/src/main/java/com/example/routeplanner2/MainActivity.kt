package com.example.routeplanner2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : ComponentActivity() {

    /** избранные точки */
    private val favorites = mutableStateListOf<Point>()

    /** сохранённые маршруты */
    private val savedRoutes = mutableStateListOf<SavedRoute>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* --- начальная загрузка --- */
        favorites += loadJson("favorites.json")
        savedRoutes += loadJson("routes.json")

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen(
                        favorites     = favorites,
                        savedRoutes    = savedRoutes,
                        onFavoritesChange = { newFavs ->
                            favorites.clear()
                            favorites += newFavs
                            saveJson("favorites.json", newFavs)
                        },
                        onRoutesChange = { newList ->
                            savedRoutes.clear()
                            savedRoutes += newList
                            saveJson("routes.json", newList)
                        }
                    )
                }
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* --------------------  generic load / save  ----------------------- */
    /* ------------------------------------------------------------------ */

    private inline fun <reified T> loadJson(fileName: String): List<T> {
        return try {
            val file = File(filesDir, fileName)
            if (!file.exists()) emptyList()
            else Gson().fromJson(file.readText(), object : TypeToken<List<T>>() {}.type)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка чтения $fileName", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    private fun <T> saveJson(fileName: String, data: List<T>) {
        try {
            File(filesDir, fileName).writeText(Gson().toJson(data))
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка записи $fileName", Toast.LENGTH_SHORT).show()
        }
    }
}
