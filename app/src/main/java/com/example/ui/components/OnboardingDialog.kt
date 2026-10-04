package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun OnboardingDialog(
    isOpen: Boolean,
    onComplete: (firstSearchQuery: String?) -> Unit
) {
    if (!isOpen) return

    val steps = listOf(
        OnboardingStep(
            title = "ابحث عن العقار بطريقة أسهل 🔎",
            subtitle = "تصفح أحدث الشقق والمنازل والأراضي في كافة أحياء دسوق مع فلاتر ذكية وفورية.",
            icon = Icons.Default.Search,
            iconColor = Color(0xFF1976D2)
        ),
        OnboardingStep(
            title = "استخدم المساعد العقاري الذكي ✨",
            subtitle = "اطلب باللغة الطبيعية واللهجة المصرية ما تبحث عنه، وسيقوم المساعد بمطابقة أفضل العقارات وتحليل أسعارها.",
            icon = Icons.Default.AutoAwesome,
            iconColor = DesoukGold
        ),
        OnboardingStep(
            title = "احفظ العقارات التي تعجبك ❤️",
            subtitle = "اضغط على أيقونة القلب لحفظ أي عقار في المفضلة وتلقي تنبيهات عند تغير السعر.",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFE91E63)
        ),
        OnboardingStep(
            title = "شارك التطبيق مع أصدقائك 📤",
            subtitle = "شارك كود الإحالة ورابط التطبيق مع عائلتك وأصدقائك لمساعدتهم في العثور على بيت أحلامهم بدسوق.",
            icon = Icons.Default.Share,
            iconColor = Color(0xFF4CAF50)
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var selectedInterest by remember { mutableIntStateOf(-1) }

    val interestOptions = listOf(
        "شقة للبيع",
        "شقة للإيجار",
        "محل تجاري",
        "قطعة أرض",
        "عقار استثماري"
    )

    Dialog(onDismissRequest = { /* force completing or pressing start */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStepIndex < steps.size) {
                    val step = steps[currentStepIndex]

                    // مؤشر الخطوات
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        steps.indices.forEach { idx ->
                            Box(
                                modifier = Modifier
                                    .size(width = if (idx == currentStepIndex) 22.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == currentStepIndex) DesoukGold else Color(0xFFE0E0E0))
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(step.iconColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = step.iconColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            currentStepIndex++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentStepIndex == steps.size - 1) "التالي: حدد اهتمامك 🎯" else "التالي",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // شاشة خطوة البداية والتنشيط: إيه اللي بتدور عليه؟ (#13)
                    Text(
                        text = "أهلاً بك في عقارات دسوق AI 👋",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "إيه اللي بتدور عليه النهاردة؟",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interestOptions.forEachIndexed { idx, option ->
                            val isSelected = selectedInterest == idx
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) DesoukNavyDark else SurfaceBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedInterest = idx }
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) Color.White else DesoukNavyDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 11.dp, horizontal = 14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val chosenQuery = if (selectedInterest in interestOptions.indices) interestOptions[selectedInterest] else null
                            onComplete(chosenQuery)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_finish_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ابدأ الآن 🚀",
                            color = DesoukNavyDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
