package com.example.routeplanner2

enum class CargoType {
    DRY, CHILLED, FROZEN
}

data class Point(
    val name: String,
    val lat: Double,
    val lon: Double,
    var type: CargoType,
    var address: String,
    var pallets: Int
)