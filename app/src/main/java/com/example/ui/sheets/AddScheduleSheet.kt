package com.example.ui.sheets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Asset
import com.example.data.model.DateUtils
import com.example.data.model.MaintenanceSchedule
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    sheetState: SheetState,
    scheduleToEdit: MaintenanceSchedule?,
    targetAssetId: String?,
    assets: List<Asset>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        assetId: String,
        taskName: String,
        frequencyType: String,
        frequencyValue: Int,
        conditionValue: String?,
        nextDueDate: Long,
        reminderPeriodDays: Int,
        estimatedCost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        notes: String?
    ) -> Unit
) {
    var selectedAssetId by remember {
        mutableStateOf(targetAssetId ?: scheduleToEdit?.assetId ?: assets.firstOrNull()?.id ?: "")
    }
    var taskName by remember { mutableStateOf(scheduleToEdit?.taskName ?: "") }
    var frequencyType by remember { mutableStateOf(scheduleToEdit?.frequencyType ?: "MONTHS") }
    var frequencyValueText by remember { mutableStateOf(scheduleToEdit?.frequencyValue?.toString() ?: "6") }
    var conditionValue by remember { mutableStateOf(scheduleToEdit?.conditionValue ?: "") }
    var reminderDays by remember { mutableIntStateOf(scheduleToEdit?.reminderPeriodDays ?: 3) }
    var estimatedCostText by remember { mutableStateOf(scheduleToEdit?.estimatedCost?.toString() ?: "") }
    var serviceProvider by remember { mutableStateOf(scheduleToEdit?.defaultServiceProvider ?: "") }
    var servicePhone by remember { mutableStateOf(scheduleToEdit?.defaultServicePhone ?: "") }
    var notes by remember { mutableStateOf(scheduleToEdit?.notes ?: "") }

    // Next due offset selector: 0 days (today), 7 days, 30 days, 90 days, 180 days, 365 days
    var dueDaysOffset by remember {
        val defaultOffset = if (scheduleToEdit != null) {
            val diff = (scheduleToEdit.nextDueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
            diff.coerceAtLeast(0).toInt()
        } else 30
        mutableIntStateOf(defaultOffset)
    }

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
                Text(
                    text = if (scheduleToEdit == null) "New Maintenance Rule" else "Edit Maintenance Rule",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Target Asset Selector if multiple
            if (targetAssetId == null && scheduleToEdit == null && assets.size > 1) {
                Column {
                    Text(
                        text = "Apply Rule to Asset",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assets.forEach { a ->
                            FilterChip(
                                selected = selectedAssetId == a.id,
                                onClick = { selectedAssetId = a.id },
                                label = { Text(a.name) }
                            )
                        }
                    }
                }
            }

            // Task Name
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task Name * (e.g. Filter Cleaning, Oil Service)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("schedule_task_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Recurrence Frequency
            Text(
                text = "Recurrence Interval",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Frequency Type chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "DAYS" to "Days",
                    "WEEKS" to "Weeks",
                    "MONTHS" to "Months",
                    "YEARS" to "Years",
                    "ONE_TIME" to "One-Time"
                ).forEach { (typeKey, label) ->
                    FilterChip(
                        selected = frequencyType == typeKey,
                        onClick = { frequencyType = typeKey },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (frequencyType != "ONE_TIME") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = frequencyValueText,
                        onValueChange = { frequencyValueText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Every (Value)") },
                        placeholder = { Text("e.g. 1, 3, 6") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = conditionValue,
                        onValueChange = { conditionValue = it },
                        label = { Text("Condition (Optional)") },
                        placeholder = { Text("e.g. or 5,000 km") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Quick Due Date Selection
            Text(
                text = "First / Next Due Date",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Due Today",
                    3 to "In 3 Days",
                    7 to "In 1 Week",
                    14 to "In 2 Weeks",
                    30 to "In 1 Month",
                    90 to "In 3 Months",
                    180 to "In 6 Months"
                ).forEach { (days, label) ->
                    FilterChip(
                        selected = dueDaysOffset == days,
                        onClick = { dueDaysOffset = days },
                        label = { Text(label) }
                    )
                }
            }

            val computedNextDueDate = System.currentTimeMillis() + (dueDaysOffset.toLong() * 24 * 60 * 60 * 1000)
            Text(
                text = "Scheduled for: ${DateUtils.formatDate(computedNextDueDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Estimated Cost & Service Provider
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = estimatedCostText,
                    onValueChange = { estimatedCostText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Est. Cost ($currencySymbol)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = serviceProvider,
                    onValueChange = { serviceProvider = it },
                    label = { Text("Preferred Provider") },
                    placeholder = { Text("e.g. CoolCare") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = servicePhone,
                onValueChange = { servicePhone = it },
                label = { Text("Provider Phone / WhatsApp") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Service Instructions & Checklist") },
                placeholder = { Text("e.g. Check gas pressure, clean drip tray...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    if (taskName.isNotBlank() && selectedAssetId.isNotBlank()) {
                        val freqVal = frequencyValueText.toIntOrNull() ?: 1
                        onSave(
                            selectedAssetId,
                            taskName,
                            frequencyType,
                            freqVal,
                            conditionValue.takeIf { it.isNotBlank() },
                            computedNextDueDate,
                            reminderDays,
                            estimatedCostText.toDoubleOrNull(),
                            serviceProvider.takeIf { it.isNotBlank() },
                            servicePhone.takeIf { it.isNotBlank() },
                            notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = taskName.isNotBlank() && selectedAssetId.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_schedule_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (scheduleToEdit == null) "Add Maintenance Rule" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
