package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KathaTopic
import com.example.data.model.SaptahaDay
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel
import kotlinx.coroutines.delay

@Composable
fun DailyKathaComponent(
    viewModel: SaptahaViewModel,
    onNavigateToTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val saptahaDays = viewModel.saptahaDays
    val selectedDayState by viewModel.selectedDay.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()

    var activeDayId by remember { mutableStateOf(selectedDayState.id) }
    var isFetching by remember { mutableStateOf(false) }

    // Synchronize inner state with viewModel's selected day
    LaunchedEffect(selectedDayState) {
        activeDayId = selectedDayState.id
    }

    // Simulate database/API fetching transition when changing days
    LaunchedEffect(activeDayId) {
        isFetching = true
        delay(400) // Beautiful organic fetching delay
        viewModel.selectDay(activeDayId)
        isFetching = false
    }

    // Detailed structured summaries for each of the 7 Saptaha days
    val daySummaries = remember {
        mapOf(
            1 to "श्रीमद्भागवत महापुराणको प्रथम दिन मङ्गलमय मंगलाचरण, भागवत महात्म्य र यसको दिव्य महत्त्वको महिमा गाउँदै प्रारम्भ हुन्छ। यस दिन व्यास देवको अशान्ति, नारदजीसँगको परम सम्वाद्, शुकदेव महाराजको प्रादुर्भाव र राजा परीक्षितको गङ्गा तटमा आगमनको दिव्य प्रसंगहरू कथाका मुख्य आकर्षण हुन्।",
            2 to "द्वितीय दिनमा सृष्टिको उत्पत्ति रहस्य र चौबीस अवतारको पावन लीला वर्णन गरिन्छ। यसका साथै कर्दम ऋषि र देवहुतिको तपस्यामय गृहस्थ जीवन र भगवान कपिलको आफ्नो माता देवहुतिलाई दिएको दिव्य साङ्ख्य योग एवं भक्ति मार्गको उपदेश यस दिनको मुख्य सार हो।",
            3 to "तृतीय दिन सती चरित्र र भगवान शिवप्रतिको भक्ति प्रसंगबाट सुरु भई ध्रुव महाराजको बाल्यकालको कठोर तपस्या र भगवान नारायणको साक्षात दर्शनको मर्मस्पर्शी प्रसंगमा जान्छ। जड़ भरतको वैराग्यमय कथा तथा प्रियव्रत र नाभि राजाको दिव्य वंश वर्णन यस दिनको मुख्य अंग हो।",
            4 to "चतुर्थ दिन परमभक्त प्रल्हादको अनन्य भक्ति र भगवान नृसिंहदेवको उग्र एवं कृपालु अवतारको प्रसंग रहन्छ। गजेन्द्र मोक्षको करुण पुकार, देव-असुरद्वारा गरिएको समुद्र मन्थन र भगवान वामन अवतारद्वारा राजा बलिको अहंकार दमन र परम उदारताको कथा प्रस्तुत हुन्छ।",
            5 to "पञ्चम दिन श्रीमद्भागवतको सबैभन्दा उत्सवमय र मङ्गलमय दिन मानिन्छ। यस दिन मर्यादा पुरुषोत्तम भगवान श्रीरामचन्द्रको पावन चरित्र र श्रीकृष्णको जन्मोत्सव, गोकुलमा बाललीला, पूतना उद्धार, दामोदर लीला तथा इन्द्रको मानमर्दन गर्दै गोवर्द्धन लीलाको दिव्य प्रसंग श्रवण गरिन्छ।",
            6 to "षष्ठ दिनमा भगवान श्रीकृष्णको लीलाहरू अझ गहन र रसमय बन्दछन्। गोपिनीहरूसँगको अलौकिक रासलीला, मथुरा गमन, कंशको संहार, द्वारिकापुरीको अलौकिक निर्माण र भगवती रुक्मिणीसँगको भव्य विवाह उत्सव कथाका प्रमुख प्रसंगहरू हुन् जसले अनन्य भक्तिको प्रतिपादन गर्छन्।",
            7 to "सप्तम दिनमा भगवान श्रीकृष्ण र बालसखा सुदामाको निश्छल मित्रता, उद्धव-गोपी ज्ञान-भक्ति सम्वाद् प्रसंग रहन्छ। यसका साथै यदुवंशको उपसंहार र भगवानको स्वधाम गमन, परीक्षितको मोक्ष प्राप्ति, भागवतको उपसंहार र सम्पूर्ण सङ्कल्प सिद्धिको महाआरती उत्सव आयोजना हुन्छ।"
        )
    }

    val currentDaySummary = daySummaries[activeDayId] ?: "श्रीमद्भागवत सप्ताह ज्ञानयज्ञको रसमय कथा प्रसंग।"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_katha_component"),
        colors = CardDefaults.cardColors(containerColor = WarmCream.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Gold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
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
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Schedule",
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "दैनिक कथा तालिका (Daily Katha Schedule)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "७ दिने श्रीमद्भागवत सप्ताहको विस्तृत कार्यतालिका",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Stepper / Horizontal Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("katha_days_stepper_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(saptahaDays) { day ->
                    val isSelected = day.id == activeDayId
                    val isDayFullyRead = day.topics.all { topic -> completedSessions.any { it.topicId == topic.id } }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> Saffron
                                    isDayFullyRead -> Saffron.copy(alpha = 0.12f)
                                    else -> WarmCream
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Saffron else Gold.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeDayId = day.id }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("katha_day_button_${day.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "दिन ${day.id}",
                                color = if (isSelected) HolyIvory else DeepText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (isDayFullyRead) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = if (isSelected) HolyIvory else SaffronLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fetching & Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minHeight(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isFetching) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.testTag("daily_katha_fetching_indicator")
                    ) {
                        CircularProgressIndicator(
                            color = Saffron,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "भागवत सप्ताह तालिका लोड हुँदैछ...",
                            fontSize = 12.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Day Content
                    val currentDayObj = saptahaDays.find { it.id == activeDayId } ?: saptahaDays.first()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    ) {
                        // Day Overview and Summary Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Saffron.copy(alpha = 0.08f),
                                            Gold.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Summary",
                                        tint = Saffron,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${currentDayObj.name} - संक्षेप सार",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentDaySummary,
                                    fontSize = 13.sp,
                                    color = DeepText,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Structured Schedule (Timeline Layout)
                        Text(
                            text = "कथा शृङ्खला तथा समय तालिका",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // If no topics
                        if (currentDayObj.topics.isEmpty()) {
                            Text(
                                text = "यस दिनको लागि कुनै कथा उपलब्ध छैन।",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = MutedText,
                                fontSize = 13.sp
                            )
                        } else {
                            currentDayObj.topics.forEachIndexed { index, topic ->
                                val isTopicCompleted = completedSessions.any { it.topicId == topic.id }
                                val isLast = index == currentDayObj.topics.size - 1

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectTopic(topic.id)
                                            onNavigateToTopic(topic.id)
                                        }
                                        .testTag("daily_katha_schedule_item_${topic.id}")
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Custom Timeline Bullet & Line
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(32.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isTopicCompleted) SaffronLight else Gold.copy(alpha = 0.2f)
                                                )
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isTopicCompleted) SaffronLight else Saffron,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isTopicCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Done",
                                                    tint = HolyIvory,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Saffron)
                                                )
                                            }
                                        }

                                        // Connective line (if not last item)
                                        if (!isLast) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(1.5.dp)
                                                    .height(44.dp)
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(Saffron, Gold.copy(alpha = 0.3f))
                                                        )
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Topic details Card
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isTopicCompleted) Saffron.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.7f)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isTopicCompleted) SaffronLight.copy(alpha = 0.4f) else Gold.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = topic.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepText
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = topic.description,
                                                    fontSize = 12.sp,
                                                    color = MutedText,
                                                    maxLines = 2
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "श्रवण गर्नुहोस्",
                                                tint = Saffron,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension to allow minimum height on Box
fun Modifier.minHeight(min: androidx.compose.ui.unit.Dp): Modifier = this.defaultMinSize(minHeight = min)
