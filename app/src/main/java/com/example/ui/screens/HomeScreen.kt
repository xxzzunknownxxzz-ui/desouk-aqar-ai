package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.ui.components.DesoukAiAssistantCard
import com.example.ui.components.DesoukCategories
import com.example.ui.components.DesoukHeader
import com.example.ui.components.DesoukPropertyCard
import com.example.ui.components.DesoukSearchBar
import com.example.ui.components.DesoukUrgentTickerBar
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.viewmodel.FilterState

@Composable
fun HomeScreen(
    properties: List<PropertyEntity>,
    filterState: FilterState,
    allProperties: List<PropertyEntity> = properties,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDistrictClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onAiAssistantClick: (String?) -> Unit,
    onExploreBannerClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    unreadNotificationsCount: Int = 0,
    onNeedsSpecified: ((String, String, String) -> Unit)? = null,
    isFirestorePreloading: Boolean = false,
    firestorePreloadSource: String = "الكاش السريع (0ms)",
    onRefreshPreload: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 20.dp)
    ) {
        // 1. رأس الصفحة باللون الأزرق الداكن وشعار وشريط المنطقة مع زر المساعد الذكي والإشعارات الديناميكية
        DesoukHeader(
            selectedDistrict = filterState.district,
            onMenuClick = onMenuClick,
            onNotificationsClick = onNotificationsClick,
            onDistrictClick = onDistrictClick,
            onAiAssistantClick = { onAiAssistantClick(null) },
            unreadNotificationsCount = unreadNotificationsCount
        )

        Spacer(modifier = Modifier.height(14.dp))

        // المساعد العقاري الذكي AI
        DesoukAiAssistantCard(
            onOpenAssistant = onAiAssistantClick,
            onNeedsSpecified = onNeedsSpecified
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. شريط "عاجل | شقق وعقارات معروضة حديثًا"
        DesoukUrgentTickerBar(
            properties = properties,
            onPropertyClick = onPropertyClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        // التصنيفات العقارية 3D: شقق – منازل – أراضي – وحدات إدارية
        DesoukCategories(
            selectedCategory = filterState.category,
            onCategoryClick = onCategoryClick,
            allProperties = allProperties,
            onClearCategory = { onCategoryClick("") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 7. خانة البحث المدمجة الأنيقة أسفل الخانات العقارية الأربعة مباشرةً
        DesoukSearchBar(
            query = filterState.query,
            onQueryChange = onQueryChange,
            onSearchClick = onSearchClick,
            onFilterClick = onFilterClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. قسم "أحدث العقارات" مع زر "عرض الكل" وعنوان ديناميكي يعكس التصفية الفورية
        // في RTL: اليمين هو Start (عنوان أحدث العقارات)، واليسار هو End (زر عرض الكل)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // اليمين (Start في RTL): "أحدث العقارات" أو التصنيف المختار مع الشريط الذهبي
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
                    text = if (filterState.category != null && filterState.category!!.isNotBlank()) {
                        "عقارات: ${filterState.category} (${properties.size})"
                    } else {
                        "أحدث العقارات"
                    },
                    color = DesoukNavyDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // اليسار (End في RTL): "عرض الكل ←"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onViewAllClick() }
                    .testTag("view_all_properties_button")
            ) {
                Text(
                    text = "عرض الكل  ←",
                    color = DesoukGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. بطاقات أحدث العقارات مع دعم الـ Skeleton Loading وحالة ضعف الإنترنت
        if (properties.isEmpty() && isFirestorePreloading) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(3) {
                    SkeletonPropertyCard()
                }
            }
        } else if (properties.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لا توجد عقارات مطابقة حاليًا أو الاتصال بالإنترنت ضعيف.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onRefreshPreload?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "إعادة المحاولة 🔄", color = DesoukNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = properties,
                    key = { it.id }
                ) { property ->
                    DesoukPropertyCard(
                        property = property,
                        onDetailsClick = onPropertyClick,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SkeletonPropertyCard(modifier: Modifier = Modifier.width(205.dp)) {
    val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    Card(
        modifier = modifier.height(230.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .background(Color.LightGray.copy(alpha = shimmerAlpha))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = shimmerAlpha))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(0.45f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = shimmerAlpha))
            )
        }
    }
}
