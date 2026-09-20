package com.tomasthrawat.hyouka3dracing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay

private data class RaceMap(
    val name: String,
    val sky: Color
)

private val maps = listOf(
    RaceMap("OCEAN GP", Color(0xFF78C7EA)),
    RaceMap("DESERT RING", Color(0xFFE7B66B)),
    RaceMap("NIGHT CIRCUIT", Color(0xFF10152B))
)

@Composable
fun RacingGame(
    screen: MainActivity.Screen,
    onScreen: (MainActivity.Screen) -> Unit
) {
    var selectedMap by remember { mutableIntStateOf(0) }
    when (screen) {
        MainActivity.Screen.MENU -> MenuScreen { onScreen(MainActivity.Screen.MAPS) }
        MainActivity.Screen.MAPS -> MapScreen(
            selectedIndex = selectedMap,
            onSelect = { selectedMap = it },
            onBack = { onScreen(MainActivity.Screen.MENU) },
            onRace = { onScreen(MainActivity.Screen.RACE) }
        )
        MainActivity.Screen.RACE -> RaceScreen(
            map = maps[selectedMap],
            onExit = { onScreen(MainActivity.Screen.MAPS) }
        )
    }
}

@Composable
private fun MenuScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF07111F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HYOUKA 3D RACING", color = Color.White, fontSize = 34.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onStart) { Text("START RACE") }
        }
    }
}

@Composable
private fun MapScreen(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onRace: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF07111F))) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SELECT MAP", color = Color.White, fontSize = 28.sp)
            Row(
                Modifier.fillMaxWidth().padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                maps.forEachIndexed { index, map ->
                    Button(
                        onClick = { onSelect(index) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (index == selectedIndex) Color(0xFF7C4DFF) else Color(0xFF30343B)
                        )
                    ) { Text(map.name) }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("REAL GLB TRACK + CAR", color = Color(0xFFB8C7D9), fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onRace) { Text("RACE") }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("BACK") }
        }
    }
}

@Composable
private fun RaceScreen(map: RaceMap, onExit: () -> Unit) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 3.4f, z = 10f)
        rotation = Rotation(x = -9f)
    }
    val mainLight = rememberMainLightNode(engine) { intensity = 120_000f }

    var steering by remember { mutableFloatStateOf(0f) }
    var throttle by remember { mutableFloatStateOf(0f) }
    var brake by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(140f) }
    var distance by remember { mutableFloatStateOf(0f) }
    var travel by remember { mutableFloatStateOf(0f) }
    var lap by remember { mutableIntStateOf(1) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!finished) {
            delay(33)
            val dt = 0.033f
            val target = when {
                throttle > 0f -> 320f
                brake > 0f -> 0f
                else -> 110f
            }
            val response = if (throttle > 0f || brake > 0f) 5.0f else 1.8f
            speed += (target - speed) * response * dt
            speed = speed.coerceIn(0f, 320f)
            val movement = speed * dt * 0.018f
            distance += speed * dt * 0.045f
            travel += movement
            if (travel >= 58f) travel = 0f
            if (distance >= 180f) {
                distance = 0f
                if (lap >= 3) finished = true else lap++
            }
        }
    }

    LaunchedEffect(travel) {
        cameraNode.position = Position(x = 0f, y = 3.4f, z = 10f - travel)
    }

    Box(Modifier.fillMaxSize().background(map.sky)) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLight,
            surfaceType = SurfaceType.TextureSurface,
            autoCenterContent = false,
            autoFitContent = false
        ) {
            val car = rememberModelInstance(modelLoader, "models/car.glb")
            val start = rememberModelInstance(modelLoader, "models/track_start.glb")
            val straight = rememberModelInstance(modelLoader, "models/track_straight.glb")
            val corner45 = rememberModelInstance(modelLoader, "models/track_corner45.glb")
            val corner90 = rememberModelInstance(modelLoader, "models/track_corner90.glb")
            val banked = rememberModelInstance(modelLoader, "models/track_banked.glb")
            val guardrail = rememberModelInstance(modelLoader, "models/guardrail.glb")
            val tower = rememberModelInstance(modelLoader, "models/tower.glb")
            val timing = rememberModelInstance(modelLoader, "models/timing.glb")

            car?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 2.4f,
                    centerOrigin = Position(x = 0f, y = -1f, z = 0f),
                    position = Position(x = steering * 2.6f, y = 0f, z = 2.0f - travel),
                    rotation = Rotation(y = steering * 10f),
                    autoAnimate = true
                )
            }

            val layout = when (map.name) {
                "DESERT RING" -> listOf(0f, -18f, -34f, -50f, -66f)
                "NIGHT CIRCUIT" -> listOf(0f, -22f, -40f, -58f, -74f)
                else -> listOf(0f, -16f, -32f, -48f, -64f)
            }

            start?.let {
                ModelNode(modelInstance = it, scaleToUnits = 12f, position = Position(z = -14f + layout[0]))
            }
            straight?.let {
                ModelNode(modelInstance = it, scaleToUnits = 12f, position = Position(z = -14f + layout[1]))
            }
            corner45?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 12f,
                    position = Position(
                        x = if (map.name == "NIGHT CIRCUIT") 3f else -3f,
                        z = -14f + layout[2]
                    ),
                    rotation = Rotation(y = if (map.name == "DESERT RING") -30f else 25f)
                )
            }
            corner90?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 12f,
                    position = Position(
                        x = if (map.name == "DESERT RING") -3f else 4f,
                        z = -14f + layout[3]
                    ),
                    rotation = Rotation(y = if (map.name == "NIGHT CIRCUIT") 90f else -35f)
                )
            }
            banked?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 12f,
                    position = Position(z = -14f + layout[4]),
                    rotation = Rotation(y = if (map.name == "OCEAN GP") 180f else 0f)
                )
            }
            guardrail?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 2.5f,
                    position = Position(x = -7f, y = 0f, z = -34f)
                )
            }
            tower?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 5f,
                    position = Position(x = -9f, y = 0f, z = -52f)
                )
            }
            timing?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 10f,
                    position = Position(z = -42f)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "LAP \$lap / 3",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.background(Color(0xCC101820)).padding(12.dp, 8.dp)
            )
            Text(
                "\${speed.toInt()} KM/H",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.background(Color(0xCC101820)).padding(14.dp, 8.dp)
            )
            Button(onClick = onExit) { Text("MENU") }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("STEERING", color = Color.White, fontSize = 13.sp)
            Slider(
                modifier = Modifier.width(330.dp),
                value = steering,
                onValueChange = { steering = it },
                valueRange = -1f..1f
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                HoldButton("BRAKE", Color(0xFFB71C1C), { brake = 1f }) { brake = 0f }
                HoldButton("◀", Color(0xFF30343B), { steering = -1f }) { steering = 0f }
                HoldButton("ACCEL", Color(0xFF1565C0), { throttle = 1f }) { throttle = 0f }
                HoldButton("▶", Color(0xFF30343B), { steering = 1f }) { steering = 0f }
            }
        }

        if (finished) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINISH", color = Color.White, fontSize = 42.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onExit) { Text("BACK TO MAPS") }
                }
            }
        }
    }
}

@Composable
private fun HoldButton(
    label: String,
    color: Color,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    Box(
        Modifier
            .size(width = 108.dp, height = 76.dp)
            .background(color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onDown()
                        tryAwaitRelease()
                        onUp()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 17.sp)
    }
}
