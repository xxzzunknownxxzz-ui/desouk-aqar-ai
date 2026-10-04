package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted

enum class NavTab {
    HOME,
    SEARCH,
    ADD,
    CHATS,
    COMMUNITY,
    PROFILE,
    FAVORITES,
    AI_ASSISTANT,
    ADMIN
}

@Composable
fun DesoukBottomNav(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    favoritesCount: Int = 0,
    chatsCount: Int = 2,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .height(86.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom White Bar Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. الرئيسية 3D (Home)
                BottomNav3DItem(
                    label = "الرئيسية",
                    isSelected = currentTab == NavTab.HOME,
                    icon3d = { isSel -> Icon3DHome(isSelected = isSel, size = 26.dp) },
                    onClick = { onTabSelected(NavTab.HOME) },
                    testTag = "nav_home"
                )

                // 2. البحث 3D (Search)
                BottomNav3DItem(
                    label = "البحث",
                    isSelected = currentTab == NavTab.SEARCH,
                    icon3d = { isSel -> Icon3DSearch(isSelected = isSel, size = 26.dp) },
                    onClick = { onTabSelected(NavTab.SEARCH) },
                    testTag = "nav_search"
                )

                // 3. Center Space reserved for FAB
                Spacer(modifier = Modifier.width(60.dp))

                // 4. المفضلة 3D (Favorites)
                BottomNav3DItem(
                    label = "المفضلة",
                    isSelected = currentTab == NavTab.FAVORITES,
                    icon3d = { isSel -> Icon3DFavorites(isSelected = isSel, size = 26.dp) },
                    onClick = { onTabSelected(NavTab.FAVORITES) },
                    testTag = "nav_favorites"
                )

                // 5. حسابي 3D (Profile)
                BottomNav3DItem(
                    label = "حسابي",
                    isSelected = currentTab == NavTab.PROFILE,
                    icon3d = { isSel -> Icon3DProfile(isSelected = isSel, size = 26.dp) },
                    onClick = { onTabSelected(NavTab.PROFILE) },
                    testTag = "nav_profile"
                )
            }
        }

        // Center Elevated Golden FAB 3D: إضافة عقار
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
        ) {
            Icon3DFabAdd(
                onClick = { onTabSelected(NavTab.ADD) },
                size = 54.dp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "إضافة عقار",
                fontSize = 11.sp,
                color = if (currentTab == NavTab.ADD) DesoukGold else TextMuted,
                fontWeight = if (currentTab == NavTab.ADD) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomNav3DItem(
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    icon3d: @Composable (Boolean) -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_item_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = 28.dp,
                    color = DesoukGold.copy(alpha = 0.35f)
                )
            ) { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .testTag(testTag)
    ) {
        icon3d(isSelected)

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) DesoukNavyDark else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        // Yellow indicator line under active tab (matching image for الرئيسية)
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(DesoukGold)
            )
        } else {
            Spacer(modifier = Modifier.height(4.5.dp))
        }
    }
}
