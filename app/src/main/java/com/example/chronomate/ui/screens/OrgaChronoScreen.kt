package com.example.chronomate.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronomate.R
import com.example.chronomate.model.BbProfile
import com.example.chronomate.model.ChronoData
import com.example.chronomate.viewmodel.ChronoViewModel
import kotlin.math.pow

@Composable
fun OrgaChronoScreen(data: ChronoData, viewModel: ChronoViewModel) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isLandscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
    val isDark = data.isDarkMode

    val activeProfiles = data.bbProfiles.filter { it.enabled }
    val weights = activeProfiles.map { it.weightGrams }
    
    // We get the raw velocity float from the latest shot for precision
    val latestVelocity = data.shots.lastOrNull()?.velocity ?: 0f
    
    val maxWeightKg = if (latestVelocity > 0f) (2f * data.maxAllowedJoule) / latestVelocity.pow(2) else 0f
    val maxWeightGrams = maxWeightKg * 1000f

    // Calculate practical weight (highest available weight that stays within Joule limit)
    val practicalWeight = weights.lastOrNull { it <= maxWeightGrams } ?: 0f
    val sparePercentage = if (practicalWeight > 0f) {
        ((maxWeightGrams - practicalWeight) / practicalWeight) * 100f
    } else 0f

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    OrgaHeroSection(data = data, practicalWeight = practicalWeight, sparePercentage = sparePercentage)
                    Spacer(modifier = Modifier.height(16.dp))
                    OrgaSettingsSection(data = data, viewModel = viewModel)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(2.5f)) { 
                    Text(stringResource(R.string.joule_grid_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activeProfiles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No weights available. Check settings.")
                        }
                    } else {
                        JouleGridStatic(
                            velocity = latestVelocity,
                            profiles = activeProfiles,
                            selectedProfileId = data.selectedBbProfileId,
                            practicalWeight = practicalWeight,
                            columns = 4,
                            isDark = isDark,
                            onProfileSelected = { profileId ->
                                viewModel.selectBbProfile(
                                    context,
                                    profileId
                                )
                            }
                        )
                    }
                }
            }
        } else {
            OrgaHeroSection(data = data, practicalWeight = practicalWeight, sparePercentage = sparePercentage)
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.joule_grid_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (activeProfiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No weights available. Check settings.")
                }
            } else {
                JouleGridStatic(
                    velocity = latestVelocity,
                    profiles = activeProfiles,
                    selectedProfileId = data.selectedBbProfileId,
                    practicalWeight = practicalWeight,
                    columns = 2,
                    isDark = isDark,
                    onProfileSelected = { profileId ->
                        viewModel.selectBbProfile(
                            context,
                            profileId
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            OrgaSettingsSection(data = data, viewModel = viewModel)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun JouleGridStatic(
    velocity: Float,
    profiles: List<BbProfile>,
    selectedProfileId: String?,
    practicalWeight: Float,
    columns: Int,
    isDark: Boolean,
    onProfileSelected: (String) -> Unit
) {
    val rows = profiles.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowProfiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowProfiles.forEach { profile ->
                    val weight = profile.weightGrams
                    val label = profile.name
                    val selected =
                        profile.id == selectedProfileId
                    val energy =
                        0.5f *
                            (weight / 1000f) *
                            velocity.pow(2)
                    
                    // Determine colors based on limits and theme
                    val (bgColor, contentColor) = when {
                        velocity <= 0f -> {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) to 
                                (if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black)
                        }
                        weight < practicalWeight -> {
                            if (isDark) Color(0xFF1B5E20) to Color(0xFF4CAF50)
                            else Color(0xFF2E7D32) to Color.White
                        }
                        weight == practicalWeight -> {
                            if (isDark) Color(0xFFE65100) to Color(0xFFFFEB3B)
                            else Color(0xFFEF6C00) to Color.White
                        }
                        else -> {
                            if (isDark) Color(0xFFB71C1C) to Color(0xFFF44336)
                            else Color(0xFFC62828) to Color.White
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onProfileSelected(profile.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                bgColor
                            }
                        ),
                        border = if (selected) {
                            androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            )
                        } else {
                            null
                        }
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label, 
                                style = MaterialTheme.typography.labelSmall, 
                                color = contentColor.copy(alpha = 0.8f)
                            )
                            Text(
                                "%.2f J".format(energy), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Black,
                                color = contentColor
                            )
                        }
                    }
                }
                if (rowProfiles.size < columns) {
                    Spacer(
                        modifier = Modifier.weight(
                            (columns - rowProfiles.size).toFloat()
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OrgaHeroSection(data: ChronoData, practicalWeight: Float, sparePercentage: Float) {
    val isDark = data.isDarkMode
    val chronoGreen = Color(0xFF88FF11)
    val contentColor = if (!isDark) MaterialTheme.colorScheme.onPrimaryContainer else chronoGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!isDark) MaterialTheme.colorScheme.primaryContainer else Color.Black
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.latest_shot),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.6f)
            )
            Text(
                text = data.velocity.toFloatOrNull()?.let { data.velocityUnit.fromMetersPerSecond(it) }?.let { "%.1f %s".format(it, data.velocityUnit.symbol) }?: "0.0 ${data.velocityUnit.symbol}",
                fontSize = 48.sp,
                color = contentColor,
                fontWeight = FontWeight.Black
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp), 
                color = contentColor.copy(alpha = 0.1f)
            )
            Text(
                text = stringResource(R.string.max_practical_weight),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
            if (practicalWeight > 0f) {
                Text(
                    text = "%.2f g".format(practicalWeight),
                    fontSize = 32.sp,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.room_to_limit).format(sparePercentage),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = stringResource(R.string.over_limit),
                    fontSize = 32.sp,
                    color = if (!isDark) Color(0xFFB71C1C) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.too_hot_for_20),
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (!isDark) Color(0xFFB71C1C) else Color.Red).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun OrgaSettingsSection(data: ChronoData, viewModel: ChronoViewModel) {
    var textFieldValue by remember(data.maxAllowedJoule) { 
        mutableStateOf(TextFieldValue(
            text = data.maxAllowedJoule.toString(),
            selection = TextRange(data.maxAllowedJoule.toString().length)
        )) 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.orga_limits), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { 
                    textFieldValue = it
                    it.text.toFloatOrNull()?.let { joule ->
                        viewModel.setMaxAllowedJoule(joule)
                    }
                },
                label = { Text(stringResource(R.string.max_allowed_joule_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}
