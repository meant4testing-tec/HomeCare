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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Asset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(
    sheetState: SheetState,
    assets: List<Asset>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        assetId: String,
        taskName: String,
        completedDate: Long,
        cost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        meterReading: String?,
        notes: String?
    ) -> Unit
) {
    var selectedAssetId by remember { mutableStateOf(assets.firstOrNull()?.id ?: "") }
    var taskName by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var serviceProvider by remember { mutableStateOf("") }
    var servicePhone by remember { mutableStateOf("") }
    var meterReading by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Log Past Service Record",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Asset selector
            Column {
                Text(
                    text = "Select Asset",
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

            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task / Service Name * (e.g. Chemical Wash, Brake Pad Replacement)") },
                modifier = Modifier.fillMaxWidth().testTag("quick_log_task_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Cost ($currencySymbol)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = meterReading,
                    onValueChange = { meterReading = it },
                    label = { Text("Reading / Usage") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = serviceProvider,
                    onValueChange = { serviceProvider = it },
                    label = { Text("Service Provider") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = servicePhone,
                    onValueChange = { servicePhone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Observations") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    if (taskName.isNotBlank() && selectedAssetId.isNotBlank()) {
                        onSave(
                            selectedAssetId,
                            taskName,
                            System.currentTimeMillis(),
                            costText.toDoubleOrNull(),
                            serviceProvider.takeIf { it.isNotBlank() },
                            servicePhone.takeIf { it.isNotBlank() },
                            meterReading.takeIf { it.isNotBlank() },
                            notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = taskName.isNotBlank() && selectedAssetId.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_quick_log_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Service Record",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
