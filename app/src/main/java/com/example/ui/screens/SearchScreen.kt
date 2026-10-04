package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.ui.components.DesoukPropertyCard
import com.example.ui.components.DesoukSearchBar
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.FilterState

@Composable
fun SearchScreen(
    properties: List<PropertyEntity>,
    filterState: FilterState,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("الكل", "شقق للبيع والايجار", "منازل للبيع", "محلات للبيع والايجار", "وحدات إدارية للبيع والايجار", "أراضي")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Search bar
        DesoukSearchBar(
            query = filterState.query,
            onQueryChange = onQueryChange,
            onSearchClick = onSearchClick,
            onFilterClick = onFilterClick,
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Categories Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = (cat == "الكل" && filterState.category == null) ||
                        (filterState.category == cat) ||
                        (cat != "الكل" && filterState.category != null && (
                            (cat.contains("شقق") && filterState.category!!.contains("شقق")) ||
                            (cat.contains("منازل") && filterState.category!!.contains("منازل")) ||
                            (cat.contains("محلات") && filterState.category!!.contains("محلات")) ||
                            ((cat.contains("إداري") || cat.contains("مكاتب")) && (filterState.category!!.contains("إداري") || filterState.category!!.contains("مكاتب"))) ||
                            ((cat.contains("أرض") || cat.contains("أراضي")) && (filterState.category!!.contains("أرض") || filterState.category!!.contains("أراضي")))
                        ))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DesoukNavyDark else Color.White)
                        .clickable {
                            if (cat == "الكل") {
                                onCategoryClick("")
                            } else {
                                onCategoryClick(cat)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results stats and active filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "نتائج البحث (${properties.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark
            )

            if (filterState.category != null || filterState.type != null || filterState.rooms != null || filterState.maxPrice != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DesoukGold.copy(alpha = 0.15f))
                        .clickable { onResetFilters() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "مسح الفلاتر", fontSize = 11.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (properties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد نتائج مطابقة لبحثك",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "جرب تغيير كلمات البحث أو إزالة بعض خيارات التصفية.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = properties,
                    key = { it.id }
                ) { property ->
                    DesoukPropertyCard(
                        property = property,
                        onDetailsClick = onPropertyClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
