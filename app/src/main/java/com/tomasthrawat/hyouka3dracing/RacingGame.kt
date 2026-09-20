package com.tomasthrawat.hyouka3dracing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import io.github.sceneview.math.Size
import io.github.sceneview.node.ConeNode
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay

private data class RaceMap(
    val name: String,
    val accent: Color,
    val water: Color,
    val ground: Color
)

private val maps = listOf(
    RaceMap("OCEAN GP", Color(0xFFE53935), Color(0xFF1688D8), Color(0xFF4CAF50)),
    RaceMap("DESERT RING", Color(0xFFFF8F00), Color(0xFF4FC3F7), Color(0xFFC98A45)),
    RaceMap("NIGHT CIRCUIT", Color(0xFF7C4DFF), Color(0xFF101C3A), Color(0xFF263238))
)

@Composable
fun RacingGame(
    screen: MainActivity.Screen,
    onScreen: (MainActivity.Screen) -> Unit
) {
    var selectedMap by remember { mutableIntStateOf(0) }

    when (screen) {
        MainActivity.Screen.MENU -> MenuScreen {
            onScreen(MainActivity.Screen.MAPS)
        }
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
    Box(
        Modifier.fillMaxSize().background(Color(0xFF07111F)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HYOUKA 3D RACING", color = Color.White, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text("NATIVE KOTLIN • 3D", color = Color(0xFFB8C7D9), fontSize = 15.sp)
            Spacer(Modifier.height(28.dp))
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
                            containerColor = if (index == selectedIndex) map.accent else Color(0xFF30343B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(map.name)
                    }
                }
            }
            Text(
                "MAP " + (selectedIndex + 1) + " • 3 LAPS • ARCADE RACE",
                color = Color(0xFFB8C7D9),
                modifier = Modifier.padding(28.dp)
            )
            Button(onClick = onRace) { Text("RACE") }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
                Text("BACK")
            }
        }
    }
}

