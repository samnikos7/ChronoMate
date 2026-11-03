package com.example.chronomate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronomate.R
import com.example.chronomate.model.CalibrationEntry
import com.example.chronomate.model.ChronoData
import com.example.chronomate.model.CompensationMode
import com.example.chronomate.model.WeightType
import com.example.chronomate.model.VelocityUnit
import com.example.chronomate.viewmodel.ChronoViewModel

@Composable
fun SettingsScreen(data: ChronoData, viewModel: ChronoViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var weightsExpanded by remember { mutableStateOf(false) }
    var calibrationExpanded by remember { mutableStateOf(false) }
    var showFixedChronoWarning by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            Triple("English", "en", "English"),
            Triple("Deutsch", "de", "Deutsch"),
            Triple("Polski", "pl", "Polski"),
            Triple("Italiano", "it", "Italiano"),
            Triple("Español", "es", "Español"),
            Triple("Nederlands", "nl", "Nederlands"),
            Triple("日本語", "ja", "日本語"),
            Triple("繁體中文", "zh-rTW", "繁體中文"),
            Triple("简体中文", "zh-rCN", "简体中文"),
            Triple("Українська", "uk", "Українська"),
            Triple("Norsk", "no", "Norsk"),
            Triple("Svenska", "sv", "Svenska"),
            Triple("Dansk", "da", "Dansk")
        ).sortedBy { it.first }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        Text(stringResource(R.string.general_settings), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Dark Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.dark_mode_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = data.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keep Screen On Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.keep_screen_on), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.keep_screen_on_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = data.keepScreenOn,
                onCheckedChange = { viewModel.toggleKeepScreenOn(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ListItem(
            headlineContent = { Text(stringResource(R.string.language)) },
            supportingContent = {
                Text(languages.find { it.second == data.language }?.first ?: "English")
            },
            leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
            modifier = Modifier.clickable { showLanguageDialog = true }
        )

        var velocityUnitExpanded by remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.velocity_unit_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.velocity_unit_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { velocityUnitExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(data.velocityUnit.symbol)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = velocityUnitExpanded,
                onDismissRequest = { velocityUnitExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.velocity_unit_mps)) },
                    onClick = {
                        viewModel.setVelocityUnit(
                            context,
                            VelocityUnit.METERS_PER_SECOND
                        )
                        velocityUnitExpanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.velocity_unit_fps)) },
                    onClick = {
                        viewModel.setVelocityUnit(
                            context,
                            VelocityUnit.FEET_PER_SECOND
                        )
                        velocityUnitExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chrono Selection
        Text(stringResource(R.string.chrono_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.select_chrono), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))

        var chronoExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { chronoExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(when(data.chronoType) {
                    com.example.chronomate.model.ChronoType.HT_X3000 -> stringResource(R.string.ht_x3000)
                    com.example.chronomate.model.ChronoType.HT_50 -> stringResource(R.string.ht_50)
                })
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = chronoExpanded, onDismissRequest = { chronoExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ht_x3000)) },
                    onClick = {
                        viewModel.setChronoType(context, com.example.chronomate.model.ChronoType.HT_X3000)
                        chronoExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ht_50)) },
                    onClick = {
                        viewModel.setChronoType(context, com.example.chronomate.model.ChronoType.HT_50)
                        chronoExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.fixed_chrono_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(
    R.string.fixed_chrono_description
),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.fixed_chrono_toggle),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (data.fixedChronoProfileEnabled) {
    stringResource(R.string.fixed_chrono_enabled)
} else {
    stringResource(R.string.fixed_chrono_disabled)
},
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.fixedChronoProfileEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Switch(
                checked = data.fixedChronoProfileEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showFixedChronoWarning = true
                    } else {
                        viewModel.setFixedChronoProfileEnabled(context, false)
                    }
                }
            )
        }

        if (data.fixedChronoProfileEnabled) {
            var chronoOutputUnitExpanded by remember {
                mutableStateOf(false)
            }

            var fixedWeightText by remember(data.fixedChronoWeightGrams) {
                val value = "%.2f".format(data.fixedChronoWeightGrams)
                mutableStateOf(
                    TextFieldValue(
                        text = value,
                        selection = TextRange(value.length)
                    )
                )
            }

            var fixedDiameterText by remember(data.fixedChronoDiameterMm) {
                val value = "%.2f".format(data.fixedChronoDiameterMm)
                mutableStateOf(
                    TextFieldValue(
                        text = value,
                        selection = TextRange(value.length)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.fixed_chrono_output_unit),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = stringResource(
    R.string.fixed_chrono_output_unit_description
),

                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { chronoOutputUnitExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(data.chronoOutputUnit.symbol)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = chronoOutputUnitExpanded,
                            onDismissRequest = { chronoOutputUnitExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.velocity_unit_mps)) },
                                onClick = {
                                    viewModel.setChronoOutputUnit(
                                        context,
                                        VelocityUnit.METERS_PER_SECOND
                                    )
                                    chronoOutputUnitExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.velocity_unit_fps)) },
                                onClick = {
                                    viewModel.setChronoOutputUnit(
                                        context,
                                        VelocityUnit.FEET_PER_SECOND
                                    )
                                    chronoOutputUnitExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fixedWeightText,
                            onValueChange = { newValue ->
                                fixedWeightText = newValue
                                newValue.text
                                    .replace(',', '.')
                                    .toFloatOrNull()
                                    ?.let { weight ->
                                        viewModel.setFixedChronoWeight(context, weight)
                                    }
                            },
                            label = {
                                Text(
                                    stringResource(R.string.fixed_chrono_locked_weight)
                                )
                            },
                            suffix = { Text("g") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = fixedDiameterText,
                            onValueChange = { newValue ->
                                fixedDiameterText = newValue
                                newValue.text
                                    .replace(',', '.')
                                    .toFloatOrNull()
                                    ?.let { diameter ->
                                        viewModel.setFixedChronoDiameter(context, diameter)
                                    }
                            },
                            label = {
    Text(
        stringResource(
            R.string.fixed_chrono_locked_diameter
        )
    )
},
                            suffix = { Text("mm") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(
    R.string.fixed_chrono_profile_summary,
    data.fixedChronoWeightGrams,
    data.fixedChronoDiameterMm,
    data.chronoOutputUnit.symbol
),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Calibration Section
        Text(stringResource(R.string.calibration_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.calibration_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var expandedMode by remember { mutableStateOf(false) }
                Text(stringResource(R.string.comp_mode_label), style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedButton(
                        onClick = { expandedMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(when(data.compensationMode) {
                            CompensationMode.NONE -> stringResource(R.string.comp_mode_none)
                            CompensationMode.MEDIAN -> stringResource(R.string.comp_mode_median)
                            CompensationMode.TRUST_EXTERNAL -> stringResource(R.string.comp_mode_external)
                        })
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedMode, onDismissRequest = { expandedMode = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.comp_mode_none)) },
                            onClick = { viewModel.setCompensationMode(context, CompensationMode.NONE); expandedMode = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.comp_mode_median)) },
                            onClick = { viewModel.setCompensationMode(context, CompensationMode.MEDIAN); expandedMode = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.comp_mode_external)) },
                            onClick = { viewModel.setCompensationMode(context, CompensationMode.TRUST_EXTERNAL); expandedMode = false }
                        )
                    }
                }

                if (data.compensationMode != CompensationMode.NONE) {
                    Text(
                        text = stringResource(R.string.comp_factor_display).format(data.compensationFactor),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (data.isCalibrationActive) viewModel.stopCalibration() else viewModel.startCalibration() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (data.isCalibrationActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(if (data.isCalibrationActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (data.isCalibrationActive) stringResource(R.string.stop_calibration) else stringResource(R.string.start_calibration))
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearCalibration(context) },
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Text(stringResource(R.string.clear_calibration))
                    }
                }

                if (data.calibrationEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { calibrationExpanded = !calibrationExpanded }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${data.calibrationEntries.size} Entries", style = MaterialTheme.typography.titleSmall)
                        Icon(if (calibrationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    }

                    AnimatedVisibility(visible = calibrationExpanded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.chrono_val_header), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.control_val_header), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                            data.calibrationEntries.asReversed().forEachIndexed { index, entry ->
                                val realIndex = data.calibrationEntries.size - 1 - index
                                CalibrationRow(
                                    entry = entry,
                                    velocityUnit = data.velocityUnit,
                                    onUpdateControl = { viewModel.updateCalibrationControlValue(context, realIndex, it) },
                                    onDelete = { viewModel.removeCalibrationEntry(context, realIndex) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weight Type Selection
        Text(stringResource(R.string.weight_config_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeightTypeButton(
                label = stringResource(R.string.weight_type_bbs),
                selected = data.weightType == WeightType.BB,
                onClick = { viewModel.setWeightType(context, WeightType.BB) },
                modifier = Modifier.weight(1f)
            )
            WeightTypeButton(
                label = stringResource(R.string.weight_type_diablos),
                selected = data.weightType == WeightType.DIABLO,
                onClick = { viewModel.setWeightType(context, WeightType.DIABLO) },
                modifier = Modifier.weight(1f)
            )
            WeightTypeButton(
                label = stringResource(R.string.weight_type_custom),
                selected = data.weightType == WeightType.CUSTOM,
                onClick = { viewModel.setWeightType(context, WeightType.CUSTOM) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expandable Custom Weights Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { weightsExpanded = !weightsExpanded }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.custom_weights_list), style = MaterialTheme.typography.titleSmall)
                    Icon(
                        if (weightsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = weightsExpanded) {
                    Column {
                        if (data.customWeights.isEmpty()) {
                            Text(
                                stringResource(R.string.no_custom_weights),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                            )
                        } else {
                            data.customWeights.forEachIndexed { index, custom ->
                                CustomWeightEntry(
                                    index = index,
                                    name = custom.name,
                                    weight = custom.weight,
                                    caliber = custom.caliber,
                                    unit = custom.caliberUnit,
                                    onUpdate = { name, w, cal, u ->
                                        viewModel.updateCustomWeight(context, index, name, w, cal, u)
                                    },
                                    onDelete = { viewModel.removeCustomWeight(context, index) }
                                )
                                if (index < data.customWeights.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.addCustomWeight(context) },
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_row_button))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(stringResource(R.string.ballistics_engine_params), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.fine_tune_physics), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        BallisticSettingField(stringResource(R.string.ballistic_diameter), data.diameterMm.toString()) { viewModel.updateBallisticSettings(diameterMm = it) }
        BallisticSettingField(stringResource(R.string.ballistic_air_density), data.airDensityRho.toString()) { viewModel.updateBallisticSettings(airDensityRho = it) }
        BallisticSettingField(stringResource(R.string.ballistic_drag_coeff), data.dragCoefficientCw.toString()) { viewModel.updateBallisticSettings(dragCoefficientCw = it) }
        BallisticSettingField(stringResource(R.string.ballistic_magnus_coeff), data.magnusCoefficientK.toString()) { viewModel.updateBallisticSettings(magnusCoefficientK = it) }
        BallisticSettingField(stringResource(R.string.ballistic_spin_damping), data.spinDampingCr.toString()) { viewModel.updateBallisticSettings(spinDampingCr = it) }
        BallisticSettingField(stringResource(R.string.ballistic_gravity), data.gravity.toString()) { viewModel.updateBallisticSettings(gravity = it) }

        Spacer(modifier = Modifier.height(32.dp))
        Text("ChronoMate v1.0", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    languages.forEach { (name, code, _) ->
                        LanguageOption(name, code, data.language) {
                            viewModel.setLanguage(context, code)
                            showLanguageDialog = false
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.restart_required_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    if (showFixedChronoWarning) {
    AlertDialog(
        onDismissRequest = {
            showFixedChronoWarning = false
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.fixed_chrono_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.fixed_chrono_warning_part_one
                    )
                )

                Text(
                    text = stringResource(
                        R.string.fixed_chrono_warning_part_two
                    )
                )

                Text(
                    text = stringResource(
                        R.string.fixed_chrono_warning_part_three
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.setFixedChronoProfileEnabled(
                        context,
                        true
                    )
                    showFixedChronoWarning = false
                }
            ) {
                Text(stringResource(R.string.fixed_chrono_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showFixedChronoWarning = false
                }
            ) {
                Text(stringResource(R.string.fixed_chrono_cancel))
            }
        }
    )
}
}

@Composable
fun CalibrationRow(
    entry: CalibrationEntry,
    velocityUnit: VelocityUnit,
    onUpdateControl: (Float?) -> Unit,
    onDelete: () -> Unit
) {
    val displayedControlValue = entry.controlValue?.let {
    velocityUnit.fromMetersPerSecond(it)
}

val displayedControlText = displayedControlValue?.let {
    "%.1f".format(it)
} ?: ""

var textFieldValue by remember(entry.controlValue, velocityUnit) {
    mutableStateOf(
        TextFieldValue(
            text = displayedControlText,
            selection = TextRange(displayedControlText.length)
        )
    )
}

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "%.1f".format(velocityUnit.fromMetersPerSecond(entry.chronoValue)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onUpdateControl(it.text.toFloatOrNull()?.let { displayedValue ->velocityUnit.toMetersPerSecond(displayedValue)})
            },
            modifier = Modifier.weight(1f).height(48.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun CustomWeightEntry(
    index: Int,
    name: String,
    weight: Float,
    caliber: Float,
    unit: String,
    onUpdate: (String, Float, Float, String) -> Unit,
    onDelete: () -> Unit
) {
    var nameText by remember(name) { mutableStateOf(name) }
    var weightValue by remember(weight) {
        mutableStateOf(TextFieldValue(
            text = weight.toString(),
            selection = TextRange(weight.toString().length)
        ))
    }
    var caliberValue by remember(caliber) {
        mutableStateOf(TextFieldValue(
            text = caliber.toString(),
            selection = TextRange(caliber.toString().length)
        ))
    }
    var unitExpanded by remember { mutableStateOf(false) }
    val units = listOf("mm", "inch", "cal", "ga")

    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    onUpdate(it, weight, caliber, unit)
                },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.weight_name_label), fontSize = 10.sp) },
                textStyle = MaterialTheme.typography.bodySmall,
                singleLine = true
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = caliberValue,
                onValueChange = {
                    caliberValue = it
                    it.text.toFloatOrNull()?.let { c -> onUpdate(name, weight, c, unit) }
                },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.caliber_label), fontSize = 10.sp) },
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Box(modifier = Modifier.weight(0.6f)) {
                OutlinedButton(
                    onClick = { unitExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(unit, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                onUpdate(name, weight, caliber, u)
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = weightValue,
            onValueChange = {
                weightValue = it
                it.text.toFloatOrNull()?.let { w -> onUpdate(name, w, caliber, unit) }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.weight_value_label), fontSize = 10.sp) },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}

@Composable
fun WeightTypeButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var resizedFontSize by remember { mutableStateOf(12.sp) }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = resizedFontSize,
                lineHeight = resizedFontSize
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) {
                    resizedFontSize *= 0.95f
                }
            }
        )
    }
}

@Composable
fun LanguageOption(label: String, code: String, currentCode: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = code == currentCode, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun BallisticSettingField(label: String, value: String, onUpdate: (Float) -> Unit) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        ))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            it.text.toFloatOrNull()?.let { f -> onUpdate(f) }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}
