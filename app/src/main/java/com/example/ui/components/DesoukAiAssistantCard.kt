package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * كرت المساعد العقاري الذكي AI المطابق للصورة المرجعية:
 * - بطاقة مستطيلة بحواف دائرية ناعمة (22dp).
 * - خلفية متدرجة باللون الأزرق الداكن إلى الأزرق السماوي مع توهج خفيف.
 * - حدود وإضاءة خفيفة باللون الأزرق السماوي (Cyan Neon).
 * - روبوت AI أبيض وأزرق داخل دائرة مضيئة في الجانب الأيسر.
 * - عنوان: "المساعد العقاري الذكي" باللون الأبيض و "AI" باللون الذهبي البارز.
 * - نص فرعي: "ساعدك في العثور على العقار المناسب في دسوق".
 * - زر أصفر/ذهبي واضح بيضاوي (Pill) مكتوب عليه: "ابدأ البحث الذكي" مع أيقونة البحث 🔍.
 * - أيقونة منزل مضيئة باللون السماوي على الجانب الأيمن مع تلال وتدرجات مبانٍ.
 * - نجوم وتأثيرات ذكاء اصطناعي مضيئة (✦).
 * - متجاوبة وتناسب جميع شاشات الهواتف.
 */
@Composable
fun DesoukAiAssistantCard(
    onOpenAssistant: (query: String?) -> Unit,
    onNeedsSpecified: ((district: String, propertyType: String, dealType: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // أنيميشن نبض رقيق للنجوم والإضاءة المحيطية
    val infiniteTransition = rememberInfiniteTransition(label = "ai_card_twinkle")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "ai_card_scale"
    )

    val btnInteractionSource = remember { MutableInteractionSource() }
    val isBtnPressed by btnInteractionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isBtnPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "ai_btn_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0xFF00D4FF).copy(alpha = 0.35f),
                spotColor = Color(0xFF0284C7).copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.6.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00D4FF),
                        Color(0xFF38BDF8),
                        Color(0xFF0096C7),
                        Color(0xFF00D4FF)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("ai_assistant_home_card"),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        // نستخدم LTR لترتيب العناصر الأفقية الأساسية لتطابق الصورة المرجعية بدقة:
        // الجانب الأيسر: الروبوت داخل الدائرة المضيئة
        // المنتصف: النصوص وزر ابدأ البحث الذكي
        // الجانب الأيمن: المنزل المضيء والنجوم
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF001738), // أزرق ليلي داكن جداً خلف الروبوت
                                Color(0xFF002752),
                                Color(0xFF003D7A),
                                Color(0xFF005A9C),
                                Color(0xFF0077B6)  // أزرق سماوي غني خلف المنزل المضيء
                            )
                        )
                    )
                    .clickable(
                        interactionSource = cardInteractionSource,
                        indication = ripple(bounded = true, color = Color(0xFF00E5FF).copy(alpha = 0.25f))
                    ) {
                        onOpenAssistant(null)
                    }
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. الجانب الأيسر: روبوت AI أبيض وأزرق داخل دائرة مضيئة مع هالة ذهبية وزرقاء
                    Box(
                        modifier = Modifier
                            .size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // هالة الإضاءة الخارجية خلف الروبوت
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.45f),
                                            Color(0xFF0091EA).copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // حلقة الإطار الدائري المزدوجة (توهج ذهبي وسماوي)
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFFFFD54F), // لمسة ذهبية مطابقة للصورة
                                            Color(0xFF00E5FF), // سماوي مضيء
                                            Color(0xFF0284C7),
                                            Color(0xFFFFD54F)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF001F3F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_ai_robot_avatar_1790765196706),
                                contentDescription = "روبوت المساعد العقاري الذكي",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 2. المنتصف: العنوان والنص التوضيحي والزر الأصفر
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // العنوان: "المساعد العقاري الذكي AI"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "المساعد العقاري الذكي",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "AI",
                                    color = Color(0xFFFFD600), // أصفر ذهبي ساطع مطابق للصورة
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // النص التوضيحي أسفل العنوان
                            Text(
                                text = "ساعدك في العثور على العقار المناسب في دسوق",
                                color = Color(0xFFF1F5F9),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // الزر الأصفر/الذهبي البيضاوي: "ابدأ البحث الذكي 🔍"
                            Surface(
                                onClick = { onOpenAssistant(null) },
                                interactionSource = btnInteractionSource,
                                modifier = Modifier
                                    .scale(btnScale)
                                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(50))
                                    .testTag("start_ai_search_button"),
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFFFE082), // تدرج ذهبي أصفر مبهج
                                                    Color(0xFFFFCA28),
                                                    Color(0xFFFFB300)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFFFF9C4),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "ابدأ البحث الذكي",
                                            color = Color(0xFF0F172A), // كحلي داكن مقروء وممتاز
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "بحث",
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3. الجانب الأيمن: أيقونة منزل مضيئة باللون السماوي مع النجوم والتلال
                    Box(
                        modifier = Modifier
                            .size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // النجوم الذهبية اللامعة ✦
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = "✦",
                                color = Color(0xFFFFD54F),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 2.dp, top = 2.dp)
                                    .scale(starScale)
                            )
                            Text(
                                text = "✦",
                                color = Color(0xFFFFE082),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 14.dp)
                                    .scale(starScale * 0.9f)
                            )
                        }

                        // رسم المنزل المضيء بالنيون السماوي والتلال الخلفية
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .align(Alignment.BottomEnd)
                                .drawBehind {
                                    val w = size.width
                                    val h = size.height

                                    // 1. تلال زرقاء ناعمة في الأسفل
                                    val hillPath = Path().apply {
                                        moveTo(0f, h * 0.85f)
                                        quadraticTo(w * 0.5f, h * 0.72f, w, h * 0.82f)
                                        lineTo(w, h)
                                        lineTo(0f, h)
                                        close()
                                    }
                                    drawPath(
                                        path = hillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF0284C7).copy(alpha = 0.5f),
                                                Color(0xFF0369A1).copy(alpha = 0.8f)
                                            )
                                        )
                                    )

                                    // 2. المنزل المضيء بالنيون السماوي
                                    val houseColor = Color(0xFF67E8F9)
                                    val strokeWidth = 3.dp.toPx()

                                    // خط الأساس (الأرضية)
                                    drawLine(
                                        color = houseColor,
                                        start = Offset(w * 0.15f, h * 0.80f),
                                        end = Offset(w * 0.85f, h * 0.80f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )

                                    // جدران المنزل
                                    drawLine(
                                        color = houseColor,
                                        start = Offset(w * 0.22f, h * 0.80f),
                                        end = Offset(w * 0.22f, h * 0.48f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = houseColor,
                                        start = Offset(w * 0.78f, h * 0.80f),
                                        end = Offset(w * 0.78f, h * 0.48f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )

                                    // سقف المنزل المثلث (Gable Roof)
                                    val roofPath = Path().apply {
                                        moveTo(w * 0.12f, h * 0.52f)
                                        lineTo(w * 0.50f, h * 0.22f)
                                        lineTo(w * 0.88f, h * 0.52f)
                                    }
                                    drawPath(
                                        path = roofPath,
                                        color = houseColor,
                                        style = Stroke(
                                            width = strokeWidth + 0.5f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    // المدخنة على جانب السقف
                                    val chimneyPath = Path().apply {
                                        moveTo(w * 0.28f, h * 0.38f)
                                        lineTo(w * 0.28f, h * 0.26f)
                                        lineTo(w * 0.36f, h * 0.26f)
                                        lineTo(w * 0.36f, h * 0.32f)
                                    }
                                    drawPath(
                                        path = chimneyPath,
                                        color = houseColor,
                                        style = Stroke(
                                            width = strokeWidth - 0.5f,
                                            cap = StrokeCap.Square,
                                            join = StrokeJoin.Miter
                                        )
                                    )

                                    // باب ونافذة المنزل في المنتصف
                                    val doorRect = Path().apply {
                                        moveTo(w * 0.40f, h * 0.80f)
                                        lineTo(w * 0.40f, h * 0.54f)
                                        lineTo(w * 0.60f, h * 0.54f)
                                        lineTo(w * 0.60f, h * 0.80f)
                                    }
                                    drawPath(
                                        path = doorRect,
                                        color = houseColor,
                                        style = Stroke(
                                            width = strokeWidth - 0.5f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    // نافذة صغيرة أعلى الباب داخل المستطيل
                                    drawRect(
                                        color = houseColor,
                                        topLeft = Offset(w * 0.46f, h * 0.59f),
                                        size = Size(w * 0.08f, h * 0.07f)
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}
