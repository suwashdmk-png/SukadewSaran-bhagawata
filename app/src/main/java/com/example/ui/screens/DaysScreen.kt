package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel

@Composable
fun DaysScreen(
    viewModel: SaptahaViewModel,
    onNavigateToTopic: (String) -> Unit
) {
    val saptahaDays = viewModel.saptahaDays
    val selectedDay by viewModel.selectedDay.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal Day Selector (1 to 7)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(saptahaDays) { day ->
                val isSelected = day.id == selectedDay.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDay(day.id) },
                    label = {
                        Text(
                            text = "दिन ${day.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Saffron,
                        selectedLabelColor = HolyIvory,
                        containerColor = WarmCream,
                        labelColor = DeepText
                    ),
                    modifier = Modifier.testTag("day_chip_${day.id}")
                )
            }
        }

        // Selected Day Details Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            border = BorderStroke(1.dp, Gold)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = selectedDay.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDay.description,
                    fontSize = 14.sp,
                    color = DeepText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtopics List under the selected day
        Text(
            text = "कथा प्रसङ्गहरू (Katha Topics)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Saffron,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(selectedDay.topics) { topic ->
                // Check if topic read has been tracked
                val isReadCompleted = completedSessions.any { it.topicId == topic.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectTopic(topic.id)
                            onNavigateToTopic(topic.id)
                        }
                        .testTag("topic_card_${topic.id}"),
                    colors = CardDefaults.cardColors(containerColor = WarmCream),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Saffron.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isReadCompleted) Icons.Default.CheckCircle else Icons.Default.Book,
                                    contentDescription = "Topic Icon",
                                    tint = if (isReadCompleted) SaffronLight else Saffron
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = topic.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topic.description,
                                    fontSize = 13.sp,
                                    color = MutedText,
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Read Topic",
                            tint = Saffron
                        )
                    }
                }
            }
        }
    }
}
