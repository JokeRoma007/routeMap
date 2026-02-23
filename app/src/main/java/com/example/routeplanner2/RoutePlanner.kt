package com.example.routeplanner2

import kotlin.math.*

object RoutePlanner {

    /** «Воздушное» расстояние между двумя точками по формуле гаверсинуса */
    private fun distance(a: Point, b: Point): Double {
        val r = 6_371_000.0          // радиус Земли в метрах
        val φ1 = Math.toRadians(a.lat)
        val φ2 = Math.toRadians(b.lat)
        val Δφ = Math.toRadians(b.lat - a.lat)
        val Δλ = Math.toRadians(b.lon - a.lon)

        val h = sin(Δφ / 2).pow(2) +
                cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
        return 2 * r * asin(sqrt(h)) // метры
    }

    /** Найти NN‑маршрут для одной «корзины» точек, начиная от start */
    private fun nearestNeighbour(start: Point, bucket: List<Point>): List<Point> {
        val left   = bucket.toMutableList()
        val result = mutableListOf<Point>()
        var current = start
        while (left.isNotEmpty()) {
            val next = left.minByOrNull { distance(current, it) }!!
            result += next
            left   -= next
            current = next
        }
        return result
    }

    /**
     * Построить маршрут:
     * Dry → Chilled → Frozen, внутри каждого сегмента — NN‑оптимизация.
     */
    fun computeRoute(start: Point, all: List<Point>): List<Point> {
        val dry     = all.filter { it.type == CargoType.DRY     }
        val chilled = all.filter { it.type == CargoType.CHILLED }
        val frozen  = all.filter { it.type == CargoType.FROZEN  }

        val result = mutableListOf<Point>()
        var current = start

        fun addSegment(segment: List<Point>) {
            if (segment.isNotEmpty()) {
                val part = nearestNeighbour(current, segment)
                result += part
                current  = part.last()
            }
        }

        addSegment(dry)
        addSegment(chilled)
        addSegment(frozen)
        return result
    }
}
