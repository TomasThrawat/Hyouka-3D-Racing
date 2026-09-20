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
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlin.math.min

private data class RaceMap(val name: String, val accent: Color)

private val maps = listOf(
    RaceMap("CITY GP", Color(0xFF2E7D32)),
    RaceMap("DESERT RING", Color(0xFFB56B2A)),
    RaceMap("NIGHT CIRCUIT", Color(0xFF304FFE))
)

@Composable
fun RacingGame(screen: MainActivity.Screen, onScreen: (MainActivity.Screen) -> Unit) {
    var selectedMap by remember { mutableIntStateOf(0) }

    when (screen) {
        MainActivity.Screen.MENU -> MenuScreen { onScreen(MainActivity.Screen.MAPS) }
        MainActivity.Screen.MAPS -> MapScreen(
            selectedIndex = selectedMap,
            onSelect = { selectedMap = it },
            onBack = { onScreen(MainActivity.Screen.MENU) },
            onRace = { onScreen(MainActivity.Screen.RACE) }
        )
        MainActivity.Screen.RACE -> RaceScreen(maps[selectedMap]) {
            onScreen(MainActivity.Screen.MAPS)
        }
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
private fun MapScreen(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onRace: () -> Unit
) {
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
                        onClick = { onSelect(index) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (index == selectedIndex) Color.White else Color.DarkGray,
                            contentColor = if (index == selectedIndex) Color.Black else Color.White
                        )
                    ) { Text(map.name) }
                }
            }
            Text(
                "MAP " + (selectedIndex + 1) + " • 3 LAPS • 4 RACERS",
                color = Color.LightGray,
                modifier = Modifier.padding(28.dp)
            )
            Button(onClick = onRace) { Text("RACE") }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("BACK") }
        }
    }
}

@Composable
private fun RaceScreen(map: RaceMap, onExit: () -> Unit) {
    val engine = rememberEngine()
    val loader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 3.6f, z = 10f)
        rotation = Rotation(x = -12f)
    }
    val light = rememberMainLightNode(engine) { intensity = 100_000f }

    var steer by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(0f) }
    var distance by remember { mutableFloatStateOf(0f) }
    var lap by remember { mutableIntStateOf(1) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!finished) {
            delay(33)
            speed = min(1f, speed + 0.018f)
            distance += speed * 0.65f
            if (distance >= 180f) {
                distance = 0f
                if (lap == 3) finished = true else lap++
            }
            steer *= 0.92f
        }
    }

    Box(Modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = loader,
            cameraNode = cameraNode,
            mainLightNode = light,
            surfaceType = SurfaceType.TextureSurface,
            autoCenterContent = false,
            autoFitContent = false
        ) {
            // Guaranteed visible procedural race surface. This removes the unrelated
            // kitchen HDR asset that was being shown as the scene background.
            for (i in 0..11) {
                val z = -i * 18f + (distance % 18f)
                CubeNode(
                    size = Size(x = 12f, y = 0.18f, z = 18f),
                    position = Position(y = -0.25f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.25f, y = 0.04f, z = 7f),
                    position = Position(x = -2f, y = -0.14f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.25f, y = 0.04f, z = 7f),
                    position = Position(x = 2f, y = -0.14f, z = z)
                )
                CubeNode(
                    size = Size(x = 18f, y = 0.08f, z = 18f),
                    position = Position(y = -0.35f, z = z)
                )
            }

            // Player car: procedural 3D model, so the race is visible even if a
            // downloaded GLB has incompatible materials/textures.
            CubeNode(
                size = Size(x = 2.2f, y = 0.45f, z = 4.0f),
                position = Position(x = steer * 3.2f, y = 0.15f, z = 0f)
            )
            CubeNode(
                size = Size(x = 1.5f, y = 0.55f, z = 1.8f),
                position = Position(x = steer * 3.2f, y = 0.55f, z = -0.35f)
            )

            val wheelX = 1.18f
            val wheelZ = 1.15f
            listOf(
                Position(x = steer * 3.2f - wheelX, y = 0.0f, z = -wheelZ),
                Position(x = steer * 3.2f + wheelX, y = 0.0f, z = -wheelZ),
                Position(x = steer * 3.2f - wheelX, y = 0.0f, z = wheelZ),
                Position(x = steer * 3.2f + wheelX, y = 0.0f, z = wheelZ)
            ).forEach { wheel ->
                CylinderNode(
                    radius = 0.38f,
                    height = 0.28f,
                    sideCount = 16,
                    position = wheel,
                    rotation = Rotation(z = 90f)
                )
            }

            // Simple roadside scenery.
            for (i in 0..7) {
                val z = -i * 30f + (distance % 30f)
                CubeNode(
                    size = Size(x = 0.45f, y = 3.0f, z = 0.45f),
                    position = Position(x = -8f, y = 1.35f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.45f, y = 3.0f, z = 0.45f),
                    position = Position(x = 8f, y = 1.35f, z = z)
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp),
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

        // Actual steering UI.
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = { steer = (steer - 0.22f).coerceIn(-1f, 1f) },
                modifier = Modifier.size(width = 86.dp, height = 62.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC222222),
                    contentColor = Color.White
                )
            ) {
                Text("◀", fontSize = 30.sp)
            }

            Box(
                Modifier
                    .size(width = 170.dp, height = 62.dp)
                    .background(Color(0xAA111111))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { steer = 0f },
                            onDragCancel = { steer = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                steer = (steer + amount.x / 420f).coerceIn(-1f, 1f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("DRAG TO STEER", color = Color.White, fontSize = 14.sp)
            }

            Button(
                onClick = { steer = (steer + 0.22f).coerceIn(-1f, 1f) },
                modifier = Modifier.size(width = 86.dp, height = 62.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC222222),
                    contentColor = Color.White
                )
            ) {
                Text("▶", fontSize = 30.sp)
            }
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
