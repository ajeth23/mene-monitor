package app.mene.monitor.presentation.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mene.monitor.R
import app.mene.monitor.presentation.theme.TextMuted
import app.mene.monitor.presentation.theme.TextSecondary

private val BrandIconCyan = Color(0xFF01F8EF)

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val iconRes: Int? = null,
    val icon: ImageVector? = null,
    val testTag: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", iconRes = R.drawable.ic_menemonitor, testTag = "bottom_nav_home"),
    BottomNavItem(Screen.Cpu, "CPU", icon = Icons.Default.Memory, testTag = "bottom_nav_cpu"),
    BottomNavItem(Screen.Memory, "Memory", icon = Icons.Default.DeveloperBoard, testTag = "bottom_nav_memory"),
    BottomNavItem(Screen.Diagnostics, "Diagnostics", icon = Icons.Default.Build, testTag = "bottom_nav_diagnostics"),
    BottomNavItem(Screen.More, "More", icon = Icons.Default.MoreHoriz, testTag = "bottom_nav_more")
)

@Composable
fun MeneBottomNavBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navBarBg = if (isDark) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainer
    val dividerColor = if (isDark) Color(0xFF182236) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBarBg)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = dividerColor,
            thickness = 0.5.dp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                val selectedColor = if (isDark) BrandIconCyan else Color(0xFF007E86)
                val unselectedColor = if (isDark) TextMuted else TextSecondary
                val contentColor = if (selected) selectedColor else unselectedColor

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val targetScale = when {
                    isPressed -> 0.88f
                    selected -> 1.18f
                    else -> 1.0f
                }

                val iconScale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tab_icon_scale_${item.title}"
                )

                val pillAlpha by animateFloatAsState(
                    targetValue = if (selected) 1.0f else 0.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tab_pill_alpha_${item.title}"
                )

                // Active indicator pill uses exact brand icon color #01F8EF
                val pillBackground = if (isDark) {
                    BrandIconCyan.copy(alpha = 0.20f * pillAlpha)
                } else {
                    BrandIconCyan.copy(alpha = 0.28f * pillAlpha)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onNavigate(item.screen) }
                        .padding(vertical = 4.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(pillBackground)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.iconRes != null) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.title,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )
                        } else if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )
                        }
                    }
                    Text(
                        text = item.title,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
