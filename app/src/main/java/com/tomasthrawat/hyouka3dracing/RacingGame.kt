package com.tomasthrawat.hyouka3dracing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlin.math.min

private data class RaceMap(val name: String, val corner: String, val scenery: String)
private val maps = listOf(
    RaceMap("CITY GP", "models/track_corner90.glb", "models/tower.glb"),
    RaceMap("DESERT RING", "models/track_banked.glb", "models/guardrail.glb"),
    RaceMap("NIGHT CIRCUIT", "models/track_corner45.glb", "models/timing.glb")
)

@Composable
fun RacingGame(screen: MainActivity.Screen, onScreen: (MainActivity.Screen) -> Unit) {
    when (screen) {
        MainActivity.Screen.MENU -> MenuScreen { onScreen(MainActivity.Screen.MAPS) }
        MainActivity.Screen.MAPS -> MapScreen(
            onBack = { onScreen(MainActivity.Screen.MENU) },
            onRace = { onScreen(MainActivity.Screen.RACE) }
        )
        MainActivity.Screen.RACE -> RaceScreen { onScreen(MainActivity.Screen.MAPS) }
    }
}

@Composable
private fun MenuScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HYOUKA 3D RACING", color = Color.White, fontSize = 34.sp)
            Text("NATIVE KOTLIN • 3D", color = Color.LightGray, fontSize = 15.sp)
            Button(
                onClick = onStart,
                modifier = Modifier.padding(top = 28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) { Text("START RACE") }
        }
    }
}

@Composable
private fun MapScreen(onBack: () -> Unit, onRace: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SELECT MAP", color = Color.White, fontSize = 28.sp)
            Row(
                Modifier.fillMaxWidth().padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                maps.forEachIndexed { index, map ->
                    Button(
                        onClick = { selected = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (index == selected) Color.White else Color.DarkGray,
                            contentColor = if (index == selected) Color.Black else Color.White
                        )
                    ) { Text(map.name) }
                }
            }
            Text(
                "MAP " + (selected + 1) + " • 3 LAPS • 4 RACERS",
                color = Color.LightGray,
                modifier = Modifier.padding(28.dp)
            )
            Button(onClick = onRace) { Text("RACE") }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("BACK") }
        }
    }
}

@Composable
private fun RaceScreen(onExit: () -> Unit) {
    val engine = rememberEngine()
    val loader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) {
        createEnvironment(environmentLoader)
    }
    val light = rememberMainLightNode(engine) { intensity = 120_000f }

    val car = rememberModelInstance(loader, "models/car.glb")
    val straight = rememberModelInstance(loader, "models/track_straight.glb")
    val start = rememberModelInstance(loader, "models/track_start.glb")
    val corner = rememberModelInstance(loader, "models/track_corner90.glb")
    val tower = rememberModelInstance(loader, "models/tower.glb")
    val rail = rememberModelInstance(loader, "models/guardrail.glb")

    var steer by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(0f) }
    var distance by remember { mutableFloatStateOf(0f) }
    var lap by remember { mutableIntStateOf(1) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!finished) {
            delay(33)
            speed = min(1f, speed + 0.015f)
            distance += speed * 0.55f
            if (distance >= 180f) {
                distance = 0f
                if (lap == 3) finished = true else lap++
            }
            steer *= 0.88f
        }
    }

    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = { steer = 0f },
                onDragCancel = { steer = 0f },
                onDrag = { change, amount ->
                    change.consume()
                    steer = (steer + amount.x / 700f).coerceIn(-1f, 1f)
                }
            )
        }
    ) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = loader,
            environment = environment,
            mainLightNode = light,
            autoCenterContent = false,
            autoFitContent = false
        ) {
            start?.let {
                ModelNode(modelInstance = it, position = Position(z = -8f), scaleToUnits = 1f)
            }
            straight?.let {
                for (i in 0..8) {
                    val z = -20f + i * 20f + distance % 20f
                    ModelNode(modelInstance = it, position = Position(z = z), scaleToUnits = 1f)
                }
            }
            corner?.let {
                ModelNode(modelInstance = it, position = Position(z = -82f + distance), scaleToUnits = 1f)
            }
            tower?.let {
                ModelNode(modelInstance = it, position = Position(x = -9f, z = -25f), scaleToUnits = 1f)
            }
            rail?.let {
                ModelNode(modelInstance = it, position = Position(x = 7f, z = -30f), scaleToUnits = 1f)
            }
            car?.let {
                ModelNode(
                    modelInstance = it,
                    position = Position(x = steer * 2.5f, y = 0.05f, z = -3.2f),
                    scaleToUnits = 1.1f
                )
                ModelNode(
                    modelInstance = it,
                    position = Position(x = -2.1f, y = 0.05f, z = -12f),
                    scaleToUnits = 1.1f
                )
                ModelNode(
                    modelInstance = it,
                    position = Position(x = 2.0f, y = 0.05f, z = -18f),
                    scaleToUnits = 1.1f
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("LAP " + lap + " / 3", color = Color.White, fontSize = 20.sp)
            Text(
                "SPEED " + (speed * 280f).toInt() + " KM/H",
                color = Color.White,
                fontSize = 20.sp
            )
            Button(onClick = onExit) { Text("MENU") }
        }

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .size(150.dp)
                .background(Color(0x66000000))
        ) {
            Text(
                "DRAG\nTO STEER",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (finished) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINISH", color = Color.White, fontSize = 42.sp)
                    Text("RACE COMPLETE", color = Color.LightGray, fontSize = 18.sp)
                    Button(
                        onClick = onExit,
                        modifier = Modifier.padding(top = 20.dp)
                    ) { Text("BACK TO MAPS") }
                }
            }
        }
    }
}
