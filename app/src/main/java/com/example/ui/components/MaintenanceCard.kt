package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.DateUtils
import com.example.data.model.ScheduleWithAsset
import com.example.data.model.TaskUrgency
import com.example.ui.theme.DueSoonBgLight
import com.example.ui.theme.DueSoonBlueLight
import com.example.ui.theme.DueSoonBorderLight
import com.example.ui.theme.DueTodayAmberLight
import com.example.ui.theme.DueTodayBgLight
import com.example.ui.theme.DueTodayBorderLight
import com.example.ui.theme.OverdueBgLight
import com.example.ui.theme.OverdueBorderLight
import com.example.ui.theme.OverdueRedLight

@Composable
fun MaintenanceCard(
    item: ScheduleWithAsset,
    currencySymbol: String = "₹",
    onCardClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onToggleReminder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val schedule = item.schedule
    val asset = item.asset
    val urgency = DateUtils.getUrgency(schedule.nextDueDate)
    val relativeDue = DateUtils.formatRelativeDue(schedule.nextDueDate)
    val frequencyText = DateUtils.frequencyDescription(
        schedule.frequencyType,
        schedule.frequencyValue,
        schedule.conditionValue
    )

    // Visual Urgency styling
    val (cardBorder, cardBg, badgeColor, badgeBg) = when (urgency) {
        TaskUrgency.OVERDUE -> Quad(
            BorderStroke(1.5.dp, OverdueBorderLight),
            OverdueBgLight,
            OverdueRedLight,
            Color(0xFFFFE4E6)
        )
        TaskUrgency.DUE_TODAY -> Quad(
            BorderStroke(1.5.dp, DueTodayBorderLight),
            DueTodayBgLight,
            DueTodayAmberLight,
            Color(0xFFFEF3C7)
        )
        TaskUrgency.DUE_SOON -> Quad(
            BorderStroke(1.dp, DueSoonBorderLight),
            DueSoonBgLight,
            DueSoonBlueLight,
            Color(0xFFDBEAFE)
        )
        TaskUrgency.UPCOMING -> Quad(
            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("maintenance_card_${schedule.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (urgency == TaskUrgency.OVERDUE || urgency == TaskUrgency.DUE_TODAY) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Asset name & Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Asset Title & Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!asset.location.isNullOrBlank()) {
                        Text(
                            text = asset.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Due status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = relativeDue,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle: Task name & Recurring Interval
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = schedule.taskName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = frequencyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Optional last completed date
            if (schedule.lastCompletedDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last done: ${DateUtils.formatDate(schedule.lastCompletedDate)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Optional Service provider or estimated cost
            if (!schedule.defaultServiceProvider.isNullOrBlank() || schedule.estimatedCost != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!schedule.defaultServiceProvider.isNullOrBlank()) {
                        Text(
                            text = "Provider: ${schedule.defaultServiceProvider}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (schedule.estimatedCost != null && schedule.estimatedCost > 0) {
                        Text(
                            text = "Est: $currencySymbol${schedule.estimatedCost.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Mark Complete Button + Details chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reminder toggle indicator
                if (onToggleReminder != null) {
                    IconButton(
                        onClick = onToggleReminder,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (schedule.remindersEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle Reminders",
                            tint = if (schedule.remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Primary "Mark Done" button
                Button(
                    onClick = onCompleteClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (urgency == TaskUrgency.OVERDUE) OverdueRedLight else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("mark_done_button_${schedule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Complete",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
