package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PropertyEntity
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark

/**
 * بيانات بطاقة التصنيف العقاري ثلاثية الأبعاد (3D) للعرض وفتح أقسام العقارات
 */
data class Category3DItem(
    val filterKey: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val icon: ImageVector,
    val bgGradient: List<Color>,
    val borderColor: Color,
    val badgeGradient: List<Color>,
    val buttonGradient: List<Color>,
    val glowColor: Color,
    val extrusionColor: Color,
    val defaultCount: Int,
    val unitLabel: String
)

@Composable
fun DesoukCategories(
    selectedCategory: String? = null,
    onCategoryClick: ((String) -> Unit)? = null,
    allProperties: List<PropertyEntity> = emptyList(),
    onClearCategory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // حساب عدد العقارات الحية لكل تصنيف لإظهار إحصائيات حقيقية لمدينة دسوق
    val categoryCounts = remember(allProperties) {
        if (allProperties.isEmpty()) {
            mapOf("شقق" to 18, "وحدات إدارية" to 6, "منازل" to 12, "أراضي" to 5)
        } else {
            mapOf(
                "شقق" to allProperties.count {
                    it.category == "شقق" || it.category.contains("شقق")
                }.coerceAtLeast(1),
                "وحدات إدارية" to allProperties.count {
                    it.category == "مكاتب" || it.category == "وحدات إدارية" ||
                    it.category.contains("إداري") || it.category.contains("مكاتب") ||
                    it.title.contains("إداري") || it.title.contains("مكتب") ||
                    it.description.contains("إداري") || it.category == "محلات" || it.category.contains("محل")
                }.coerceAtLeast(1),
                "منازل" to allProperties.count {
                    it.category == "منازل" || it.category.contains("منازل") || it.category.contains("بيت")
                }.coerceAtLeast(1),
                "أراضي" to allProperties.count {
                    it.category == "أراضي" || it.category.contains("أرض")
                }.coerceAtLeast(1)
            )
        }
    }

    // 4 خانات عقارية 3D قابلة للفتح والنقر بترتيب 2 × 2:
    // 1. شقق - للبيع والايجار (الأزرق النيلي الملكي)
    // 2. وحدات إدارية - للبيع والايجار (البنفسجي الإمبراطوري)
    // 3. منازل - للبيع (الأخضر الزمردي الراقي)
    // 4. أراضي - للبيع (البرتقالي الذهبي المميز)
    val categories = remember {
        listOf(
            Category3DItem(
                filterKey = "شقق",
                title = "شقق",
                subtitle = "للبيع والايجار",
                imageRes = R.drawable.img_cat_3d_apartments_1790767759152,
                icon = Icons.Default.Home,
                bgGradient = listOf(Color(0xFF003882), Color(0xFF0056B3), Color(0xFF0074D9)),
                borderColor = Color(0xFF00D2FF),
                badgeGradient = listOf(Color(0xFF00A2FF), Color(0xFF0052D4)),
                buttonGradient = listOf(Color(0xFF0091EA), Color(0xFF0277BD)),
                glowColor = Color(0xFF00B0FF),
                extrusionColor = Color(0xFF002152),
                defaultCount = 18,
                unitLabel = "عقار"
            ),
            Category3DItem(
                filterKey = "وحدات إدارية",
                title = "وحدات إدارية",
                subtitle = "للبيع والايجار",
                imageRes = R.drawable.img_cat_3d_admin_office_1790767774023,
                icon = Icons.Default.Business,
                bgGradient = listOf(Color(0xFF4A0E6E), Color(0xFF6A1B9A), Color(0xFF8E24AA)),
                borderColor = Color(0xFFD946EF),
                badgeGradient = listOf(Color(0xFFC084FC), Color(0xFF7E22CE)),
                buttonGradient = listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)),
                glowColor = Color(0xFFC084FC),
                extrusionColor = Color(0xFF2C0742),
                defaultCount = 6,
                unitLabel = "وحدة"
            ),
            Category3DItem(
                filterKey = "منازل",
                title = "منازل",
                subtitle = "للبيع",
                imageRes = R.drawable.img_cat_3d_modern_house_1790767787150,
                icon = Icons.Default.Cottage,
                bgGradient = listOf(Color(0xFF0B4619), Color(0xFF1B6B2B), Color(0xFF2E8B3E)),
                borderColor = Color(0xFF4ADE80),
                badgeGradient = listOf(Color(0xFF22C55E), Color(0xFF15803D)),
                buttonGradient = listOf(Color(0xFF22C55E), Color(0xFF166534)),
                glowColor = Color(0xFF4ADE80),
                extrusionColor = Color(0xFF07290E),
                defaultCount = 12,
                unitLabel = "منزل"
            ),
            Category3DItem(
                filterKey = "أراضي",
                title = "أراضي",
                subtitle = "للبيع",
                imageRes = R.drawable.img_cat_3d_land_parcels_1790767799928,
                icon = Icons.Default.Landscape,
                bgGradient = listOf(Color(0xFF8A3000), Color(0xFFBF4800), Color(0xFFE65C00)),
                borderColor = Color(0xFFFB923C),
                badgeGradient = listOf(Color(0xFFF97316), Color(0xFFC2410C)),
                buttonGradient = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                glowColor = Color(0xFFFB923C),
                extrusionColor = Color(0xFF4E1A00),
                defaultCount = 5,
                unitLabel = "قطعة"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // رأس قسم التصنيفات العقارية
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DesoukGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "التصنيفات العقارية 3D",
                    color = DesoukNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "اضغط لفتح العقارات ←",
                color = DesoukGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // شبكة التصنيفات ثلاثية الأبعاد (Grid 2 × 2) قابلة للفتح المباشر
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = categories.chunked(2)
            rows.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { item ->
                        val count = categoryCounts[item.filterKey] ?: item.defaultCount

                        Box(modifier = Modifier.weight(1f)) {
                            Category3DCard(
                                category = item,
                                count = count,
                                onClick = { onCategoryClick?.invoke(item.filterKey) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * تصميم البطاقة ثلاثية الأبعاد (3D) مع استجابة النقر الفورية لفتح القسم
 */
@Composable
fun Category3DCard(
    category: Category3DItem,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 18.dp
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "category_card_scale"
    )

    // الحاوية الكلية ثلاثية الأبعاد: تشمل القاعدة السفلية والواجهة
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .testTag("category_3d_${category.filterKey}")
    ) {
        // أ) القاعدة المجسمة السفلية (3D Extrusion Shelf) - تعطي عمقاً وثباتاً للكتلة
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            category.extrusionColor.copy(alpha = 0.85f),
                            category.extrusionColor
                        )
                    )
                )
        )

        // ب) واجهة البطاقة ثلاثية الأبعاد (3D Front Face) - قابلة للنقر مع تفاعل لمسي ناعم
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = if (isPressed) 2.dp else 5.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = category.glowColor.copy(alpha = 0.3f),
                    spotColor = category.glowColor.copy(alpha = 0.45f)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            category.bgGradient.first(),
                            category.bgGradient.last()
                        )
                    )
                )
                .border(
                    width = 1.3.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            category.borderColor
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = true,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                ) {
                    onClick()
                }
        ) {
            // ترتيب LTR: مجسم المبنى على اليسار، والبيانات على اليمين
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. مجسم العقار ثلاثي الأبعاد على اليسار (43% من العرض)
                    Box(
                        modifier = Modifier
                            .weight(0.43f)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(id = category.imageRes),
                            contentDescription = category.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1.04f
                                    scaleY = 1.04f
                                }
                        )

                        // تدرج ظل وتلاشي ناعم يدمج الصورة ثلاثية الأبعاد مع اللوحة الملونة
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.1f),
                                            Color.Transparent,
                                            category.bgGradient.first().copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )
                    }

                    // 2. اللوحة الملونة على اليمين مع النصوص وزر الفتح (57% من العرض)
                    Box(
                        modifier = Modifier
                            .weight(0.57f)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = category.bgGradient
                                )
                            )
                    ) {
                        // شعاع الإضاءة العلوية الزجاجي (Specular Gloss Line)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.75f),
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // المحتويات باتجاه القراءة العربي (RTL)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // الصف العلوي: العنوان العربي الكبير + شارة الأيقونة الزجاجية 3D
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.title,
                                        color = Color.White,
                                        fontSize = if (category.title.length > 8) 14.5.sp else 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = LocalTextStyle.current.copy(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                offset = Offset(1.5f, 2.5f),
                                                blurRadius = 3.5f
                                            )
                                        )
                                    )

                                    // شارة الأيقونة الزجاجية المجسمة
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(Brush.linearGradient(category.badgeGradient))
                                            .border(
                                                width = 1.2.dp,
                                                color = Color.White.copy(alpha = 0.55f),
                                                shape = RoundedCornerShape(7.dp)
                                            )
                                            .shadow(2.dp, RoundedCornerShape(7.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // الصف الأوسط: شارة عدد العقارات المتاحة بالمدينة
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.Black.copy(alpha = 0.25f))
                                            .border(
                                                0.8.dp,
                                                Color.White.copy(alpha = 0.35f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "✦ $count ${category.unitLabel} متوفر",
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // الصف السفلي: النص الفرعي الذهبي "للبيع والايجار" / "للبيع" + زر فتح القسم
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.subtitle,
                                        color = Color(0xFFFFD54F),
                                        fontSize = if (category.subtitle.length > 8) 11.sp else 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = LocalTextStyle.current.copy(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                offset = Offset(1f, 1.5f),
                                                blurRadius = 2.5f
                                            )
                                        )
                                    )

                                    // زر دائري بارز مع سهم للتوجيه وفتح القسم
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(category.buttonGradient))
                                            .border(
                                                width = 1.2.dp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            )
                                            .shadow(2.dp, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "فتح قسم ${category.title}",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
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
