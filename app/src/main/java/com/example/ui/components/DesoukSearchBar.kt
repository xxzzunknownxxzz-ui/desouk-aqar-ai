package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * تصميم مدمج وأنيق (Compact Premium) لخانة البحث في الصفحة الرئيسية:
 * - موضوعة أسفل الخانات العقارية الأربعة مباشرةً
 * - ارتفاع صغير ومريح: 52dp على شاشة الهاتف
 * - عرض متناسق أصغر قليلاً من عرض الخانات (padding horizontal 22dp)
 * - حواف دائرية ناعمة 20dp مع خلفية بيضاء عاجية وظل ناعم وإطار خفيف
 * - زر بحث دائري صغير باللون الذهبي مع حركة ضغط ناعمة
 * - أيقونة فلاتر متناسقة ومصغرة
 * - نص واضح ومتوازن: "ابحث عن شقة، منزل، محل أو عقار آخر..."
 * - الحفاظ الكامل على كافة وظائف البحث والتصفية
 */
@Composable
fun DesoukSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
    val searchBtnScale by animateFloatAsState(
        targetValue = if (isSearchPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "search_btn_scale"
    )

    val filterInteractionSource = remember { MutableInteractionSource() }
    val isFilterPressed by filterInteractionSource.collectIsPressedAsState()
    val filterBtnScale by animateFloatAsState(
        targetValue = if (isFilterPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "filter_btn_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF0F1E36).copy(alpha = 0.08f),
                spotColor = Color(0xFF0F1E36).copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. زر البحث الدائري الذهبي الصغير (في RTL: أقصى اليمين Start)
                Box(
                    modifier = Modifier
                        .scale(searchBtnScale)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F),
                                    Color(0xFFFFA000)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = searchInteractionSource,
                            indication = androidx.compose.material3.ripple(
                                bounded = true,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        ) { onSearchClick() }
                        .testTag("search_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 2. حقل إدخال النص مع نص إرشادي مصغر ومتوازن رأسياً
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "ابحث عن شقة، منزل، محل أو عقار آخر...",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            textAlign = TextAlign.Start
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. أيقونة الفلاتر المصغرة والمتناسقة (في RTL: أقصى اليسار End)
                Box(
                    modifier = Modifier
                        .scale(filterBtnScale)
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = filterInteractionSource,
                            indication = androidx.compose.material3.ripple(
                                bounded = true,
                                color = DesoukGold.copy(alpha = 0.2f)
                            )
                        ) { onFilterClick() }
                        .testTag("filter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "تصفية متقدمة",
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
