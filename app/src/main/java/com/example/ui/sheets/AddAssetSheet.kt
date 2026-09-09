package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Asset
import com.example.data.model.AssetTemplate
import com.example.data.model.DateUtils
import com.example.data.model.HomeMaintenanceTemplates
import com.example.data.model.ScheduleTemplate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetSheet(
    sheetState: SheetState,
    assetToEdit: Asset?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        brandModel: String?,
        serialNumber: String?,
        purchaseDate: Long?,
        purchasePrice: Double?,
        location: String?,
        warrantyMonths: Int?,
        warrantyExpiry: Long?,
        invoiceNumber: String?,
        warrantyNotes: String?,
        notes: String?,
        suggestedSchedules: List<ScheduleTemplate>
    ) -> Unit
) {
    var name by remember { mutableStateOf(assetToEdit?.name ?: "") }
    var category by remember { mutableStateOf(assetToEdit?.category ?: "General") }
    var brandModel by remember { mutableStateOf(assetToEdit?.brandModel ?: "") }
    var serialNumber by remember { mutableStateOf(assetToEdit?.serialNumber ?: "") }
    var location by remember { mutableStateOf(assetToEdit?.location ?: "") }
    var purchasePriceText by remember { mutableStateOf(assetToEdit?.purchasePrice?.toString() ?: "") }
    var warrantyMonthsText by remember { mutableStateOf(assetToEdit?.warrantyDurationMonths?.toString() ?: "") }
    var invoiceNumber by remember { mutableStateOf(assetToEdit?.invoiceNumber ?: "") }
    var warrantyNotes by remember { mutableStateOf(assetToEdit?.warrantyNotes ?: "") }
    var notes by remember { mutableStateOf(assetToEdit?.notes ?: "") }

    val selectedSuggestedSchedules = remember { mutableStateListOf<ScheduleTemplate>() }

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
                    text = if (assetToEdit == null) "Add Household Asset" else "Edit ${assetToEdit.name}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Quick Starter Templates (only for new assets)
            if (assetToEdit == null) {
                Text(
                    text = "Quick Template Suggestions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeMaintenanceTemplates.templates.forEach { template ->
                        FilterChip(
                            selected = name == template.defaultName,
                            onClick = {
                                name = template.defaultName
                                category = template.category
                                selectedSuggestedSchedules.clear()
                                selectedSuggestedSchedules.addAll(template.suggestedSchedules)
                            },
                            label = { Text(template.defaultName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Asset Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Asset Name * (e.g. Master Bedroom AC)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("asset_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Selector
            Column {
                Text(
                    text = "Category",
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
                    HomeMaintenanceTemplates.categories.filter { it != "All" }.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Location & Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location in Home") },
                    placeholder = { Text("e.g. Kitchen, Terrace") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("asset_location_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = brandModel,
                    onValueChange = { brandModel = it },
                    label = { Text("Brand / Model") },
                    placeholder = { Text("e.g. Daikin 1.5T") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("asset_model_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Serial Number & Purchase Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number / VIN") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Price ($currencySymbol)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Warranty Information Section
            HorizontalDivider()
            Text(
                text = "Warranty Information (Optional)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = warrantyMonthsText,
                    onValueChange = { warrantyMonthsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Warranty (Months)") },
                    placeholder = { Text("e.g. 12 or 24") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice / Bill No.") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = warrantyNotes,
                onValueChange = { warrantyNotes = it },
                label = { Text("Warranty Coverage Notes") },
                placeholder = { Text("e.g. 5-year compressor, 1-year comprehensive") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            // General Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("General Notes") },
                placeholder = { Text("Special maintenance tips, technician contact, etc.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            // Suggested Schedules checkboxes if template selected
            if (selectedSuggestedSchedules.isNotEmpty() && assetToEdit == null) {
                HorizontalDivider()
                Text(
                    text = "Suggested Maintenance Tasks for this Asset",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    selectedSuggestedSchedules.forEach { s ->
                        val freqText = DateUtils.frequencyDescription(s.frequencyType, s.frequencyValue, s.conditionValue)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = true,
                                onCheckedChange = { checked ->
                                    if (!checked) selectedSuggestedSchedules.remove(s)
                                }
                            )
                            Column {
                                Text(
                                    text = s.taskName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = freqText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val months = warrantyMonthsText.toIntOrNull()
                        val expiryDate = if (months != null && months > 0) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MONTH, months)
                            cal.timeInMillis
                        } else null

                        onSave(
                            name,
                            category,
                            brandModel,
                            serialNumber,
                            System.currentTimeMillis(), // purchase date
                            purchasePriceText.toDoubleOrNull(),
                            location,
                            months,
                            expiryDate,
                            invoiceNumber,
                            warrantyNotes,
                            notes,
                            selectedSuggestedSchedules.toList()
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_asset_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (assetToEdit == null) "Create Asset" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
