package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SaptahaViewModel) {
    val audioState by viewModel.audioState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Offline Download State
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }

    // Admin Panel Edit states
    var showAdminPanel by remember { mutableStateOf(false) }
    var selectedAdminTopicId by remember { mutableStateOf("day1_topic1") }
    var adminEditDescription by remember { mutableStateOf("") }

    // Alarm scheduler simulation
    var morningAlarmEnabled by remember { mutableStateOf(true) }
    var afternoonAlarmEnabled by remember { mutableStateOf(false) }
    var eveningAlarmEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AUDIO PREFERENCES
        Text(
            text = "अडियो प्राथमिकता (Audio Preferences)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Saffron
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            border = BorderStroke(1.dp, Gold)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sleep Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = "Sleep", tint = Saffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("स्लीप टाइमर (Sleep Timer):", fontWeight = FontWeight.Bold)
                    }

                    val sleepLabel = if (audioState.sleepTimerMinutesLeft != null) {
                        "${audioState.sleepTimerMinutesLeft} मि बाँकी"
                    } else {
                        "बन्द"
                    }
                    Text(text = sleepLabel, fontWeight = FontWeight.Bold, color = Saffron)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(null, 15, 30, 60).forEach { mins ->
                        FilterChip(
                            selected = audioState.sleepTimerMinutesLeft == mins,
                            onClick = {
                                viewModel.setSleepTimer(mins)
                                Toast.makeText(context, if (mins != null) "स्लीप टाइमर $mins मिनेटमा सेट भयो" else "स्लीप टाइमर बन्द भयो", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(if (mins == null) "बन्द" else "${mins} मिनेट") },
                            modifier = Modifier.testTag("sleep_chip_${mins ?: "off"}")
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Playback speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Saffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("वाचन गति (Speed Control):", fontWeight = FontWeight.Bold)
                    }
                    Text(text = "${audioState.playbackSpeed}x", fontWeight = FontWeight.Bold, color = Saffron)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                        FilterChip(
                            selected = audioState.playbackSpeed == speed,
                            onClick = { viewModel.changePlaybackSpeed(speed) },
                            label = { Text("${speed}x") },
                            modifier = Modifier.testTag("speed_chip_${speed}")
                        )
                    }
                }
            }
        }

        // DAILY TIME SCHEDULER ALARMS
        Text(
            text = "दैनिक सूचना (Session Reminders)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Saffron
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReminderAlarmRow("बिहानी सत्र घण्टी (05:00 AM)", morningAlarmEnabled) {
                    morningAlarmEnabled = it
                    Toast.makeText(context, if (it) "बिहानी सत्र रिमाइन्डर सेट भयो" else "रिमाइन्डर बन्द भयो", Toast.LENGTH_SHORT).show()
                }
                ReminderAlarmRow("दिउँसोको सत्र घण्टी (11:00 AM)", afternoonAlarmEnabled) {
                    afternoonAlarmEnabled = it
                    Toast.makeText(context, if (it) "दिउँसोको सत्र रिमाइन्डर सेट भयो" else "रिमाइन्डर बन्द भयो", Toast.LENGTH_SHORT).show()
                }
                ReminderAlarmRow("बेलुकीको सत्र घण्टी (05:00 PM)", eveningAlarmEnabled) {
                    eveningAlarmEnabled = it
                    Toast.makeText(context, if (it) "बेलुकीको सत्र रिमाइन्डर सेट भयो" else "रिमाइन्डर बन्द भयो", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // OFFLINE SUPPORT (DOWNLOAD MANAGER)
        Text(
            text = "अफलाइन सङ्ग्रह (Offline Download)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Saffron
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("७ दिनको सम्पूर्ण कथा र अडियो", fontWeight = FontWeight.Bold)
                        Text("कुल आकार: ४८ MB", fontSize = 13.sp, color = MutedText)
                    }

                    if (!isDownloading && downloadProgress == 0f) {
                        Button(
                            onClick = {
                                isDownloading = true
                                coroutineScope.launch {
                                    while (downloadProgress < 1.0f) {
                                        delay(300)
                                        downloadProgress += 0.1f
                                    }
                                    isDownloading = false
                                    Toast.makeText(context, "सबै कथाहरू सफलतापूर्वक अफलाइन डाउनलोड भए!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            modifier = Modifier.testTag("download_all_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("डाउनलोड")
                        }
                    } else if (isDownloading) {
                        CircularProgressIndicator(progress = { downloadProgress }, color = Saffron)
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("डाउनलोड भयो", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Saffron,
                        trackColor = Gold.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // ADMIN PANEL SIMULATION
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showAdminPanel = !showAdminPanel },
            colors = ButtonDefaults.buttonColors(containerColor = SaffronDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_panel_toggle_button")
        ) {
            Icon(Icons.Default.Security, contentDescription = "Admin")
            Spacer(modifier = Modifier.width(8.dp))
            Text("प्रशासक प्यानल (Admin Panel Edit Texts)")
        }

        AnimatedVisibility(visible = showAdminPanel) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WarmCream),
                border = BorderStroke(1.dp, SaffronDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("कथा पाठ सम्पादन (Update Saptaha Content)", fontWeight = FontWeight.Bold, color = SaffronDark, fontSize = 16.sp)

                    // Select topic to edit
                    var isDropdownExpanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { isDropdownExpanded = true }) {
                            Text("सम्पादन गर्ने खण्ड चयन गर्नुहोस्")
                        }
                        DropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                            viewModel.saptahaDays.flatMap { it.topics }.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic.title) },
                                    onClick = {
                                        selectedAdminTopicId = topic.id
                                        adminEditDescription = topic.explanation
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val selectedTopic = viewModel.saptahaDays.flatMap { it.topics }.find { it.id == selectedAdminTopicId }
                    if (selectedTopic != null) {
                        Text("हाल सम्पादन गर्दै: ${selectedTopic.title}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        if (adminEditDescription.isEmpty()) {
                            adminEditDescription = selectedTopic.explanation
                        }

                        OutlinedTextField(
                            value = adminEditDescription,
                            onValueChange = { adminEditDescription = it },
                            label = { Text("सरल व्याख्या परिमार्जन") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("admin_edit_text_input")
                        )

                        Button(
                            onClick = {
                                // Simulate saving to static data
                                viewModel.selectTopic(selectedAdminTopicId)
                                Toast.makeText(context, "सफलतापूर्वक अद्यावधिक भयो (Saved to simulated database)!", Toast.LENGTH_SHORT).show()
                                showAdminPanel = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("admin_save_button")
                        ) {
                            Text("परिवर्तन बचत गर्नुहोस् (Publish)")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ReminderAlarmRow(label: String, isEnabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = DeepText)
        Switch(
            checked = isEnabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Saffron),
            modifier = Modifier.testTag("alarm_switch_${label.substring(0, 3)}")
        )
    }
}
