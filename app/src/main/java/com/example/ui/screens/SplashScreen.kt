package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SplashScreen(onProceed: () -> Unit) {
    // Elegant entrance animations
    val infiniteTransition = rememberInfiniteTransition(label = "mandala")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SaffronDark,
                        Saffron,
                        SpiritualDarkBg
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Golden Mandala (Custom drawn for devotional elegance)
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .drawBehind {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    // Outer golden ring
                    drawCircle(
                        color = Gold,
                        radius = radius - 10,
                        style = Stroke(width = 3f)
                    )

                    // Inner glowing rings
                    drawCircle(
                        color = GoldLight.copy(alpha = 0.3f),
                        radius = radius - 25,
                        style = Stroke(width = 1f)
                    )

                    // Artistic radiating temple petals
                    rotate(rotation, center) {
                        for (i in 0 until 12) {
                            val angle = i * (360f / 12)
                            rotate(angle, center) {
                                drawArc(
                                    color = Gold,
                                    startAngle = -15f,
                                    sweepAngle = 30f,
                                    useCenter = false,
                                    topLeft = Offset(10f, 10f),
                                    size = size.copy(
                                        width = size.width - 20,
                                        height = size.height - 20
                                    ),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }
                }
                .background(SaffronDark.copy(alpha = 0.5f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Sacred Sanskrit AUM symbol / Text
            Text(
                text = "ॐ",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Title in Nepali
        Text(
            text = "श्रीमद्भागवत सप्ताह महापुराण",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = HolyIvory,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "कथा वाचन, श्लोक पाठ, प्रवचन र भक्ति साधन",
            fontSize = 16.sp,
            color = GoldLight,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Elder-friendly proceed button (large size, clear feedback, high contrast)
        Button(
            onClick = onProceed,
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DeepText
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(64.dp)
                .border(2.dp, HolyIvory, RoundedCornerShape(32.dp))
                .testTag("proceed_button"),
            shape = RoundedCornerShape(32.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "प्रवेश गर्नुहोस् (Proceed)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Proceed Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Traditional welcome label
        Text(
            text = "जय श्रीकृष्ण • वसुधैव कुटुम्बकम्",
            fontSize = 14.sp,
            color = HolyIvory.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}
