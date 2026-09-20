# Hyouka 3D Racing

Native Kotlin Android 3D racing game rebuilt around the reusable CircuitRush3D architecture.

## Systems
- Game state separated from rendering.
- Physics, AI, Race, and Track are independent.
- SceneView is isolated in Scene.kt.
- HUD is isolated in HudView.kt.
- GLB assets are validated during the build.

## Gameplay
- Ocean GP, Desert Ring, and Night Circuit.
- Three AI rivals.
- Throttle, brake, left and right controls.
- Three laps.
- Third-person chase camera.
