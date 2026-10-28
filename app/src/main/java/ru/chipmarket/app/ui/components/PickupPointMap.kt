package ru.chipmarket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import ru.chipmarket.app.BuildConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

const val PICKUP_LAT = 62.035452
const val PICKUP_LON = 129.675474

@Composable
fun PickupPointMap(modifier: Modifier = Modifier) {
    if (BuildConfig.DGIS_MAPGL_KEY.isBlank()) {
        AsyncImage(storageUrl + "map.png", "Карта пункта получения", modifier)
        return
    }
    val fullScreen = remember { mutableStateOf(false) }
    Column(modifier) {
        DgisMap(Modifier.fillMaxWidth().weight(1f), 14)
        Button(onClick = { fullScreen.value = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Icon(Icons.Outlined.Fullscreen, null)
            Text("На весь экран", modifier = Modifier.padding(start = 8.dp))
        }
    }
    if (fullScreen.value) {
        Dialog(onDismissRequest = { fullScreen.value = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize()) {
                Column {
                    IconButton(onClick = { fullScreen.value = false }, modifier = Modifier.align(Alignment.End).padding(12.dp)) {
                        Icon(Icons.Outlined.Close, "Закрыть карту")
                    }
                    DgisMap(Modifier.fillMaxWidth().weight(1f), 15)
                }
            }
        }
    }
}

@Composable
fun DgisMap(modifier: Modifier, startZoom: Int) {
    val zoom = remember { mutableStateOf(startZoom) }
    val count = 2.0.pow(zoom.value)
    val x = (PICKUP_LON + 180) / 360 * count
    val latRad = Math.toRadians(PICKUP_LAT)
    val y = (1 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2 * count
    val tileX = floor(x).toInt()
    val tileY = floor(y).toInt()
    val tileSize = 128.dp

    BoxWithConstraints(modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF1F4))) {
        val centerX = maxWidth / 2
        val centerY = maxHeight / 2
        for (dx in -2..2) {
            for (dy in -3..3) {
                val url = "https://tile2.maps.2gis.com/v2/tiles/online_sd/${zoom.value}/${tileX + dx}/${tileY + dy}.png" +
                    "?key=${BuildConfig.DGIS_MAPGL_KEY}"
                val left = centerX + tileSize * (tileX + dx - x).toFloat()
                val top = centerY + tileSize * (tileY + dy - y).toFloat()
                AsyncImage(url, null, Modifier.offset(left, top).size(tileSize))
            }
        }
        Icon(
            Icons.Outlined.LocationOn,
            "Пункт получения ChipMarket",
            tint = Color(0xFF146EDB),
            modifier = Modifier.offset(centerX - 21.dp, centerY - 40.dp).size(42.dp)
        )
        Column(Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White, CircleShape)) {
            IconButton(onClick = { if (zoom.value < 18) zoom.value++ }) { Icon(Icons.Outlined.Add, "Приблизить", tint = Color.Black) }
            IconButton(onClick = { if (zoom.value > 3) zoom.value-- }) { Icon(Icons.Outlined.Remove, "Отдалить", tint = Color.Black) }
        }
    }
}
