package com.example.routeplanner2

/**
 * Хранит построенный и сохранённый пользователем маршрут.
 *
 * @param id    уникальный ID (используем UUID)
 * @param title понятное имя маршрута
 * @param points упорядоченный список точек маршрута
 */
data class SavedRoute(
    val id: String,
    val title: String,
    val points: List<Point>
)
