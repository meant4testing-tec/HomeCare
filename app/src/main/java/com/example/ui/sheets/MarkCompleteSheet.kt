package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateUtils
import com.example.data.model.ScheduleWithAsset
import com.example.ui.theme.CompletedBgLight
import com.example.ui.theme.CompletedGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkCompleteSheet(
    sheetState: SheetState,
    task: ScheduleWithAsset,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        completedDate: Long,
        cost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        meterReading: String?,
        notes: String?,
        nextDueDateOverride: Long?
    ) -> Unit
) {
    val schedule = task.schedule
    val asset = task.asset

    var completedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var costText by remember {
        mutableStateOf(schedule.estimatedCost?.toInt()?.toString() ?: "")
    }
    var serviceProvider by remember {
        mutableStateOf(schedule.defaultServiceProvider ?: "")
    }
    var servicePhone by remember {
        mutableStateOf(schedule.defaultServicePhone ?: "")
    }
    var meterReading by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Next due date preview calculation
    val defaultNextDue = DateUtils.calculateNextDueDate(
        completedDateMillis = completedDate,
        frequencyType = schedule.frequencyType,
        frequencyValue = schedule.frequencyValue
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CompletedBgLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CompletedGreenLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Record Completed Service",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${asset.name} — ${schedule.taskName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Next Due Date Advance Notice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Next schedule will automatically advance to ${DateUtils.formatDate(defaultNextDue)} (${DateUtils.frequencyDescription(schedule.frequencyType, schedule.frequencyValue, schedule.conditionValue)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Optional Expenses Incurred
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Actual Cost ($currencySymbol)") },
                    placeholder = { Text("e.g. 500") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_cost_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = meterReading,
                    onValueChange = { meterReading = it },
                    label = { Text("Meter / Reading") },
                    placeholder = { Text("e.g. 15,200 km") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_reading_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Service Provider Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = serviceProvider,
                    onValueChange = { serviceProvider = it },
                    label = { Text("Serviced By") },
                    placeholder = { Text("e.g. CoolCare Technician") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_provider_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = servicePhone,
                    onValueChange = { servicePhone = it },
                    label = { Text("Technician Phone") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Notes / What was done
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Service Details & Parts Replaced") },
                placeholder = { Text("e.g. Cleaned coils, refilled 200g R32 gas, replaced mesh filter...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_notes_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Confirm Button
            Button(
                onClick = {
                    onConfirm(
                        completedDate,
                        costText.toDoubleOrNull(),
                        serviceProvider.takeIf { it.isNotBlank() },
                        servicePhone.takeIf { it.isNotBlank() },
                        meterReading.takeIf { it.isNotBlank() },
                        notes.takeIf { it.isNotBlank() },
                        null // use computed next due
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_complete_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CompletedGreenLight)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Service Record & Advance Due Date",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
