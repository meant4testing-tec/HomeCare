package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateUtils
import com.example.data.model.LogWithAsset
import com.example.data.model.ScheduleWithAsset
import com.example.ui.components.MaintenanceCard
import com.example.ui.theme.CompletedBgLight
import com.example.ui.theme.CompletedGreenLight
import com.example.ui.theme.DueTodayAmberLight
import com.example.ui.theme.DueTodayBgLight
import com.example.ui.theme.OverdueBgLight
import com.example.ui.theme.OverdueRedLight
import com.example.ui.viewmodel.DashboardState
import com.example.ui.viewmodel.ExpensesSummary

@Composable
fun DashboardScreen(
    dashboardState: DashboardState,
    expensesSummary: ExpensesSummary,
    totalAssetsCount: Int,
    currencySymbol: String,
    onTaskClick: (ScheduleWithAsset) -> Unit,
    onCompleteTask: (ScheduleWithAsset) -> Unit,
    onToggleReminder: (ScheduleWithAsset) -> Unit,
    onViewAsset: (String) -> Unit,
    onAddNewTask: () -> Unit,
    onViewAllAssets: () -> Unit
) {
    val totalPending = dashboardState.overdue.size + dashboardState.dueToday.size + dashboardState.dueSoon.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Household Status Header Cards
        item {
            HouseholdHealthSummary(
                overdueCount = dashboardState.overdue.size,
                dueTodayCount = dashboardState.dueToday.size,
                dueSoonCount = dashboardState.dueSoon.size,
                totalAssets = totalAssetsCount,
                completedThisYear = expensesSummary.completedCount,
                onViewAssetsClick = onViewAllAssets
            )
        }

        // Expiring Warranties Banner if any
        if (dashboardState.expiringWarranties.isNotEmpty()) {
            item {
                ExpiringWarrantiesBanner(
                    warranties = dashboardState.expiringWarranties,
                    onAssetClick = onViewAsset
                )
            }
        }

        // 1. Overdue Section
        if (dashboardState.overdue.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Overdue Attention",
                    count = dashboardState.overdue.size,
                    color = OverdueRedLight,
                    icon = Icons.Default.ErrorOutline
                )
            }
            items(dashboardState.overdue, key = { "overdue_${it.schedule.id}" }) { item ->
                MaintenanceCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onCardClick = { onTaskClick(item) },
                    onCompleteClick = { onCompleteTask(item) },
                    onToggleReminder = { onToggleReminder(item) }
                )
            }
        }

        // 2. Due Today Section
        if (dashboardState.dueToday.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Due Today",
                    count = dashboardState.dueToday.size,
                    color = DueTodayAmberLight,
                    icon = Icons.Default.WarningAmber
                )
            }
            items(dashboardState.dueToday, key = { "today_${it.schedule.id}" }) { item ->
                MaintenanceCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onCardClick = { onTaskClick(item) },
                    onCompleteClick = { onCompleteTask(item) },
                    onToggleReminder = { onToggleReminder(item) }
                )
            }
        }

        // 3. Due Soon Section (Next 14 Days)
        if (dashboardState.dueSoon.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Due Soon (Next 14 Days)",
                    count = dashboardState.dueSoon.size,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Checklist
                )
            }
            items(dashboardState.dueSoon, key = { "soon_${it.schedule.id}" }) { item ->
                MaintenanceCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onCardClick = { onTaskClick(item) },
                    onCompleteClick = { onCompleteTask(item) },
                    onToggleReminder = { onToggleReminder(item) }
                )
            }
        }

        // Empty state when nothing is due or upcoming
        if (totalPending == 0 && dashboardState.upcoming.isEmpty()) {
            item {
                EmptyAllCaughtUpCard(onAddNewTask = onAddNewTask)
            }
        }

        // 4. Upcoming Maintenance Section
        if (dashboardState.upcoming.isNotEmpty()) {
            item {
                var expandedUpcoming by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedUpcoming = !expandedUpcoming }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionHeader(
                            title = "Upcoming Maintenance",
                            count = dashboardState.upcoming.size,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            icon = Icons.Default.Construction
                        )
                        Text(
                            text = if (expandedUpcoming) "Hide" else "Show (${dashboardState.upcoming.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = expandedUpcoming || totalPending == 0) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            dashboardState.upcoming.forEach { item ->
                                MaintenanceCard(
                                    item = item,
                                    currencySymbol = currencySymbol,
                                    onCardClick = { onTaskClick(item) },
                                    onCompleteClick = { onCompleteTask(item) },
                                    onToggleReminder = { onToggleReminder(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recently Completed Logs Section
        if (dashboardState.recentlyCompleted.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Completed",
                    count = dashboardState.recentlyCompleted.size,
                    color = CompletedGreenLight,
                    icon = Icons.Default.CheckCircleOutline
                )
            }
            items(dashboardState.recentlyCompleted, key = { "recent_${it.log.id}" }) { logItem ->
                RecentlyCompletedRow(
                    item = logItem,
                    currencySymbol = currencySymbol,
                    onClick = { onViewAsset(logItem.asset.id) }
                )
            }
        }
    }
}

@Composable
fun HouseholdHealthSummary(
    overdueCount: Int,
    dueTodayCount: Int,
    dueSoonCount: Int,
    totalAssets: Int,
    completedThisYear: Int,
    onViewAssetsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Household Care Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalAssets items tracked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    label = "Overdue",
                    count = overdueCount,
                    color = if (overdueCount > 0) OverdueRedLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    bgColor = if (overdueCount > 0) OverdueBgLight else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Today",
                    count = dueTodayCount,
                    color = if (dueTodayCount > 0) DueTodayAmberLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    bgColor = if (dueTodayCount > 0) DueTodayBgLight else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Due Soon",
                    count = dueSoonCount,
                    color = MaterialTheme.colorScheme.primary,
                    bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Done",
                    count = completedThisYear,
                    color = CompletedGreenLight,
                    bgColor = CompletedBgLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun RecentlyCompletedRow(
    item: LogWithAsset,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CompletedBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = CompletedGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "${item.asset.name} — ${item.log.taskName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Completed on ${DateUtils.formatDate(item.log.completedDate)}${if (item.log.cost != null) " • $currencySymbol${item.log.cost.toInt()}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ExpiringWarrantiesBanner(
    warranties: List<com.example.data.model.Asset>,
    onAssetClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DueTodayBgLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = DueTodayAmberLight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Warranty Notice",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DueTodayAmberLight
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            warranties.take(2).forEach { asset ->
                val info = DateUtils.getWarrantyInfo(asset.warrantyExpiryDate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssetClick(asset.id) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = info.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = DueTodayAmberLight
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAllCaughtUpCard(onAddNewTask: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CompletedBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = CompletedGreenLight,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Caught Up!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CompletedGreenLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No maintenance tasks are overdue or due today. Your household is in great shape.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
