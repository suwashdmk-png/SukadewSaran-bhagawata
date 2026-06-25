package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import com.example.data.model.KathaTopic
import com.example.data.model.SaptahaDay
import com.example.data.repository.SearchResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SaptahaViewModel,
    onNavigateToDay: (Int) -> Unit,
    onNavigateToTopic: (String) -> Unit,
    onNavigateToDevotional: () -> Unit,
    onNavigateToBookmarks: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    val isShankhaActive by viewModel.shankhaAnimationPlaying.collectAsState()
    val isBellActive by viewModel.bellAnimationPlaying.collectAsState()

    val context = LocalContext.current

    // Detect Current Session based on actual local time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (activeSessionName, activeSessionTime) = when {
        currentHour in 5..7 -> "बिहानी सत्र (Morning Session)" to "05:00 - 08:00"
        currentHour in 11..12 -> "दिउँसोको सत्र (Afternoon Session)" to "11:00 - 13:00"
        currentHour in 17..19 -> "बेलुकीको सत्र (Evening Session)" to "17:00 - 20:00"
        else -> "सत्र बाहिर (Offline Study)" to "सदा स्मरण"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Devotional Welcome Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Logo Avatar representing the Meditating Yogi/Sadhu
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(SaffronLight, SaffronDark)
                                        )
                                    )
                                    .border(1.5.dp, Gold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Yogi Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column {
                                Text(
                                    text = "हरे कृष्ण, स्वागत छ!",
                                    fontSize = 16.sp,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "श्रीमद्भागवत साधना",
                                    fontSize = 22.sp,
                                    color = HolyIvory,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        // Temple Bell Quick Action
                        IconButton(
                            onClick = { viewModel.playVirtualBell() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Gold.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                .border(1.dp, Gold, RoundedCornerShape(24.dp))
                                .testTag("temple_bell_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Play Temple Bell",
                                tint = if (isBellActive) SaffronLight else Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = HolyIvory.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Inspiration Quotes
                    Text(
                        text = "दैनिक प्रेरणा (Daily Inspiration)",
                        fontSize = 12.sp,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“श्रवणं कीर्तनं विष्णोः स्मरणं पादसेवनम्।\nअर्चनं वन्दनं दास्यं सख्यमात्मनिवेदनम्॥”",
                        fontSize = 16.sp,
                        color = HolyIvory,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— नवधा भक्ति, श्रीमद्भागवत महापुराण",
                        fontSize = 12.sp,
                        color = HolyIvory.copy(alpha = 0.8f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Search Bar for Shlokas & Days
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("कथा, श्लोक वा अध्याय खोज्नुहोस्...", color = MutedText) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Saffron) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Saffron)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Gold,
                    focusedContainerColor = WarmCream.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        // Search Results Panel
        if (searchQuery.isNotEmpty()) {
            if (searchResults.isEmpty()) {
                item {
                    Text(
                        text = "कुनै नतिजा फेला परेन।",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MutedText
                    )
                }
            } else {
                item {
                    Text(
                        text = "खोजिएका नतिजाहरू (${searchResults.size}):",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                }
                items(searchResults) { result ->
                    when (result) {
                        is SearchResult.DayResult -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToDay(result.day.id) },
                                colors = CardDefaults.cardColors(containerColor = WarmCream)
                            ) {
                                ListTile(
                                    title = result.day.name,
                                    subtitle = result.day.description,
                                    icon = Icons.Default.DateRange
                                )
                            }
                        }
                        is SearchResult.TopicResult -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTopic(result.topic.id) },
                                colors = CardDefaults.cardColors(containerColor = WarmCream)
                            ) {
                                ListTile(
                                    title = result.topic.title,
                                    subtitle = result.topic.description,
                                    icon = Icons.Default.Book
                                )
                            }
                        }
                        is SearchResult.ShlokaResult -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTopic(result.topicId) },
                                colors = CardDefaults.cardColors(containerColor = WarmCream)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "Shloka", tint = Saffron, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "श्लोक: ${result.topicTitle}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = result.shloka.originalSanskrit, style = MaterialTheme.typography.bodyMedium, color = SaffronDark, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = result.shloka.nepaliTranslation, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Normal Dashboard when not searching
            // Daily Time Scheduler Session Tracker
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarmCream),
                    border = BorderStroke(1.dp, Gold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = Saffron
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "दैनिक सत्र तालिका (Scheduler)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Saffron.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "सक्रिय",
                                    fontSize = 12.sp,
                                    color = Saffron,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Display scheduler entries
                        val sessionTypes = listOf(
                            Triple("morning", "🌅 बिहानी सत्र", "05:00 - 08:00"),
                            Triple("afternoon", "☀️ दिउँसोको सत्र", "11:00 - 13:00"),
                            Triple("evening", "🌃 बेलुकीको सत्र", "17:00 - 20:00")
                        )

                        sessionTypes.forEach { (typeKey, title, timeStr) ->
                            val isCompleted = completedSessions.any { it.sessionType == typeKey }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(
                                        if (activeSessionName.contains(title.substring(2))) Gold.copy(alpha = 0.1f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepText
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 13.sp,
                                        color = MutedText
                                    )
                                }

                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { complete ->
                                        viewModel.toggleSessionProgress(
                                            dayId = 1,
                                            topicId = "daily_scheduler",
                                            sessionType = typeKey,
                                            complete = complete
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Saffron,
                                        uncheckedColor = MutedText
                                    ),
                                    modifier = Modifier.testTag("session_checkbox_${typeKey}")
                                )
                            }
                        }
                    }
                }
            }

            // Quick Devotional Actions
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "भक्ति साधनहरू (Spiritual Tools)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = "आरती / शङ्ख",
                        icon = Icons.Default.LibraryMusic,
                        color = Saffron,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_button_devotional"),
                        onClick = onNavigateToDevotional
                    )

                    QuickActionButton(
                        title = "जप काउन्टर",
                        icon = Icons.Default.AddCircle,
                        color = GoldDark,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_button_jap"),
                        onClick = onNavigateToDevotional
                    )

                    QuickActionButton(
                        title = "मनपर्ने र नोट",
                        icon = Icons.Default.Bookmarks,
                        color = SaffronLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_button_notes"),
                        onClick = onNavigateToBookmarks
                    )
                }
            }

            // Daily Katha Schedule Component (Interactive 7-day schedule & summaries)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                DailyKathaComponent(
                    viewModel = viewModel,
                    onNavigateToTopic = onNavigateToTopic,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ListTile(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Saffron,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepText)
            Text(text = subtitle, fontSize = 13.sp, color = MutedText, maxLines = 1)
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WarmCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText,
                textAlign = TextAlign.Center
            )
        }
    }
}
