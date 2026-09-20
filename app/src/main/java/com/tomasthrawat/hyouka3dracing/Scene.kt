package com.tomasthrawat.hyouka3dracing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.DynamicSkyNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.utils.colorOf

private fun assetFor(kind: String): String = "models/track_" + kind + ".glb"

@Composable
fun RaceScene(
    modifier: Modifier,
    map: MapId,
    snapshot: RaceSnapshot
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)

    val pose = Track.pose(map, snapshot.progress, 0f)
    val headingRadians = Math.toRadians(pose.yaw.toDouble())
    val directionX = kotlin.math.sin(headingRadians).toFloat()
    val directionZ = kotlin.math.cos(headingRadians).toFloat()
    val cameraDistance = 10f

    val cameraNode = rememberCameraNode(engine) {
        position = Position(
            x = pose.x - directionX * cameraDistance,
            y = 5.2f,
            z = pose.z - directionZ * cameraDistance
        )
    }
    cameraNode.position = Position(
        x = pose.x - directionX * cameraDistance,
        y = 5.2f,
        z = pose.z - directionZ * cameraDistance
    )
    cameraNode.lookAt(Position(pose.x, 0.8f, pose.z))

    val light = rememberMainLightNode(engine) { intensity = 110_000f }
    val groundMaterial = remember(materialLoader, map) {
        val color = when (map) {
            MapId.OCEAN -> Color(0xFF315B38)
            MapId.DESERT -> Color(0xFF8A693D)
            MapId.NIGHT -> Color(0xFF20252B)
        }
        materialLoader.createColorInstance(colorOf(color), metallic = 0f, roughness = 0.92f)
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        cameraNode = cameraNode,
        mainLightNode = light,
        surfaceType = SurfaceType.TextureSurface,
        autoCenterContent = false,
        autoFitContent = false,
        cameraManipulator = null
    ) {
        DynamicSkyNode(
            timeOfDay = when (map) {
                MapId.OCEAN -> 14f
                MapId.DESERT -> 16.5f
                MapId.NIGHT -> 21f
            },
            turbidity = 2.2f,
            sunIntensity = if (map == MapId.NIGHT) 18_000f else 110_000f
        )

        PlaneNode(
            size = io.github.sceneview.math.Size(420f, 420f),
            materialInstance = groundMaterial,
            position = Position(y = -0.08f)
        )

        val player = rememberModelInstance(modelLoader, "models/car.glb")
        val ai1 = rememberModelInstance(modelLoader, "models/car_ai_1.glb")
        val ai2 = rememberModelInstance(modelLoader, "models/car_ai_2.glb")
        val ai3 = rememberModelInstance(modelLoader, "models/car_ai_3.glb")

        val trackNodes = remember(map) { Track.segments(map) }
        trackNodes.forEachIndexed { index, segment ->
            val path = when (segment.model) {
                "start" -> "models/track_start.glb"
                "straight" -> "models/track_straight.glb"
                else -> assetFor(segment.model)
            }
            val instance = rememberModelInstance(modelLoader, path)
            val position = remember(map, index) { Position(segment.x, 0f, segment.z) }
            val rotation = remember(map, index) { Rotation(y = segment.yaw) }
            instance?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = segment.scale,
                    position = position,
                    rotation = rotation,
                    autoAnimate = false
                )
            }
        }

        val guardrail = rememberModelInstance(modelLoader, "models/guardrail.glb")
        val tower = rememberModelInstance(modelLoader, "models/tower.glb")
        val timing = rememberModelInstance(modelLoader, "models/timing.glb")

        guardrail?.let {
            ModelNode(
                modelInstance = it,
                scaleToUnits = 4f,
                position = remember(map) { Position(-38f, 0f, -28f) }
            )
        }
        tower?.let {
            ModelNode(
                modelInstance = it,
                scaleToUnits = 7f,
                position = remember(map) { Position(40f, 0f, -58f) }
            )
        }
        timing?.let {
            ModelNode(
                modelInstance = it,
                scaleToUnits = 12f,
                position = remember(map) { Position(0f, 0f, 0f) }
            )
        }

        player?.let {
            ModelNode(
                modelInstance = it,
                scaleToUnits = 2.4f,
                position = Position(pose.x, 0f, pose.z),
                rotation = Rotation(y = pose.yaw),
                autoAnimate = true,
                animationLoop = true
            )
        }

        listOf(ai1, ai2, ai3).forEachIndexed { index, instance ->
            instance?.let {
                val ai = snapshot.ai[index]
                val aiPose = Track.pose(map, ai.first, ai.second)
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 2.25f,
                    position = Position(aiPose.x, 0f, aiPose.z),
                    rotation = Rotation(y = aiPose.yaw),
                    autoAnimate = true,
                    animationLoop = true
                )
            }
        }
    }
}
