package com.tomasthrawat.hyouka3dracing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RaceHud(
    snapshot: RaceSnapshot,
    onExit: () -> Unit,
    onInput: (CarInput) -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(18.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            HudChip("LAP " + snapshot.lap + " / 3")
            HudChip(snapshot.speedKmh.toInt().toString() + " KM/H")
            Button(onClick = onExit) { Text("MENU") }
        }
    }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            HoldButton("BRAKE", Color(0xFFB71C1C)) { down ->
                onInput(CarInput(brake = if (down) 1f else 0f))
            }
            HoldButton("LEFT", Color(0xFF263238)) { down ->
                onInput(CarInput(steer = if (down) -1f else 0f))
            }
            HoldButton("ACCEL", Color(0xFF1565C0)) { down ->
                onInput(CarInput(throttle = if (down) 1f else 0f))
            }
            HoldButton("RIGHT", Color(0xFF263238)) { down ->
                onInput(CarInput(steer = if (down) 1f else 0f))
            }
        }
    }
}

@Composable
private fun HudChip(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 21.sp,
        modifier = Modifier
            .background(Color(0xCC101820), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    )
}

@Composable
private fun HoldButton(label: String, color: Color, onState: (Boolean) -> Unit) {
    Box(
        Modifier
            .size(width = 112.dp, height = 78.dp)
            .background(color, RoundedCornerShape(12.dp))
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        onState(true)
                        try {
                            tryAwaitRelease()
                        } finally {
                            onState(false)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
    }
}
