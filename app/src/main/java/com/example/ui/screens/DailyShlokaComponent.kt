package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShlokaItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DailyShlokaComponent(
    viewModel: SaptahaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val saptahaDays = viewModel.saptahaDays
    val selectedDayState by viewModel.selectedDay.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var activeDayId by remember { mutableStateOf(selectedDayState.id) }
    var currentShlokaIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) } // 0: मूल श्लोक, 1: शब्दार्थ, 2: नेपाली अनुवाद

    // Chanting helper states
    var chantCount by remember { mutableStateOf(0) }
    val targetChant = 11
    var showFlowerShower by remember { mutableStateOf(false) }

    // Synchronize day selection with global state
    LaunchedEffect(selectedDayState) {
        if (activeDayId != selectedDayState.id) {
            activeDayId = selectedDayState.id
            currentShlokaIndex = 0
        }
    }

    // Load shlokas for the selected day
    val dayShlokas = remember(activeDayId) {
        val dayObj = saptahaDays.find { it.id == activeDayId }
        val list = mutableListOf<Pair<String, ShlokaItem>>() // Pair(Topic Title, Shloka)
        dayObj?.topics?.forEach { topic ->
            topic.shlokas.forEach { shloka ->
                list.add(topic.title to shloka)
            }
        }
        list
    }

    // Safe bounds check for current shloka index
    val currentShlokaPair = if (dayShlokas.isNotEmpty()) {
        val safeIndex = currentShlokaIndex.coerceIn(0, dayShlokas.size - 1)
        dayShlokas[safeIndex]
    } else null

    // Reset chant count when switching shlokas
    LaunchedEffect(activeDayId, currentShlokaIndex) {
        chantCount = 0
    }

    // Determine if the current shloka is bookmarked
    var isBookmarked by remember { mutableStateOf(false) }
    LaunchedEffect(currentShlokaPair, bookmarks) {
        currentShlokaPair?.let { (_, shloka) ->
            isBookmarked = bookmarks.any { it.itemId == shloka.id && it.itemType == "shloka" }
        }
    }

    // Play virtual bell chime on completing chant cycles
    val playChime: () -> Unit = {
        viewModel.playVirtualBell()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_shloka_component"),
        colors = CardDefaults.cardColors(containerColor = WarmCream.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Gold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Saffron.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Shloka Icon",
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "दैनिक श्लोक साधना (Daily Shloka)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "भागवत महापुराणको प्रत्येक दिनको पावन श्लोक संग्रह",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Stepper
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shloka_days_stepper"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(saptahaDays) { day ->
                    val isSelected = day.id == activeDayId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Saffron else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Saffron else Gold.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                activeDayId = day.id
                                currentShlokaIndex = 0
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("shloka_day_chip_${day.id}"),
                        contentAlignment = Alignment.Center
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

            Spacer(modifier = Modifier.height(14.dp))

            if (currentShlokaPair == null) {
                // Fallback state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "यस दिनको लागि कुनै श्लोक भेटिएन।",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                }
            } else {
                val (topicTitle, shloka) = currentShlokaPair

                // Carousel Controls if multiple shlokas exist for the day
                if (dayShlokas.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentShlokaIndex > 0) currentShlokaIndex--
                            },
                            enabled = currentShlokaIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = "Previous Shloka",
                                tint = if (currentShlokaIndex > 0) Saffron else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "श्लोक ${currentShlokaIndex + 1} / ${dayShlokas.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )

                        IconButton(
                            onClick = {
                                if (currentShlokaIndex < dayShlokas.size - 1) currentShlokaIndex++
                            },
                            enabled = currentShlokaIndex < dayShlokas.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Next Shloka",
                                tint = if (currentShlokaIndex < dayShlokas.size - 1) Saffron else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Sacred Manuscript Scroll Card (Original Sanskrit text)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFBEB), // Elegant soft cream
                                    Color(0xFFFEF3C7)  // Soft gold warm hue
                                )
                            )
                        )
                        .border(1.5.dp, Gold, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Topic Source Subtitle
                        Text(
                            text = "प्रसङ्ग: $topicTitle",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sanskrit Text with rich traditional display style
                        Text(
                            text = shloka.originalSanskrit,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("shloka_sanskrit_text")
                        )

                        // Transliteration assist
                        Text(
                            text = shloka.nepaliTransliteration,
                            fontSize = 13.sp,
                            color = DeepText.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meaning Segment Tab Layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    val tabs = listOf("नेपाली अनुवाद", "शब्दार्थ (Word Meanings)")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Saffron else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) HolyIvory else DeepText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minHeight(100.dp)
                        .testTag("shloka_details_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "shloka_tab_content"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Translate,
                                                contentDescription = "Translation",
                                                tint = Saffron,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "सरल नेपाली अर्थ:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = shloka.nepaliTranslation,
                                            fontSize = 13.sp,
                                            color = DeepText,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.testTag("shloka_nepali_translation")
                                        )
                                    }
                                }
                                1 -> {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.MenuBook,
                                                contentDescription = "Word Meanings",
                                                tint = Saffron,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "पदच्छेद र शब्दार्थ:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = shloka.wordMeaning,
                                            fontSize = 13.sp,
                                            color = DeepText,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.testTag("shloka_word_meaning")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Toolbar (Bookmark, Share, Copy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Bookmark Toggle button
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    itemType = "shloka",
                                    itemId = shloka.id,
                                    title = "दिन ${activeDayId} - श्लोक",
                                    subtitle = shloka.originalSanskrit.take(40) + "..."
                                )
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "श्लोक मनपर्ने सूचीबाट हटाइयो" else "श्लोक मनपर्ने सूचीमा थपियो!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Saffron
                            ),
                            border = BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("shloka_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBookmarked) "सुरक्षित" else "बचत गर्नुहोस्", fontSize = 12.sp)
                        }

                        // Copy Button
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val fullText = "दिन ${activeDayId} श्लोक:\n${shloka.originalSanskrit}\n\nनेपाली अनुवाद:\n${shloka.nepaliTranslation}"
                                val clip = ClipData.newPlainText("Saptaha Shloka", fullText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "श्लोक कपी गरियो!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DeepText
                            ),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("shloka_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Shloka",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "कपी", fontSize = 12.sp)
                        }
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "साझेदारी लिङ्क तयार पारिँदैछ...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Gold.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Gold, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Saffron,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SHLOKA SADHANA: INTERACTIVE CHANTING ASSISTANT ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shloka_chanting_card"),
                    colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Chant Sadhana",
                                    tint = Saffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "श्लोक जप साधना (Chanting Sadhana)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDark
                                )
                            }
                            // Reset Count option
                            if (chantCount > 0) {
                                Text(
                                    text = "रिसेट गर्नुहोस्",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedText,
                                    modifier = Modifier
                                        .clickable { chantCount = 0 }
                                        .testTag("reset_chant_button")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Chanting Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Circular Indicator representing the mala count
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { chantCount.toFloat() / targetChant.toFloat() },
                                    color = Saffron,
                                    trackColor = Gold.copy(alpha = 0.15f),
                                    strokeWidth = 6.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$chantCount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDark
                                    )
                                    Text(
                                        text = "/$targetChant",
                                        fontSize = 10.sp,
                                        color = MutedText
                                    )
                                }
                            }

                            // Interactive "जप गर्नुहोस्" Button with flower shower animation trigger
                            Button(
                                onClick = {
                                    if (chantCount < targetChant) {
                                        chantCount++
                                        if (chantCount == targetChant) {
                                            playChime()
                                            coroutineScope.launch {
                                                showFlowerShower = true
                                                delay(2500)
                                                showFlowerShower = false
                                            }
                                            Toast.makeText(context, "अद्भूत! तपाईँले आजको श्लोक ११ पटक जप पूरा गर्नुभयो।", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "जप पूरा भइसक्यो! साधना धन्य भयो।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("shloka_chant_increment_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Chant",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "श्लोक जप गर्नुहोस्",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Flower shower/Pushpa-Vrishti animation feedback
                        AnimatedVisibility(
                            visible = showFlowerShower,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "🌸 दिव्य पुष्पवृष्टि! साधनामा मङ्गल होस्! 🌸",
                                color = Saffron,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .testTag("flower_shower_text")
                            )
                        }
                    }
                }
            }
        }
    }
}
