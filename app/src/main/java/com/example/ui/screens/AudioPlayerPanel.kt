package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AudioSegment
import com.example.ui.viewmodel.SaptahaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerPanel(viewModel: SaptahaViewModel) {
    val audioState by viewModel.audioState.collectAsState()
    val context = LocalContext.current

    var isExpanded by remember { mutableStateOf(false) }

    // Dynamic rotation for rotating mandala artwork during audio play
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "art_rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "art_pulse"
    )

    if (!audioState.showMiniPlayer) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_player_overlay")
    ) {
        if (!isExpanded) {
            // --- MINI PLAYER ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { isExpanded = true }
                    .testTag("mini_player"),
                colors = CardDefaults.cardColors(containerColor = SaffronDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, Gold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Tiny spinning icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(scale)
                                .drawBehind {
                                    val radius = size.width / 2
                                    val center = Offset(size.width / 2, size.height / 2)
                                    rotate(if (audioState.isPlaying) rotation else 0f, center) {
                                        drawCircle(color = Gold, radius = radius - 4, style = Stroke(width = 2f))
                                        drawLine(color = Gold, start = center, end = Offset(center.x, center.y - radius + 4), strokeWidth = 2f)
                                    }
                                }
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ॐ", fontSize = 16.sp, color = Gold, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = audioState.currentTopicTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HolyIvory,
                                maxLines = 1
                            )
                            Text(
                                text = "${audioState.currentDayName} • ${audioState.currentSegment.label}",
                                fontSize = 11.sp,
                                color = GoldLight,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Play/Pause
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier.testTag("mini_play_pause")
                        ) {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Close mini player
                        IconButton(
                            onClick = {
                                viewModel.pauseAudio()
                                viewModel.seekTo(0f)
                                // Close
                                // We can reset state
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = HolyIvory.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        } else {
            // --- FULL SCREEN EXPANDED PLAYER MODAL ---
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("expanded_audio_player"),
                color = SpiritualDarkBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isExpanded = false }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Gold, modifier = Modifier.size(32.dp))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "श्रीमद्भागवत सप्ताह सङ्गीत",
                                fontSize = 12.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = audioState.currentDayName,
                                fontSize = 15.sp,
                                color = HolyIvory,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bookmark current play time
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    "audio_bookmark",
                                    audioState.currentTopicId,
                                    audioState.currentTopicTitle,
                                    "अडियो बुकमार्क: ${audioState.currentSegment.label}"
                                )
                                Toast.makeText(context, "वर्तमान अडियो ट्र्याक बुकमार्क भयो!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("audio_bookmark_button")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Bookmark Track", tint = Gold)
                        }
                    }

                    // Rotating sacred mandala artwork in the center
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(scale)
                            .drawBehind {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.width / 2

                                // Beautiful radial cosmic saffron background
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(SaffronLight, SaffronDark)
                                    ),
                                    radius = radius
                                )

                                // Gilded golden framing rings
                                drawCircle(color = Gold, radius = radius - 5, style = Stroke(width = 3f))
                                drawCircle(color = GoldLight, radius = radius - 15, style = Stroke(width = 1f))

                                // Rotating temple petals
                                rotate(if (audioState.isPlaying) rotation else 0f, center) {
                                    for (i in 0 until 16) {
                                        val angle = i * (360f / 16)
                                        rotate(angle, center) {
                                            drawArc(
                                                color = Gold,
                                                startAngle = -10f,
                                                sweepAngle = 20f,
                                                useCenter = false,
                                                topLeft = Offset(20f, 20f),
                                                size = size.copy(
                                                    width = size.width - 40,
                                                    height = size.height - 40
                                                ),
                                                style = Stroke(width = 1.5f)
                                            )
                                        }
                                    }
                                }
                            }
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ॐ",
                            fontSize = 90.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Titles
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = audioState.currentTopicTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HolyIvory,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = audioState.currentSegment.label,
                            fontSize = 14.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Playback Segment Selectors (Mantra, Katha, Recitation, Prayer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpiritualDarkSurface, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AudioSegment.values().forEach { seg ->
                            val isSelected = audioState.currentSegment == seg
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Saffron else Color.Transparent)
                                    .clickable { viewModel.selectAudioSegment(seg) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = seg.name.substring(0, 3) + " " + if (seg == AudioSegment.MANTRA) "मन्त्र" else if (seg == AudioSegment.KATHA) "कथा" else if (seg == AudioSegment.SHLOKA) "श्लोक" else "प्रार्थना",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) HolyIvory else GoldLight
                                )
                            }
                        }
                    }

                    // Progress Timeline Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = audioState.progress,
                            onValueChange = { viewModel.seekTo(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = Gold.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timeline_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(audioState.currentTimeSec),
                                fontSize = 12.sp,
                                color = GoldLight
                            )
                            Text(
                                text = formatTime(audioState.totalTimeSec),
                                fontSize = 12.sp,
                                color = GoldLight
                            )
                        }
                    }

                    // Ambient Background Sound Toggles (Tanpura / Flute)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AmbientToggle(
                            label = "तानपुरा (Tanpura)",
                            isEnabled = audioState.isTanpuraBgEnabled,
                            onToggle = { viewModel.toggleTanpura(it) }
                        )

                        AmbientToggle(
                            label = "बाँसुरी (Flute)",
                            isEnabled = audioState.isFluteBgEnabled,
                            onToggle = { viewModel.toggleFlute(it) }
                        )
                    }

                    // Control Buttons (Prev, Play, Next) and preferences indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Change playback speed
                        IconButton(
                            onClick = {
                                val nextSpeed = when (audioState.playbackSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 0.75f
                                    else -> 1.0f
                                }
                                viewModel.changePlaybackSpeed(nextSpeed)
                            }
                        ) {
                            Text(
                                text = "${audioState.playbackSpeed}x",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Big Play / Pause Button
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(72.dp)
                                .background(Gold, CircleShape)
                                .testTag("full_player_play_pause")
                        ) {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = SpiritualDarkBg,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Sleep timer button
                        IconButton(
                            onClick = {
                                val currentMins = audioState.sleepTimerMinutesLeft
                                val nextMins = when (currentMins) {
                                    null -> 15
                                    15 -> 30
                                    30 -> 60
                                    else -> null
                                }
                                viewModel.setSleepTimer(nextMins)
                                Toast.makeText(context, if (nextMins != null) "स्लीप टाइमर $nextMins मिनेटमा सेट भयो" else "स्लीप टाइमर बन्द भयो", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Gold)
                                if (audioState.sleepTimerMinutesLeft != null) {
                                    Text(
                                        text = "${audioState.sleepTimerMinutesLeft}m",
                                        fontSize = 10.sp,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AmbientToggle(label: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = Gold,
                uncheckedColor = GoldLight.copy(alpha = 0.5f)
            )
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = HolyIvory,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatTime(sec: Int): String {
    val minutes = sec / 60
    val seconds = sec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
