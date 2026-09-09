package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateUtils
import com.example.data.model.ScheduleWithAsset
import com.example.ui.components.MaintenanceCard
import com.example.ui.theme.DueTodayAmberLight
import com.example.ui.theme.OverdueRedLight
import java.util.Calendar

@Composable
fun CalendarScreen(
    schedules: List<ScheduleWithAsset>,
    selectedDateMillis: Long,
    currencySymbol: String,
    onSelectDate: (Long) -> Unit,
    onTaskClick: (ScheduleWithAsset) -> Unit,
    onCompleteTask: (ScheduleWithAsset) -> Unit,
    onToggleReminder: (ScheduleWithAsset) -> Unit
) {
    var calendarMonth by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val monthYearText = DateUtils.formatMonthYear(calendarMonth.timeInMillis)

    // Tasks due on selected date
    val selectedDateStart = DateUtils.getStartOfDay(selectedDateMillis)
    val selectedDateEnd = DateUtils.getEndOfDay(selectedDateMillis)
    val tasksOnSelectedDate = schedules.filter {
        it.schedule.nextDueDate in selectedDateStart..selectedDateEnd
    }

    // Tasks due in the current displayed month
    val monthStartCal = (calendarMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val monthEndCal = (calendarMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }
    val tasksInMonth = schedules.filter {
        it.schedule.nextDueDate in monthStartCal.timeInMillis..monthEndCal.timeInMillis
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val prev = (calendarMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                }
                                calendarMonth = prev
                            },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month"
                            )
                        }

                        Text(
                            text = monthYearText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                val next = (calendarMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, 1)
                                }
                                calendarMonth = next
                            },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of week row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month Calendar Grid Days
                    MonthGrid(
                        currentMonth = calendarMonth,
                        selectedDateMillis = selectedDateMillis,
                        schedules = schedules,
                        onDateSelected = onSelectDate
                    )
                }
            }
        }

        // Section: Tasks due on selected date
        item {
            Text(
                text = "Tasks Due on ${DateUtils.formatDate(selectedDateMillis)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (tasksOnSelectedDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No maintenance tasks scheduled for this day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(tasksOnSelectedDate, key = { "day_${it.schedule.id}" }) { item ->
                MaintenanceCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onCardClick = { onTaskClick(item) },
                    onCompleteClick = { onCompleteTask(item) },
                    onToggleReminder = { onToggleReminder(item) }
                )
            }
        }

        // Section: All tasks in this month
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Activities in $monthYearText (${tasksInMonth.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (tasksInMonth.isEmpty()) {
            item {
                Text(
                    text = "No tasks due in this entire month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(tasksInMonth, key = { "month_${it.schedule.id}" }) { item ->
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

@Composable
fun MonthGrid(
    currentMonth: Calendar,
    selectedDateMillis: Long,
    schedules: List<ScheduleWithAsset>,
    onDateSelected: (Long) -> Unit
) {
    val cal = (currentMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val todayStart = DateUtils.getStartOfDay()
    val selectedDayStart = DateUtils.getStartOfDay(selectedDateMillis)

    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (week in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in 0 until 7) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val cellCal = (currentMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val cellMillis = cellCal.timeInMillis
                        val isSelected = cellMillis == selectedDayStart
                        val isToday = cellMillis == todayStart

                        val hasTasks = schedules.any {
                            DateUtils.getStartOfDay(it.schedule.nextDueDate) == cellMillis
                        }

                        val dayBg = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        }

                        val textFg = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(dayBg)
                                .clickable { onDateSelected(cellMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    ),
                                    color = textFg
                                )
                                if (hasTasks) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty cell before or after month
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}