@Composable
private fun RaceScreen(
    map: RaceMap,
    onExit: () -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 4.1f, z = 10.5f)
        rotation = Rotation(x = -12f)
    }
    val mainLight = rememberMainLightNode(engine) {
        intensity = 110_000f
    }

    val road = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF343941), unlit = true)
    }
    val roadLine = remember(materialLoader) {
        materialLoader.createColorInstance(Color.White, unlit = true)
    }
    val water = remember(materialLoader) {
        materialLoader.createColorInstance(map.water, unlit = true)
    }
    val ground = remember(materialLoader) {
        materialLoader.createColorInstance(map.ground, unlit = true)
    }
    val barrierRed = remember(materialLoader) {
        materialLoader.createColorInstance(map.accent, unlit = true)
    }
    val barrierWhite = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFF2F2F2), unlit = true)
    }
    val carBody = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF00A8FF), unlit = true)
    }
    val carAccent = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFFFC107), unlit = true)
    }
    val glass = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF101820), unlit = true)
    }
    val tire = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF111111), unlit = true)
    }
    val tree = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF238B45), unlit = true)
    }
    val trunk = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF704214), unlit = true)
    }
    val gantry = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF607D8B), unlit = true)
    }
    val checkered = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFECEFF1), unlit = true)
    }

    var steer by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(280f) }
    var distance by remember { mutableFloatStateOf(0f) }
    var lap by remember { mutableIntStateOf(1) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!finished) {
            delay(33)
            val dt = 0.033f
            distance += speed * dt * 0.045f
            if (distance >= 180f) {
                distance = 0f
                if (lap >= 3) finished = true else lap++
            }
            steer *= 0.90f
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xFF71B8EA))
    ) {
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
            CubeNode(
                size = Size(x = 120f, y = 0.25f, z = 260f),
                materialInstance = water,
                position = Position(y = -1.0f, z = -105f)
            )
            CubeNode(
                size = Size(x = 80f, y = 0.5f, z = 80f),
                materialInstance = ground,
                position = Position(x = -42f, y = -0.65f, z = -58f)
            )
            CubeNode(
                size = Size(x = 75f, y = 0.5f, z = 70f),
                materialInstance = ground,
                position = Position(x = 43f, y = -0.65f, z = -92f)
            )

            for (i in 0..15) {
                val z = -i * 12f + (distance % 12f)
                CubeNode(
                    size = Size(x = 12f, y = 0.22f, z = 12f),
                    materialInstance = road,
                    position = Position(y = -0.25f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.16f, y = 0.025f, z = 5.5f),
                    materialInstance = roadLine,
                    position = Position(x = 0f, y = -0.10f, z = z)
                )

                val barrierMaterial = if (i % 2 == 0) barrierRed else barrierWhite
                CubeNode(
                    size = Size(x = 0.38f, y = 0.65f, z = 12f),
                    materialInstance = barrierMaterial,
                    position = Position(x = -6.25f, y = 0.05f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.38f, y = 0.65f, z = 12f),
                    materialInstance = barrierMaterial,
                    position = Position(x = 6.25f, y = 0.05f, z = z)
                )
            }

            for (i in 0..8) {
                val z = -i * 22f + (distance % 22f)
                CubeNode(
                    size = Size(x = 0.55f, y = 2.6f, z = 0.55f),
                    materialInstance = trunk,
                    position = Position(x = -9f, y = 1.0f, z = z)
                )
                ConeNode(
                    radius = 2.0f,
                    height = 4.4f,
                    sideCount = 8,
                    materialInstance = tree,
                    position = Position(x = -9f, y = 4.2f, z = z)
                )
                CubeNode(
                    size = Size(x = 0.55f, y = 2.6f, z = 0.55f),
                    materialInstance = trunk,
                    position = Position(x = 9f, y = 1.0f, z = z - 9f)
                )
                ConeNode(
                    radius = 2.0f,
                    height = 4.4f,
                    sideCount = 8,
                    materialInstance = tree,
                    position = Position(x = 9f, y = 4.2f, z = z - 9f)
                )
            }

            val checkpointZ = -48f + (distance % 48f)
            CubeNode(
                size = Size(x = 0.28f, y = 4.2f, z = 0.28f),
                materialInstance = gantry,
                position = Position(x = -4.8f, y = 2.0f, z = checkpointZ)
            )
            CubeNode(
                size = Size(x = 0.28f, y = 4.2f, z = 0.28f),
                materialInstance = gantry,
                position = Position(x = 4.8f, y = 2.0f, z = checkpointZ)
            )
            CubeNode(
                size = Size(x = 9.9f, y = 0.28f, z = 0.28f),
                materialInstance = gantry,
                position = Position(y = 4.0f, z = checkpointZ)
            )
            CubeNode(
                size = Size(x = 3.6f, y = 0.16f, z = 0.16f),
                materialInstance = checkered,
                position = Position(y = 3.98f, z = checkpointZ - 0.18f)
            )

            val carX = steer * 2.8f
            CubeNode(
                size = Size(x = 2.25f, y = 0.48f, z = 4.0f),
                materialInstance = carBody,
                position = Position(x = carX, y = 0.25f, z = 1.2f)
            )
            CubeNode(
                size = Size(x = 1.55f, y = 0.58f, z = 1.85f),
                materialInstance = glass,
                position = Position(x = carX, y = 0.70f, z = 0.65f)
            )
            CubeNode(
                size = Size(x = 2.05f, y = 0.12f, z = 0.42f),
                materialInstance = carAccent,
                position = Position(x = carX, y = 0.53f, z = -0.65f)
            )

            val wheelX = 1.16f
            val wheelZ = 1.15f
            listOf(
                Position(carX - wheelX, 0.02f, 1.2f - wheelZ),
                Position(carX + wheelX, 0.02f, 1.2f - wheelZ),
                Position(carX - wheelX, 0.02f, 1.2f + wheelZ),
                Position(carX + wheelX, 0.02f, 1.2f + wheelZ)
            ).forEach { wheel ->
                CylinderNode(
                    radius = 0.38f,
                    height = 0.30f,
                    sideCount = 16,
                    materialInstance = tire,
                    position = wheel,
                    rotation = Rotation(z = 90f)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "LAP " + lap + " / 3",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.background(Color(0xAA19324A)).padding(horizontal = 18.dp, vertical = 10.dp)
            )
            Text(
                "SPEED " + speed.toInt() + " KM/H",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.background(Color(0xAA19324A)).padding(horizontal = 18.dp, vertical = 10.dp)
            )
            Button(onClick = onExit) { Text("MENU") }
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            ControlButton("◀") { steer = -1f }

            Column(
                Modifier
                    .widthIn(min = 260.dp, max = 430.dp)
                    .background(Color(0xCC15191E))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("DRAG TO STEER", color = Color.White, fontSize = 14.sp)
                Slider(
                    value = speed,
                    onValueChange = { speed = it.coerceIn(0f, 320f) },
                    valueRange = 0f..320f
                )
                Text(speed.toInt().toString() + " KM/H", color = Color.White, fontSize = 16.sp)
            }

            ControlButton("▶") { steer = 1f }
        }

        if (finished) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINISH", color = Color.White, fontSize = 42.sp)
                    Text("RACE COMPLETE", color = Color.LightGray, fontSize = 18.sp)
                    Button(onClick = onExit, modifier = Modifier.padding(top = 20.dp)) {
                        Text("BACK TO MAPS")
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlButton(
    label: String,
    onSteer: () -> Unit
) {
    Box(
        Modifier
            .size(92.dp)
            .background(Color(0xDD24282D))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onSteer()
                        tryAwaitRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 34.sp)
    }
}
