package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DesoukGold

/**
 * تأثير Ripple ناعم وحركة تصغير طفيفة (Scale Animation) احترافية عند الضغط.
 * يعطي إحساساً فخماً بالاستجابة والفيزيائية الحقيقية (Micro-interaction & Haptic feedback visual).
 */
@Composable
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    rippleColor: Color = DesoukGold.copy(alpha = 0.22f),
    bounded: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce_click_scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = bounded, color = rippleColor),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * يطبق فقط حركة التصغير الطفيفة بناءً على InteractionSource موجود، مع تحكم دقيق بمقدار التصغير.
 */
@Composable
fun Modifier.bounceScale(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.965f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "bounce_scale_only"
    )
    return this.scale(scale)
}
