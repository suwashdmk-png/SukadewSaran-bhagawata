package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KathaTopic
import com.example.ui.viewmodel.AudioSegment
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    viewModel: SaptahaViewModel,
    onBack: () -> Unit
) {
    val topicOpt by viewModel.selectedTopic.collectAsState()
    val fontScale by viewModel.readingFontScale.collectAsState()
    val readingTheme by viewModel.readingTheme.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (topicOpt == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("कृपया सूचीबाट एउटा विषय चयन गर्नुहोस्।", color = MutedText)
        }
        return
    }

    val topic = topicOpt!!

    val isBookmarked by viewModel.isTopicBookmarked(topic.id).collectAsState(initial = false)
    val isReadCompleted = completedSessions.any { it.topicId == topic.id }

    // Memorization Mode State
    var showMemorizationMode by remember { mutableStateOf(false) }
    var repeatCountTarget by remember { mutableStateOf(11) }
    var currentRepeatCount by remember { mutableStateOf(0) }

    // Auto Scroll State
    var isAutoScrollEnabled by remember { mutableStateOf(false) }
    var autoScrollSpeed by remember { mutableStateOf(1f) } // 1x, 2x, 3x
    val scrollState = rememberScrollState()

    // Inline Notes state
    var showAddNoteModal by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val allNotes by viewModel.notes.collectAsState()
    val topicNotes = remember(allNotes, topic) { allNotes.filter { it.topicId == topic.id } }

    // Apply color scheme based on selected reading theme
    val (bgThemeColor, textThemeColor, accentThemeColor, surfaceThemeColor) = when (readingTheme) {
        "warm_saffron" -> Quadruple(Color(0xFFFFF3E0), Color(0xFF4E342E), Color(0xFFE64A19), Color(0xFFFFE0B2))
        "night_mode" -> Quadruple(Color(0xFF1E1008), Color(0xFFECE0D8), Color(0xFFFFCC80), Color(0xFF2D1B10))
        else -> Quadruple(HolyIvory, DeepText, Saffron, WarmCream) // Light Ivory default
    }

    // Auto scroll handler
    LaunchedEffect(isAutoScrollEnabled, autoScrollSpeed) {
        if (isAutoScrollEnabled) {
            while (isAutoScrollEnabled && scrollState.value < scrollState.maxValue) {
                scrollState.scrollTo(scrollState.value + (autoScrollSpeed * 2).toInt())
                delay(40)
            }
            if (scrollState.value >= scrollState.maxValue) {
                isAutoScrollEnabled = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgThemeColor)
    ) {
        // Top Toolbar
        TopAppBar(
            title = {
                Text(
                    text = topic.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentThemeColor)
                }
            },
            actions = {
                // Font Resizer Panel Toggle
                var showFontPanel by remember { mutableStateOf(false) }
                IconButton(onClick = { showFontPanel = !showFontPanel }) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Adjust Font", tint = accentThemeColor)
                }

                // Bookmark Toggle
                IconButton(onClick = {
                    viewModel.toggleBookmark(
                        "topic",
                        topic.id,
                        topic.title,
                        "अध्याय कथा र श्लोक पाठ"
                    )
                    Toast.makeText(context, if (isBookmarked) "मनपर्ने सूचीबाट हटाइयो" else "मनपर्ने सूचीमा थपियो", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color.Red else accentThemeColor
                    )
                }

                // Complete toggle
                IconButton(onClick = {
                    viewModel.toggleSessionProgress(
                        dayId = topic.dayId,
                        topicId = topic.id,
                        sessionType = "reading_complete",
                        complete = !isReadCompleted
                    )
                    Toast.makeText(context, if (isReadCompleted) "अपूर्ण चिन्ह लगाइयो" else "पढाइ सम्पन्न भयो!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = if (isReadCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = "Complete Read",
                        tint = if (isReadCompleted) Color(0xFF4CAF50) else accentThemeColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = bgThemeColor)
        )

        // Dropdown Font / Theme Adjuster Panel
        AnimatedVisibility(
            visible = true,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceThemeColor)
                    .padding(12.dp)
            ) {
                // Font Size Slider (optimized for elders: scale from 1.0x to 2.2x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("अक्षरको आकार (Font Size):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.updateFontScale(it) },
                        valueRange = 0.9f..2.2f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .testTag("font_scale_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = accentThemeColor,
                            activeTrackColor = accentThemeColor
                        )
                    )
                    Text(text = "${(fontScale * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Theme selection row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("पृष्ठभूमि (Themes):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
                    Spacer(modifier = Modifier.width(8.dp))

                    ThemeOptionButton("light_ivory", "चाँदी (Ivory)", readingTheme == "light_ivory") {
                        viewModel.updateReadingTheme("light_ivory")
                    }
                    ThemeOptionButton("warm_saffron", "गेरुवा (Saffron)", readingTheme == "warm_saffron") {
                        viewModel.updateReadingTheme("warm_saffron")
                    }
                    ThemeOptionButton("night_mode", "रात्री (Night)", readingTheme == "night_mode") {
                        viewModel.updateReadingTheme("night_mode")
                    }
                }
            }
        }

        // Reading Main Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Sanskrit Shloka Box with traditional gilded golden border
            if (topic.shlokas.isNotEmpty()) {
                topic.shlokas.forEach { shloka ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceThemeColor),
                        border = BorderStroke(2.dp, Gold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "॥ संस्कृत श्लोक ॥",
                                fontSize = (14 * fontScale).sp,
                                color = accentThemeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = shloka.originalSanskrit,
                                fontSize = (22 * fontScale).sp,
                                color = accentThemeColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = (30 * fontScale).sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Quick memorizer mode activation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.playVirtualBell()
                                        showMemorizationMode = !showMemorizationMode
                                        currentRepeatCount = 0
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.School, contentDescription = "Practice", tint = accentThemeColor, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("कण्ठस्थ अभ्यास (Memorize)", color = accentThemeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Simulated Recitation Sound
                                TextButton(
                                    onClick = {
                                        viewModel.playAudio()
                                        viewModel.selectAudioSegment(AudioSegment.SHLOKA)
                                        Toast.makeText(context, "श्लोक वाचन अडियो सुरु भयो", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Hear", tint = accentThemeColor, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("श्लोक वाचन (Hear Recitation)", color = accentThemeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Memorization Mode Panel
                            AnimatedVisibility(visible = showMemorizationMode) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .background(accentThemeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "श्लोक आवृत्ति काउन्टर (Repeat Mode)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textThemeColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(onClick = { if (repeatCountTarget > 1) repeatCountTarget-- }) {
                                            Icon(Icons.Default.Remove, contentDescription = "Less", tint = textThemeColor)
                                        }
                                        Text(
                                            text = "लक्ष्य: $repeatCountTarget पटक",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textThemeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(onClick = { repeatCountTarget++ }) {
                                            Icon(Icons.Default.Add, contentDescription = "More", tint = textThemeColor)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Counter button
                                    Button(
                                        onClick = {
                                            viewModel.playVirtualBell()
                                            if (currentRepeatCount < repeatCountTarget) {
                                                currentRepeatCount++
                                                if (currentRepeatCount == repeatCountTarget) {
                                                    Toast.makeText(context, "बधाई छ! श्लोक कण्ठस्थ लक्ष्य पुरा भयो!", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentThemeColor),
                                        modifier = Modifier.testTag("memorize_repeat_button")
                                    ) {
                                        Text(
                                            text = "मेरो जप: $currentRepeatCount / $repeatCountTarget",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HolyIvory
                                        )
                                    }

                                    TextButton(onClick = { currentRepeatCount = 0 }) {
                                        Text("पुनः सुरु गर्नुहोस् (Reset)", color = textThemeColor, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Transliteration
                            Text(
                                text = "नेपाली लिप्यन्तरण (Transliteration):",
                                fontSize = (13 * fontScale).sp,
                                color = textThemeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = shloka.nepaliTransliteration,
                                fontSize = (16 * fontScale).sp,
                                color = textThemeColor,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(bottom = 8.dp)
                            )

                            // Word meaning
                            Text(
                                text = "पदार्थ (Word Meaning):",
                                fontSize = (13 * fontScale).sp,
                                color = textThemeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = shloka.wordMeaning,
                                fontSize = (15 * fontScale).sp,
                                color = textThemeColor,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(bottom = 8.dp)
                            )

                            // Nepali translation
                            Text(
                                text = "सरल नेपाली अनुवाद (Nepali Meaning):",
                                fontSize = (13 * fontScale).sp,
                                color = textThemeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = shloka.nepaliTranslation,
                                fontSize = (16 * fontScale).sp,
                                color = textThemeColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                    }
                }
            }

            // Simple Explanation
            SectionHeader("१. सरल व्याख्या (Explanation)", textThemeColor, accentThemeColor, fontScale)
            SectionBody(topic.explanation, textThemeColor, fontScale)

            // Deep Meaning
            SectionHeader("२. आध्यात्मिक रहस्य (Deep Spiritual Meaning)", textThemeColor, accentThemeColor, fontScale)
            SectionBody(topic.deepMeaning, textThemeColor, fontScale)

            // Drishtanta
            SectionHeader("३. सान्दर्भिक दृष्टान्त (Drishtanta)", textThemeColor, accentThemeColor, fontScale)
            SectionBody("📖 " + topic.drishtanta, textThemeColor, fontScale)

            // Poetry
            SectionHeader("४. गुन रत्नमालिका शैली भक्ति छन्द (Poetry)", textThemeColor, accentThemeColor, fontScale)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceThemeColor),
                border = BorderStroke(1.dp, Gold)
            ) {
                Text(
                    text = topic.poetry,
                    fontSize = (17 * fontScale).sp,
                    color = textThemeColor,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = (26 * fontScale).sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Closing Reflection
            SectionHeader("५. मनन र आजको संकल्प (Closing Reflection)", textThemeColor, accentThemeColor, fontScale)
            SectionBody("✨ " + topic.reflection, textThemeColor, fontScale)

            // Personal Notes Section for this specific Topic
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = accentThemeColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मेरो टिपोटहरू (My Notes)",
                    fontSize = (16 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor
                )
                Button(
                    onClick = { showAddNoteModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentThemeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_note_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("टिपोट थप्नुहोस्")
                }
            }

            if (topicNotes.isEmpty()) {
                Text(
                    text = "यस अध्यायमा कुनै व्यक्तिगत टिपोटहरू लेखिएको छैन।",
                    fontSize = 13.sp,
                    color = MutedText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                for (note in topicNotes) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceThemeColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = note.noteText, fontSize = (15 * fontScale).sp, color = textThemeColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Auto Scroll Floating Controller at the bottom
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceThemeColor),
            border = BorderStroke(1.dp, accentThemeColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isAutoScrollEnabled,
                        onCheckedChange = { isAutoScrollEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentThemeColor),
                        modifier = Modifier.testTag("auto_scroll_switch")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("स्वचालित स्क्रोल (Auto Scroll)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
                }

                if (isAutoScrollEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("गति (Speed):", fontSize = 12.sp, color = textThemeColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(1f, 2f, 3f).forEach { speed ->
                            FilterChip(
                                selected = autoScrollSpeed == speed,
                                onClick = { autoScrollSpeed = speed },
                                label = { Text("${speed.toInt()}x") },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Note Modal
    if (showAddNoteModal) {
        AlertDialog(
            onDismissRequest = { showAddNoteModal = false },
            title = { Text("व्यक्तिगत टिपोट (New Note)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("भगवानको बारेमा वा मनन गरेका बुँदाहरू लेख्नुहोस्...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("note_text_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.saveNote(topic.id, topic.title, noteText)
                            noteText = ""
                            showAddNoteModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("सुरक्षित गर्नुहोस्")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteModal = false }) {
                    Text("रद्द गर्नुहोस्")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, textColor: Color, accentColor: Color, fontScale: Float) {
    Column(modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)) {
        Text(
            text = title,
            fontSize = (17 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(Gold)
        )
    }
}

@Composable
fun SectionBody(text: String, textColor: Color, fontScale: Float) {
    Text(
        text = text,
        fontSize = (16 * fontScale).sp,
        color = textColor,
        lineHeight = (24 * fontScale).sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ThemeOptionButton(
    themeKey: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                1.dp,
                if (isSelected) Saffron else Color.Gray.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .background(
                if (isSelected) Saffron.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Saffron else MutedText
        )
    }
}

// Simple Quadruple container helper
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
