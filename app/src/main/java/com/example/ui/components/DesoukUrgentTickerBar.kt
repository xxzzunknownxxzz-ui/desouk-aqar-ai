package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * شريط عاجل للعقارات الجديدة:
 * - يعرض أحدث الشقق والعقارات المعروضة حديثاً.
 * - عنوان الشريط: "🔴 عاجل | شقق وعقارات معروضة حديثًا".
 * - يتحرك أفقياً بشكل تلقائي وهادئ.
 * - عند الضغط على أي عقار يتم فتح تفاصيله.
 * - يعرض: نوع العقار، المنطقة، السعر، عدد الغرف، وكلمة "جديد".
 * - بعرض الشاشة مع حواف دائرية وتصميم احترافي مدمج.
 */
@Composable
fun DesoukUrgentTickerBar(
    properties: List<PropertyEntity>,
    onPropertyClick: (PropertyEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (properties.isEmpty()) return

    // إعداد قائمة العناصر وتكرارها لمنح حركة شريط مستمرة وسلسة
    val latestProperties = remember(properties) {
        val baseList = properties.take(12)
        if (baseList.size >= 3) baseList + baseList + baseList else baseList
    }

    val listState = rememberLazyListState()

    // تأثير حركة أفقية تلقائية وهادئة
    LaunchedEffect(latestProperties.size) {
        if (latestProperties.isNotEmpty()) {
            while (isActive) {
                delay(60)
                if (!listState.isScrollInProgress) {
                    listState.scrollBy(2.4f)
                    // إذا وصل إلى نهاية القائمة، يعود بسلاسة إلى البداية
                    if (!listState.canScrollForward) {
                        listState.scrollToItem(0)
                    }
                }
            }
        }
    }

    // أنيميشن نبض النقطة الحمراء لكلمة "عاجل"
    val infiniteTransition = rememberInfiniteTransition(label = "urgent_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = BadgeForSaleRed.copy(alpha = 0.15f),
                spotColor = DesoukNavyDark.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        BadgeForSaleRed.copy(alpha = 0.5f),
                        Color(0xFFE2E8F0),
                        DesoukGold.copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("urgent_properties_ticker_bar"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF7F7),
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(vertical = 7.dp)
        ) {
            // شريط العنوان: "🔴 عاجل | شقق وعقارات معروضة حديثًا"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // نقطة النبض الحمراء
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(BadgeForSaleRed)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "عاجل | شقق وعقارات معروضة حديثًا",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BadgeForSaleRed
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BadgeForSaleRed.copy(alpha = 0.12f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "مباشر ⚡",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BadgeForSaleRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // الشريط المتحرك أفقياً للعقارات الحديثة
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = latestProperties,
                    key = { index, prop -> "urgent_${prop.id}_$index" }
                ) { _, property ->
                    UrgentPropertyItemChip(
                        property = property,
                        onClick = { onPropertyClick(property) }
                    )
                }
            }
        }
    }
}

/**
 * عنصر العقار داخل شريط عاجل:
 * يعرض:
 * - شارة "جديد" / "NEW"
 * - نوع العقار
 * - المنطقة
 * - السعر
 * - عدد الغرف إن وجد
 */
@Composable
private fun UrgentPropertyItemChip(
    property: PropertyEntity,
    onClick: () -> Unit
) {
    val propertyTypeShort = remember(property) {
        val cat = if (property.category.isNotBlank()) property.category else "عقار"
        val type = if (property.type.isNotBlank()) property.type else ""
        "$cat $type".trim()
    }

    val districtClean = remember(property.district) {
        property.district.replace("دسوق - ", "").trim()
    }

    Box(
        modifier = Modifier
            .shadow(1.5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .border(0.8.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .bounceClick(
                scaleDown = 0.95f,
                rippleColor = DesoukGold.copy(alpha = 0.25f),
                onClick = onClick
            )
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // شارة "جديد"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(BadgeForSaleRed)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "جديد ✨",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // نوع العقار
            Text(
                text = propertyTypeShort,
                color = DesoukNavyDark,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            // المنطقة
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = districtClean,
                    color = Color(0xFF475569),
                    fontSize = 10.5.sp,
                    maxLines = 1
                )
            }

            // عدد الغرف إن وجد
            if (property.rooms > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${property.rooms} غرف",
                        color = Color(0xFF64748B),
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            // السعر
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${property.price} ${property.priceUnit}",
                    color = Color(0xFF0369A1),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
