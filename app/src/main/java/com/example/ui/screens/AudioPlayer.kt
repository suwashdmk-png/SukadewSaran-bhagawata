package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KathaTopic
import com.example.ui.theme.*
import com.example.ui.viewmodel.AudioSegment
import com.example.ui.viewmodel.SaptahaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayer(
    viewModel: SaptahaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ViewModel States
    val audioState by viewModel.audioState.collectAsState()
    val saptahaDays = viewModel.saptahaDays
    val selectedDay by viewModel.selectedDay.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()

    // Local Player UI States
    var selectedDayId by remember { mutableStateOf(selectedDay.id) }
    val dayTopics = remember(selectedDayId) {
        saptahaDays.find { it.id == selectedDayId }?.topics ?: emptyList()
    }
    var selectedTopicObj by remember(selectedDayId) {
        mutableStateOf(dayTopics.firstOrNull())
    }

    // Downloading/Fetching simulation states
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadSpeedStr by remember { mutableStateOf("0 KB/s") }
    var streamQuality by remember { mutableStateOf("192 kbps (Standard)") }
    var isMuted by remember { mutableStateOf(false) }

    // Waveform bounce animations
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(350, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(550, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w3"
    )
    val waveHeight4 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w4"
    )

    // Pulsing and spinning arts
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin_rotation"
    )

    // Sync selected day and topic with global viewmodel selection when loaded
    LaunchedEffect(selectedDay) {
        selectedDayId = selectedDay.id
    }

    // Triggered whenever a new topic is loaded or clicked
    val triggerFetchAndPlay: (KathaTopic) -> Unit = { topic ->
        coroutineScope.launch {
            isDownloading = true
            downloadProgress = 0f
            viewModel.pauseAudio()

            // Realistic downloading simulation steps
            val sizes = listOf(11.4, 14.2, 9.8, 16.5, 12.1, 15.3, 10.9)
            val baseSize = sizes.getOrElse(topic.dayId - 1) { 12.0 }
            
            while (downloadProgress < 1.0f) {
                delay(120)
                downloadProgress += Random.nextFloat() * 0.15f
                if (downloadProgress > 1.0f) downloadProgress = 1.0f
                
                val currentSpeed = Random.nextInt(850, 1800)
                downloadSpeedStr = "$currentSpeed KB/s"
            }
            delay(200) // complete hook delay
            isDownloading = false
            
            // Set track in viewmodel and start playing immediately
            viewModel.selectTopic(topic.id)
            viewModel.playAudio()
            Toast.makeText(context, "${topic.title} अडियो वाचन सफलतापूर्वक लोड भयो!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECTION 1: HEADER & SELECTORS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("audio_player_selector_card"),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            border = BorderStroke(1.dp, Gold)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Section Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio Icon",
                        tint = Saffron,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "कथा वाचन सङ्ग्रह (Katha Audio Library)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Selector Chips Row
                Text(
                    text = "दिन छनोट गर्नुहोस् (Select Day):",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(saptahaDays) { day ->
                        val isSelected = day.id == selectedDayId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Saffron else Color.White)
                                .border(1.dp, if (isSelected) Saffron else Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedDayId = day.id
                                    selectedTopicObj = day.topics.firstOrNull()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "दिन ${day.id}",
                                color = if (isSelected) HolyIvory else DeepText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Topic selection dropdown or row list
                Text(
                    text = "कथा प्रसङ्ग छनोट गर्नुहोस् (Select Topic):",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                dayTopics.forEach { topic ->
                    val isSelected = selectedTopicObj?.id == topic.id
                    val isCompleted = completedSessions.any { it.topicId == topic.id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Saffron.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Saffron else Gold.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTopicObj = topic }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedTopicObj = topic },
                            colors = RadioButtonDefaults.colors(selectedColor = Saffron)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = topic.description,
                                fontSize = 11.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed Listening",
                                tint = SaffronLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 2: STREAMING / DOWNLOAD STATUS ---
        AnimatedVisibility(
            visible = isDownloading,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("audio_downloader_progress"),
                colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(20.dp),
                                color = Saffron,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "कथा अडियो डाउनलोड हुँदैछ...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                        }
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SaffronDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Saffron,
                        trackColor = Saffron.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "गति: $downloadSpeedStr",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                        Text(
                            text = "सर्भर: कथा क्लाउड CDN #2",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }

        // Action button to download and play the selected track if not loaded
        val isCurrentPlayingSelected = selectedTopicObj != null && audioState.currentTopicId == selectedTopicObj?.id
        
        if (!isCurrentPlayingSelected && selectedTopicObj != null) {
            Button(
                onClick = { triggerFetchAndPlay(selectedTopicObj!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("fetch_audio_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${selectedTopicObj?.title} वाचन लोड र प्ले गर्नुहोस्",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // --- SECTION 3: THE MAIN SACRED AUDIO PLAYER DASHBOARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_player_main_dashboard"),
            colors = CardDefaults.cardColors(containerColor = SpiritualDarkBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Gold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Streaming Info Tag
                Row(
                    modifier = Modifier
                        .background(SpiritualDarkSurface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (audioState.isPlaying) Color.Green else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (audioState.isPlaying) "लाइक स्ट्रिम: $streamQuality" else "स्ट्रिम तयार छ",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful Concentric Spinning/Pulsing Sacred Mandala
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (audioState.isPlaying) pulseScale else 1.0f)
                        .drawBehind {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2

                            // Cosmic red-saffron radial background
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(SaffronLight, SaffronDark)
                                ),
                                radius = radius
                            )

                            // Sacred golden rings
                            drawCircle(color = Gold, radius = radius - 4, style = Stroke(width = 2f))
                            drawCircle(color = GoldLight.copy(alpha = 0.5f), radius = radius - 10, style = Stroke(width = 1f))

                            // Spinning spiritual halo spikes
                            rotate(if (audioState.isPlaying) spinRotation else 0f, center) {
                                for (i in 0 until 8) {
                                    val angle = i * 45f
                                    rotate(angle, center) {
                                        drawLine(
                                            color = Gold,
                                            start = Offset(center.x, center.y - radius + 15),
                                            end = Offset(center.x, center.y - radius + 4),
                                            strokeWidth = 2f
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
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Audio Waveform Visualizer
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeHeight1 = if (audioState.isPlaying) waveHeight1 else 0.15f
                    val activeHeight2 = if (audioState.isPlaying) waveHeight2 else 0.25f
                    val activeHeight3 = if (audioState.isPlaying) waveHeight3 else 0.10f
                    val activeHeight4 = if (audioState.isPlaying) waveHeight4 else 0.20f

                    val waveColors = listOf(Gold, GoldLight, SaffronLight, Gold)
                    
                    for (i in 0 until 18) {
                        val currentHeight = when (i % 4) {
                            0 -> activeHeight1
                            1 -> activeHeight2
                            2 -> activeHeight3
                            else -> activeHeight4
                        }
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight(currentHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(waveColors[i % waveColors.size])
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Titles & Metadata
                Text(
                    text = audioState.currentTopicTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HolyIvory,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "श्रीमद्भागवत सप्ताह • ${audioState.currentDayName}",
                    fontSize = 12.sp,
                    color = GoldLight,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "वाचक: स्वामी कृष्णदास जी महाराज",
                    fontSize = 11.sp,
                    color = HolyIvory.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Timeline Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = audioState.progress,
                        onValueChange = { viewModel.seekTo(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Gold.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(audioState.currentTimeSec),
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                        Text(
                            text = formatTime(audioState.totalTimeSec),
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segment Row Selector (Mantra, Katha, Shloka, Prayer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpiritualDarkSurface, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AudioSegment.values().forEach { segment ->
                        val isSelected = audioState.currentSegment == segment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Saffron else Color.Transparent)
                                .clickable { viewModel.selectAudioSegment(segment) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (segment == AudioSegment.MANTRA) "मन्त्र" else if (segment == AudioSegment.KATHA) "कथा" else if (segment == AudioSegment.SHLOKA) "श्लोक" else "प्रार्थना",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) HolyIvory else GoldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Buttons (Prev Day, Rewind 10s, Play/Pause, Fast-forward 10s, Next Day)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev Topic Button
                    IconButton(
                        onClick = {
                            val prevDayId = if (selectedDayId > 1) selectedDayId - 1 else 7
                            val prevDayObj = saptahaDays.find { it.id == prevDayId }
                            prevDayObj?.topics?.firstOrNull()?.let {
                                selectedDayId = prevDayId
                                selectedTopicObj = it
                                triggerFetchAndPlay(it)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous Day",
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Rewind 10s
                    IconButton(
                        onClick = {
                            val currentSec = audioState.currentTimeSec
                            val targetSec = (currentSec - 10).coerceAtLeast(0)
                            val progress = targetSec.toFloat() / audioState.totalTimeSec.toFloat()
                            viewModel.seekTo(progress)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = GoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Large Round Play/Pause
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Gold)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("audio_player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "Pause" else "Play",
                            tint = SpiritualDarkBg,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Fast Forward 10s
                    IconButton(
                        onClick = {
                            val currentSec = audioState.currentTimeSec
                            val targetSec = (currentSec + 10).coerceAtMost(audioState.totalTimeSec)
                            val progress = targetSec.toFloat() / audioState.totalTimeSec.toFloat()
                            viewModel.seekTo(progress)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = "Forward 10s",
                            tint = GoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Topic Button
                    IconButton(
                        onClick = {
                            val nextDayId = if (selectedDayId < 7) selectedDayId + 1 else 1
                            val nextDayObj = saptahaDays.find { it.id == nextDayId }
                            nextDayObj?.topics?.firstOrNull()?.let {
                                selectedDayId = nextDayId
                                selectedTopicObj = it
                                triggerFetchAndPlay(it)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Day",
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Preferences & Toggles Row (Speed, Sleep Timer, Stream Quality, Mute, Bookmark, Mark Finished)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Playback Speed Button
                    TextButton(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = "Speed", tint = GoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${audioState.playbackSpeed}x", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Sleep Timer Button
                    TextButton(
                        onClick = {
                            val currentVal = audioState.sleepTimerMinutesLeft
                            val nextVal = when (currentVal) {
                                null -> 15
                                15 -> 30
                                30 -> 60
                                else -> null
                            }
                            viewModel.setSleepTimer(nextVal)
                            Toast.makeText(context, if (nextVal != null) "स्लीप टाइमर $nextVal मिनेटमा सेट भयो" else "स्लीप टाइमर बन्द भयो", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = "Sleep Timer", tint = GoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (audioState.sleepTimerMinutesLeft != null) "${audioState.sleepTimerMinutesLeft}m" else "Timer",
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. Audio Quality Settings Toggle
                    TextButton(
                        onClick = {
                            if (streamQuality.startsWith("192")) {
                                streamQuality = "320 kbps (High HD)"
                                Toast.makeText(context, "गुणस्तर: 320kbps HD मा सेट भयो", Toast.LENGTH_SHORT).show()
                            } else if (streamQuality.startsWith("320")) {
                                streamQuality = "64 kbps (Eco)"
                                Toast.makeText(context, "गुणस्तर: 64kbps डाटा बचतमा सेट भयो", Toast.LENGTH_SHORT).show()
                            } else {
                                streamQuality = "192 kbps (Standard)"
                                Toast.makeText(context, "गुणस्तर: 192kbps मध्यममा सेट भयो", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = "Quality", tint = GoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (streamQuality.startsWith("320")) "HD" else if (streamQuality.startsWith("64")) "Eco" else "STD",
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 4. Bookmark Topic / Audio Timestamp
                    IconButton(
                        onClick = {
                            viewModel.toggleBookmark(
                                "audio_bookmark",
                                audioState.currentTopicId,
                                audioState.currentTopicTitle,
                                "अडियो वाचन: ${audioState.currentSegment.label} - ${audioState.currentDayName}"
                            )
                            Toast.makeText(context, "वर्तमान कथा वाचन अडियो बुकमार्क गरियो!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmark",
                            tint = Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 5. Complete Current Section Toggle
                    IconButton(
                        onClick = {
                            selectedTopicObj?.let { topic ->
                                val isCompleted = completedSessions.any { it.topicId == topic.id }
                                viewModel.toggleSessionProgress(
                                    dayId = topic.dayId,
                                    topicId = topic.id,
                                    sessionType = "afternoon",
                                    complete = !isCompleted
                                )
                                Toast.makeText(
                                    context,
                                    if (!isCompleted) "कथा खण्ड समाप्त भएको चिह्नित गरियो!" else "कथा खण्ड असमाप्त चिह्नित गरियो",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        val isCompleted = completedSessions.any { it.topicId == (selectedTopicObj?.id ?: "") }
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = "Complete Status",
                            tint = if (isCompleted) Color.Green else GoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ambient Background Sound Mixers
                Divider(color = Gold.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleTanpura(!audioState.isTanpuraBgEnabled) }
                    ) {
                        Checkbox(
                            checked = audioState.isTanpuraBgEnabled,
                            onCheckedChange = { viewModel.toggleTanpura(it) },
                            colors = CheckboxDefaults.colors(checkedColor = Gold, uncheckedColor = GoldLight.copy(alpha = 0.5f))
                        )
                        Text("तानपुरा ध्वनि", color = HolyIvory, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleFlute(!audioState.isFluteBgEnabled) }
                    ) {
                        Checkbox(
                            checked = audioState.isFluteBgEnabled,
                            onCheckedChange = { viewModel.toggleFlute(it) },
                            colors = CheckboxDefaults.colors(checkedColor = Gold, uncheckedColor = GoldLight.copy(alpha = 0.5f))
                        )
                        Text("बाँसुरी ध्वनि", color = HolyIvory, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
