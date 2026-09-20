package com.tomasthrawat.hyouka3dracing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private data class MapInfo(
    val id: MapId,
    val name: String,
    val description: String,
    val background: Color
)

private val maps = listOf(
    MapInfo(MapId.OCEAN, "OCEAN GP", "Fast coastal circuit with long straights.", Color(0xFF4FA9D8)),
    MapInfo(MapId.DESERT, "DESERT RING", "Wide high-speed layout with a hairpin.", Color(0xFFD59A5B)),
    MapInfo(MapId.NIGHT, "NIGHT CIRCUIT", "Tight night layout with a technical middle.", Color(0xFF17152D))
)

@Composable
fun RacingGame(
    screen: MainActivity.Screen,
    onScreen: (MainActivity.Screen) -> Unit
) {
    var selectedMap by remember { mutableStateOf(MapId.OCEAN) }
    when (screen) {
        MainActivity.Screen.MENU -> MenuScreen { onScreen(MainActivity.Screen.MAPS) }
        MainActivity.Screen.MAPS -> MapScreen(
            selected = selectedMap,
            onSelect = { selectedMap = it },
            onBack = { onScreen(MainActivity.Screen.MENU) },
            onRace = { onScreen(MainActivity.Screen.RACE) }
        )
        MainActivity.Screen.RACE -> RaceScreen(
            map = selectedMap,
            onExit = { onScreen(MainActivity.Screen.MAPS) }
        )
    }
}

@Composable
private fun MenuScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF05080D)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HYOUKA", color = Color.White, fontSize = 48.sp)
            Text("3D RACING", color = Color(0xFFB7C8DA), fontSize = 24.sp)
            Spacer(Modifier.padding(12.dp))
            Button(onClick = onStart) { Text("SELECT TRACK") }
        }
    }
}

@Composable
private fun MapScreen(
    selected: MapId,
    onSelect: (MapId) -> Unit,
    onBack: () -> Unit,
    onRace: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF05080D))) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SELECT TRACK", color = Color.White, fontSize = 30.sp)
            Spacer(Modifier.padding(8.dp))
            maps.forEach { map ->
                Button(
                    onClick = { onSelect(map.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == map.id) Color(0xFF6C4DD9) else Color(0xFF252B33)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(map.name)
                        Text(map.description, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.padding(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onBack) { Text("BACK") }
                Button(onClick = onRace) { Text("RACE") }
            }
        }
    }
}

@Composable
private fun RaceScreen(map: MapId, onExit: () -> Unit) {
    val physics = remember { Physics() }
    var input by remember { mutableStateOf(CarInput()) }
    var snapshot by remember {
        mutableStateOf(
            RaceSnapshot(
                speedKmh = 0f,
                progress = 0f,
                lap = 1,
                finished = false,
                ai = listOf(18f to -1.1f, 42f to 1.1f, 72f to 0f)
            )
        )
    }

    LaunchedEffect(map) {
        val race = Race(map, 3)
        val aiSystems = listOf(Ai(0.55f), Ai(0.72f), Ai(0.85f))
        val player = CarState()
        val aiCars = listOf(
            CarState(progress = 18f, lateral = -1.1f),
            CarState(progress = 42f, lateral = 1.1f),
            CarState(progress = 72f, lateral = 0f)
        )

        while (!race.finished(player.progress)) {
            delay(20)
            val dt = 0.02f
            physics.update(player, input, dt)
            aiCars.forEachIndexed { index, car -> aiSystems[index].update(car, dt) }
            snapshot = RaceSnapshot(
                speedKmh = player.speedKmh,
                progress = player.progress,
                lap = race.lapFor(player.progress),
                finished = race.finished(player.progress),
                ai = aiCars.map { it.progress to it.lateral }
            )
        }
    }

    val mapInfo = maps.first { it.id == map }

    Box(Modifier.fillMaxSize().background(mapInfo.background)) {
        RaceScene(Modifier.fillMaxSize(), map, snapshot)
        RaceHud(snapshot, onExit) { input = it }

        if (snapshot.finished) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINISH", color = Color.White, fontSize = 44.sp)
                    Spacer(Modifier.padding(10.dp))
                    Button(onClick = onExit) { Text("BACK TO TRACKS") }
                }
            }
        }
    }
}
