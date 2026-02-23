package com.example.routeplanner2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    favorites: List<Point>,
    savedRoutes: List<SavedRoute>,
    onFavoritesChange: (List<Point>) -> Unit,
    onRoutesChange: (List<SavedRoute>) -> Unit
) {
    val ctx = LocalContext.current

    var startLatStr by remember { mutableStateOf("") }
    var startLonStr by remember { mutableStateOf("") }
    val points = remember { mutableStateListOf<Point>() }
    var routeBuilt by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<Point>>(emptyList()) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var tmpRouteName by remember { mutableStateOf("") }

    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Точки", "Маршрут", "Избр. точки", "Сохр. маршруты")

    fun pr(t: CargoType) = when (t) {
        CargoType.DRY -> 3
        CargoType.CHILLED -> 2
        CargoType.FROZEN -> 1
    }

    fun recalc() {
        val la = startLatStr.toDoubleOrNull()
        val lo = startLonStr.toDoubleOrNull()
        if (routeBuilt && la != null && lo != null && points.isNotEmpty()) {
            routePoints = buildRouteWithPriority(la, lo, points, ::pr)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Планировщик маршрута") }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {

            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = (tab == i), onClick = { tab = i }) {
                        Text(t, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            when (tab) {
                0 -> PointsTab(
                    points,
                    onChange = { recalc() },
                    onAddToFavorites = { p ->
                        if (favorites.any { it.name == p.name && it.lat == p.lat && it.lon == p.lon })
                            Toast.makeText(ctx, "Уже в избранном", Toast.LENGTH_SHORT).show()
                        else onFavoritesChange(favorites + p)
                    }
                )

                1 -> RouteTab(
                    startLat = startLatStr,
                    startLon = startLonStr,
                    onStartLatChange = { startLatStr = it; recalc() },
                    onStartLonChange = { startLonStr = it; recalc() },
                    points = routePoints,
                    onBuild = {
                        val la = startLatStr.toDoubleOrNull()
                        val lo = startLonStr.toDoubleOrNull()
                        if (la == null || lo == null) {
                            Toast.makeText(ctx, "Введите старт", Toast.LENGTH_SHORT).show()
                            return@RouteTab
                        }
                        if (points.isEmpty()) {
                            Toast.makeText(ctx, "Нет точек", Toast.LENGTH_SHORT).show()
                            return@RouteTab
                        }
                        routeBuilt = true
                        routePoints = buildRouteWithPriority(la, lo, points, ::pr)
                        tmpRouteName = "Маршрут ${savedRoutes.size + 1}"
                        showSaveDialog = true
                    }
                )

                2 -> FavoritesTab(
                    favorites,
                    onAddToRoute = { p -> points += p; recalc() },
                    onDeleteFavorite = { p -> onFavoritesChange(favorites - p) }
                )

                3 -> SavedRoutesTab(
                    routes = savedRoutes,
                    onRestore = { rt ->
                        startLatStr = rt.points.first().lat.toString()
                        startLonStr = rt.points.first().lon.toString()
                        points.clear()
                        points += rt.points
                        routeBuilt = true
                        routePoints = rt.points
                        tab = 1
                    },
                    onRename = { rt, newName ->
                        onRoutesChange(savedRoutes.map { if (it.id == rt.id) rt.copy(title = newName) else it })
                    },
                    onDelete = { rt -> onRoutesChange(savedRoutes - rt) }
                )
            }

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            onRoutesChange(
                                savedRoutes + SavedRoute(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = tmpRouteName.ifBlank { "Без имени" },
                                    points = routePoints
                                )
                            )
                            showSaveDialog = false
                            Toast.makeText(ctx, "Сохранено", Toast.LENGTH_SHORT).show()
                        }) { Text("Сохранить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) { Text("Отмена") }
                    },
                    title = { Text("Имя маршрута") },
                    text = {
                        OutlinedTextField(
                            value = tmpRouteName,
                            onValueChange = { tmpRouteName = it },
                            singleLine = true
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PointsTab(
    points: MutableList<Point>,
    onChange: () -> Unit,
    onAddToFavorites: (Point) -> Unit
) {
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CargoType.DRY) }
    var addr by remember { mutableStateOf("") }
    var pal by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(lat, { lat = it }, label = { Text("Широта") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(lon, { lon = it }, label = { Text("Долгота") }, singleLine = true, modifier = Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип") },
                    trailingIcon = {
                        IconButton(onClick = { menu = !menu }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    CargoType.values().forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = { type = it; menu = false }
                        )
                    }
                }
            }
            OutlinedTextField(pal, { pal = it.filter(Char::isDigit) }, label = { Text("Палеты") }, singleLine = true, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(addr, { addr = it }, label = { Text("Адрес") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val la = lat.toDoubleOrNull()
                val lo = lon.toDoubleOrNull()
                if (name.isBlank() || la == null || lo == null) {
                    Toast.makeText(ctx, "Проверьте данные", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                points += Point(name, la, lo, type, addr, pal.toIntOrNull() ?: 0)
                onChange()
                name = ""
                lat = ""
                lon = ""
                addr = ""
                pal = ""
                type = CargoType.DRY
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(points) { i, p ->
            EditablePointCard(
                idx = i,
                point = p,
                onChange = { points[i] = it; onChange() },
                onAddFav = onAddToFavorites
            )
        }
    }
}

@Composable
private fun RouteTab(
    startLat: String,
    startLon: String,
    onStartLatChange: (String) -> Unit,
    onStartLonChange: (String) -> Unit,
    points: List<Point>,
    onBuild: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = startLat,
            onValueChange = onStartLatChange,
            label = { Text("Начальная широта") },
            singleLine = true
        )
        OutlinedTextField(
            value = startLon,
            onValueChange = onStartLonChange,
            label = { Text("Начальная долгота") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBuild, modifier = Modifier.fillMaxWidth()) {
            Text("Построить маршрут")
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(points) { p ->
                Text("${p.name} (${p.type.name}) — ${p.lat}, ${p.lon}")
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    favorites: List<Point>,
    onAddToRoute: (Point) -> Unit,
    onDeleteFavorite: (Point) -> Unit
) {
    LazyColumn {
        items(favorites) { p ->
            ListItem(
                headlineContent = { Text(p.name) },
                supportingContent = { Text("${p.lat}, ${p.lon}") },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onAddToRoute(p) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Добавить")
                        }
                        IconButton(onClick = { onDeleteFavorite(p) }) {
                            Icon(Icons.Outlined.StarBorder, contentDescription = "Удалить")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SavedRoutesTab(
    routes: List<SavedRoute>,
    onRestore: (SavedRoute) -> Unit,
    onRename: (SavedRoute, String) -> Unit,
    onDelete: (SavedRoute) -> Unit
) {
    val ctx = LocalContext.current
    var renamingId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    LazyColumn {
        items(routes) { rt ->
            ListItem(
                headlineContent = {
                    if (renamingId == rt.id) {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(rt.title)
                    }
                },
                supportingContent = { Text("${rt.points.size} точек") },
                trailingContent = {
                    Row {
                        if (renamingId == rt.id) {
                            IconButton(onClick = {
                                if (renameText.isNotBlank()) {
                                    onRename(rt, renameText)
                                    renamingId = null
                                } else {
                                    Toast.makeText(ctx, "Введите имя", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Сохранить")
                            }
                            IconButton(onClick = { renamingId = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Отмена")
                            }
                        } else {
                            IconButton(onClick = { onRestore(rt) }) {
                                Icon(Icons.Filled.Restore, contentDescription = "Восстановить")
                            }
                            IconButton(onClick = {
                                renamingId = rt.id
                                renameText = rt.title
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Переименовать")
                            }
                            IconButton(onClick = { onDelete(rt) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun EditablePointCard(
    idx: Int,
    point: Point,
    onChange: (Point) -> Unit,
    onAddFav: (Point) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${idx + 1}. ${point.name} (${point.type.name})")
                IconButton(onClick = { onAddFav(point) }) {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Добавить в избранное")
                }
            }
            if (expanded) {
                OutlinedTextField(
                    value = point.name,
                    onValueChange = { onChange(point.copy(name = it)) },
                    label = { Text("Название") },
                    singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = point.lat.toString(),
                        onValueChange = { v -> v.toDoubleOrNull()?.let { onChange(point.copy(lat = it)) } },
                        label = { Text("Широта") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = point.lon.toString(),
                        onValueChange = { v -> v.toDoubleOrNull()?.let { onChange(point.copy(lon = it)) } },
                        label = { Text("Долгота") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Тип груза с дропдауном
                Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = point.type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип груза") },
                        trailingIcon = {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        CargoType.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    onChange(point.copy(type = it))
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Кол-во паллет
                OutlinedTextField(
                    value = point.pallets.toString(),
                    onValueChange = {
                        val num = it.filter(Char::isDigit).toIntOrNull() ?: 0
                        onChange(point.copy(pallets = num))
                    },
                    label = { Text("Палеты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Адрес (если хочешь, тоже можно редактировать)
                OutlinedTextField(
                    value = point.address,
                    onValueChange = { onChange(point.copy(address = it)) },
                    label = { Text("Адрес") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}


fun buildRouteWithPriority(
    startLat: Double,
    startLon: Double,
    points: List<Point>,
    priorityFun: (CargoType) -> Int
): List<Point> {
    if (points.isEmpty()) return emptyList()

    val orderedTypes = listOf(CargoType.DRY, CargoType.CHILLED, CargoType.FROZEN)
    val result = mutableListOf<Point>()
    var currentLat = startLat
    var currentLon = startLon

    for (type in orderedTypes) {
        val pointsOfType = points.filter { it.type == type }.toMutableList()
        while (pointsOfType.isNotEmpty()) {
            // Находим ближайшую точку по Евклидову расстоянию
            val nearest = pointsOfType.minByOrNull {
                euclideanDistance(currentLat, currentLon, it.lat, it.lon)
            }!!
            result.add(nearest)
            currentLat = nearest.lat
            currentLon = nearest.lon
            pointsOfType.remove(nearest)
        }
    }
    return result
}

fun euclideanDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dx = lat1 - lat2
    val dy = lon1 - lon2
    return sqrt(dx * dx + dy * dy)
}