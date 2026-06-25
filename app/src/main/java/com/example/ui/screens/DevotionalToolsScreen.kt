package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.viewmodel.AudioSegment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionalToolsScreen(viewModel: SaptahaViewModel) {
    val japState by viewModel.japCounter.collectAsState()
    val isShankhaActive by viewModel.shankhaAnimationPlaying.collectAsState()
    val isBellActive by viewModel.bellAnimationPlaying.collectAsState()

    val context = LocalContext.current

    // Shankha pulsating effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val shankhaScale by infiniteTransition.animateFloat(
        initialValue = if (isShankhaActive) 1.0f else 1.0f,
        targetValue = if (isShankhaActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shankhaScale"
    )

    var currentTab by remember { mutableStateOf(0) } // 0: Jap Counter, 1: Virtual Aarti, 2: Sacred Sounds, 3: Katha Audio Player

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = WarmCream,
            contentColor = Saffron,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                    color = Saffron
                )
            }
        ) {
            Tab(
                selected = currentTab == 0,
                onClick = { currentTab = 0 },
                text = { Text("जप काउन्टर", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_jap")
            )
            Tab(
                selected = currentTab == 1,
                onClick = { currentTab = 1 },
                text = { Text("आरती (Aarti)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_aarti")
            )
            Tab(
                selected = currentTab == 2,
                onClick = { currentTab = 2 },
                text = { Text("शङ्ख ध्वनि", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_sounds")
            )
            Tab(
                selected = currentTab == 3,
                onClick = { currentTab = 3 },
                text = { Text("कथा अडियो", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_audio_player")
            )
        }

        // Tab Content
        when (currentTab) {
            0 -> {
                // JAP COUNTER TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "मन्त्र साधना",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Mantra selector card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarmCream),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "सक्रिय मन्त्र (Active Mantra):",
                                fontSize = 12.sp,
                                color = MutedText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = japState.mantraText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SaffronDark,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Quick select common mantras row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val commonMantras = listOf(
                                    "ॐ नमो भगवते वासुदेवाय",
                                    "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे",
                                    "ॐ नमः शिवाय"
                                )
                                commonMantras.forEach { mantra ->
                                    val isSelected = japState.mantraText == mantra
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, if (isSelected) Saffron else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Saffron.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setMantra(mantra) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (mantra.length > 8) mantra.substring(0, 8) + "..." else mantra,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Saffron else MutedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Completed Malas indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Loop, contentDescription = "Malas", tint = GoldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "पूरा भएको माला: ${japState.completedMalas} माला (Mala Completed)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepText
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Giant clicker button
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(SaffronLight, SaffronDark)
                                )
                            )
                            .border(6.dp, Gold, CircleShape)
                            .clickable {
                                viewModel.incrementJap()
                                viewModel.playVirtualBell()
                            }
                            .testTag("jap_click_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${japState.currentCount}",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black,
                                color = HolyIvory
                            )
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Gold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "लक्ष्य: ${japState.targetCount}",
                                fontSize = 14.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "काउन्टर बढाउन स्क्रिनको बीचमा रहेको ठूलो गोलाकार बटनमा क्लिक गर्नुहोस्। १०८ पुरा भएपछि स्वतः १ माला थपिनेछ।",
                        fontSize = 13.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reset button
                    Button(
                        onClick = { viewModel.resetJap() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.testTag("reset_jap_button")
                    ) {
                        Text("काउन्टर रिसेट (Reset Counter)", color = HolyIvory)
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            1 -> {
                // VIRTUAL AARTI TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCream),
                            border = BorderStroke(1.dp, Gold)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "आरती संग्रह",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "श्रीमद्भागवत आरती र भगवान श्रीकृष्णको स्तुति।",
                                    fontSize = 14.sp,
                                    color = DeepText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.playAudio()
                                            viewModel.selectAudioSegment(AudioSegment.PRAYER)
                                            Toast.makeText(context, "आरती भजन अडियो सुरु भयो", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = "Play Audio")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("आरती अडियो बजाउनुहोस्")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HolyIvory),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "श्रीमद्भागवत आरती (Aarti Text)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val aartiLines = listOf(
                                    "आरती अतिपावन पुराण की।\nधर्म भक्ति सुविज्ञान खान की॥",
                                    "व्यास बखानी शुकदेव बोली।\nपरम परमहंसन की झोली॥",
                                    "मुनि जन ध्यान धरे सदा जाकी।\nआरती अतिपावन पुराण की॥",
                                    "कलि मल नाशिनी पावन गंगा।\nभव भय तारिणी ज्ञान तरंगा॥",
                                    "परमानन्द स्वरूप हरि की।\nआरती अतिपावन पुराण की॥",
                                    "संसारको दुःख कष्ठ हराउने।\nभक्ति र प्रेमको गंगा बगाउने॥",
                                    "श्रीकृष्णको पाउमा ध्यान लगाउने।\nआरती अतिपावन पुराण की॥"
                                )

                                aartiLines.forEach { line ->
                                    Text(
                                        text = line,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = DeepText,
                                        lineHeight = 26.sp,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // SACRED SOUNDS (BELL / SHANKHA) TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "दिव्य पवित्र ध्वनिहरू",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "पूजा गर्दा बजाइने पवित्र शङ्ख र मन्दिरको घण्टीको ध्वनि सुन्नुहोस्। यसले मनमा सकारात्मक ऊर्जा र मन्दिर जस्तो वातावरण सिर्जना गर्दछ।",
                        fontSize = 14.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Shankha Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .scale(shankhaScale)
                            .clickable {
                                viewModel.playVirtualShankha()
                            }
                            .testTag("shankha_click_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isShankhaActive) Saffron.copy(alpha = 0.2f) else WarmCream
                        ),
                        border = BorderStroke(2.dp, if (isShankhaActive) Saffron else Gold)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Shankha",
                                tint = if (isShankhaActive) SaffronLight else Saffron,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "पवित्र शङ्ख बजाउनुहोस् (Blow Shankha)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isShankhaActive) "🔔 शङ्ख फुकिँदै छ..." else "क्लिक गर्नुहोस्",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Temple Bell Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clickable {
                                viewModel.playVirtualBell()
                            }
                            .testTag("bell_click_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBellActive) GoldLight.copy(alpha = 0.2f) else WarmCream
                        ),
                        border = BorderStroke(2.dp, if (isBellActive) GoldDark else Gold)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Temple Bell",
                                tint = if (isBellActive) SaffronLight else GoldDark,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "मन्दिरको घण्टी बजाउनुहोस् (Ring Temple Bell)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBellActive) "🔔 टन-टन... बज्दै छ..." else "क्लिक गर्नुहोस्",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            3 -> {
                // KATHA NARRATION AUDIO PLAYER TAB
                AudioPlayer(viewModel = viewModel)
            }
        }
    }
}
