package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark

/**
 * تصميم فخم وعصري (Premium) لبطاقة "المجتمع العقاري الذكي" في الصفحة الرئيسية
 * مستوحى ومطابق بدقة للتصميم المعتمد في عقارات دسوق:
 * - بطاقة كبيرة بحواف 22dp وخلفية كحلية متدرجة عميقة
 * - صورة واقعية سينمائية لمجمع سكني حديث في دسوق على اليسار
 * - منحنى هندسي ذهبي أنيق مع دمج احترافي
 * - شارة "✨ مجتمع عقارات دسوق" في الأعلى
 * - العنوان: "المجتمع العقاري الذكي" مع أيقونة أشخاص
 * - السطر الذهبي: "اسأل • شارك • اكتشف"
 * - الوصف: "مجتمع يجمع الباحثين عن العقارات والملاك والمهتمين بسوق العقارات في دسوق."
 * - زر ذهبي جذاب: "دخول المجتمع العقاري ←" مع تفاعل انسيابي
 * - نقاط مؤشرات الكاروسيل الـ 5 بالأسفل
 */
@Composable
fun DesoukPromoBanner(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "banner_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
                .scale(scale)
                .testTag("banner_community_card")
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(
                        bounded = true,
                        color = Color(0xFFFFD54F).copy(alpha = 0.25f)
                    )
                ) { onExploreClick() },
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // نستخدم LayoutDirection.Ltr لضبط اليسار دائماً للصورة واليمين دائماً للمحتوى العربي
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF040E1E), // أقصى اليسار تحت الصورة
                                    Color(0xFF071933),
                                    Color(0xFF0B2144),
                                    Color(0xFF061427)  // أقصى اليمين
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 600f)
                            )
                        )
                ) {
                    // 1. صورة المجمع السكني الواقعي الحديث في الطرف الأيسر
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.50f)
                            .align(Alignment.CenterStart)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.img_cat_3d_apartments_1790767759152)
                                .crossfade(true)
                                .size(600, 450)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "مجمع سكني حديث دسوق",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // تدرج داكن شفاف يدمج الصورة تدريجياً لليمين
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF06152B).copy(alpha = 0.45f),
                                            Color(0xFF071933).copy(alpha = 0.90f),
                                            Color(0xFF071933)
                                        )
                                    )
                                )
                        )
                    }

                    // 2. منحنى هندسي فخم فاصل بلون ذهبي متوهج وشبكات النقاط الضوئية
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // رسم المنحنى الانسيابي الأنيق الفاصل بين الصورة والمحتوى
                        val curvePath = Path().apply {
                            moveTo(w * 0.44f, 0f)
                            cubicTo(
                                w * 0.48f, h * 0.25f,
                                w * 0.40f, h * 0.70f,
                                w * 0.25f, h
                            )
                        }

                        // توهج خلفي أزرق عميق على طول المنحنى
                        drawPath(
                            path = curvePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.5f),
                                    Color(0xFF0284C7).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            style = Stroke(width = 8.dp.toPx())
                        )

                        // الخط الذهبي الرئيسي الأنيق
                        drawPath(
                            path = curvePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFEAB308),
                                    Color(0xFFFBBF24),
                                    Color(0xFFD97706),
                                    Color(0xFF78350F).copy(alpha = 0.4f)
                                )
                            ),
                            style = Stroke(width = 2.2.dp.toPx())
                        )

                        // رسم شبكة نقاط ضوئية هندسية في الأعلى يميناً (بدون ازدحام)
                        val dotColor = Color(0xFF38BDF8).copy(alpha = 0.18f)
                        val startDotX = w * 0.91f
                        val startDotY = h * 0.14f
                        val spacing = 7.dp.toPx()
                        for (row in 0..2) {
                            for (col in 0..3) {
                                drawCircle(
                                    color = dotColor,
                                    radius = 1.2.dp.toPx(),
                                    center = Offset(startDotX + (col * spacing), startDotY + (row * spacing))
                                )
                            }
                        }

                        // رسم شبكة نقاط ثانية خافتة قرب منتصف البطاقة
                        val dotGold = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        val midDotX = w * 0.44f
                        val midDotY = h * 0.60f
                        for (row in 0..2) {
                            for (col in 0..3) {
                                drawCircle(
                                    color = dotGold,
                                    radius = 1.2.dp.toPx(),
                                    center = Offset(midDotX + (col * spacing), midDotY + (row * spacing))
                                )
                            }
                        }
                    }

                    // 3. الجزء الأيمن: المحتوى العربي الكامل (RTL)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.66f)
                                .align(Alignment.CenterEnd)
                                .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            // أ) الشارة العلوية الذهبية: ✨ مجتمع عقارات دسوق
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF091F3E))
                                    .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✦",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "مجتمع عقارات دسوق",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // ب) العنوان الرئيسي مع أيقونة الأشخاص: المجتمع العقاري الذكي
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "المجتمع العقاري الذكي",
                                    color = Color.White,
                                    fontSize = 17.5.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // أيقونة أشخاص ثلاثية مصممة بلون ذهبي وأبيض أنيق
                                CommunityPeopleGroupIcon(size = 20.dp)
                            }

                            // ج) السطر الفرعي الذهبي: اسأل • شارك • اكتشف
                            Text(
                                text = "اسأل • شارك • اكتشف",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Start
                            )

                            // د) الوصف القصير
                            Text(
                                text = "مجتمع يجمع الباحثين عن العقارات والملاك والمهتمين بسوق العقارات في دسوق.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start,
                                lineHeight = 13.5.sp,
                                maxLines = 2
                            )

                            // هـ) الزر الذهبي الرئيسي: دخول المجتمع العقاري ←
                            val buttonInteraction = remember { MutableInteractionSource() }
                            val isBtnPressed by buttonInteraction.collectIsPressedAsState()
                            val btnScale by animateFloatAsState(
                                targetValue = if (isBtnPressed) 0.94f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.7f),
                                label = "btn_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .scale(btnScale)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFD54F), // ذهبي مشرق
                                                Color(0xFFFFB300),
                                                Color(0xFFFFA000)
                                            )
                                        )
                                    )
                                    .clickable(
                                        interactionSource = buttonInteraction,
                                        indication = androidx.compose.material3.ripple(
                                            bounded = true,
                                            color = Color(0xFF071830).copy(alpha = 0.2f)
                                        )
                                    ) { onExploreClick() }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("banner_enter_community_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "دخول المجتمع العقاري",
                                        color = Color(0xFF071830),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "←",
                                        color = Color(0xFF071830),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. نقاط مؤشرات الكاروسيل الـ 5 (مطابقة للصورة بدقة: 1 زرقاء، 2 كبسولة ذهبية، 3 و 4 و 5 زرقاء فاتحة)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نقطة 1
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF93C5FD))
            )
            Spacer(modifier = Modifier.width(5.dp))

            // نقطة 2: الكبسولة الذهبية النشطة
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEAB308))
            )
            Spacer(modifier = Modifier.width(5.dp))

            // نقطة 3
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFDBFE))
            )
            Spacer(modifier = Modifier.width(5.dp))

            // نقطة 4
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFDBFE))
            )
            Spacer(modifier = Modifier.width(5.dp))

            // نقطة 5
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFDBFE))
            )
        }
    }
}

/**
 * أيقونة أنيقة لمجموعة أشخاص (3 أشخاص متدرجين باللون الذهبي والأبيض)
 * تعكس روح المجتمع العقاري الذكي بدقة كما في التصميم العالمي
 */
@Composable
private fun CommunityPeopleGroupIcon(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // شخص بالخلف على اليسار (أبيض ناصع)
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = w * 0.15f,
                center = Offset(w * 0.28f, h * 0.32f)
            )
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.10f, h * 0.52f),
                size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.40f)
            )

            // شخص بالخلف على اليمين (ذهبي ناعم)
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                radius = w * 0.15f,
                center = Offset(w * 0.72f, h * 0.32f)
            )
            drawArc(
                color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.54f, h * 0.52f),
                size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.40f)
            )

            // الشخص الرئيسي في المنتصف (أكبر وبلون ذهبي متألق)
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = w * 0.18f,
                center = Offset(w * 0.50f, h * 0.26f)
            )
            drawArc(
                color = Color(0xFFFFD54F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.28f, h * 0.46f),
                size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.48f)
            )
        }
    }
}
