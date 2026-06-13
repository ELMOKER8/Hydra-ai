package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HydrationViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: HydrationViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }

    // Read current variables local states
    val reminderMode = profile?.reminderMode ?: "Smart AI"
    val soundEnabled = profile?.soundEnabled ?: true
    val vibrationEnabled = profile?.vibrationEnabled ?: true
    val unitSystem = profile?.unitSystem ?: "ml"
    val dailyGoal = profile?.dailyGoalMl ?: 2500
    val activeTheme = profile?.chosenTheme ?: "Ocean Theme"

    var localGoalSlider by remember(dailyGoal) { mutableFloatStateOf(dailyGoal.toFloat()) }

    val themesList = listOf(
        "Light Mode", "Dark Mode", "AMOLED Mode", "Ocean Theme", "Ice Theme", "Midnight Theme", "Dynamic Material You"
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Coach Control Room",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic Goal Override Modifier
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Daily Hydration Objective Override", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manually redefine target (otherwise computed balance: weight x 35ml).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Goal:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${localGoalSlider.roundToInt()} ml", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = localGoalSlider,
                        onValueChange = {
                            localGoalSlider = it
                            viewModel.updateDailyGoalOverride(it.roundToInt())
                        },
                        valueRange = 1000f..5000f,
                        steps = 40
                    )
                }
            }

            // Visual Themes Presets
            Text(text = "Visual Palette Style", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    themesList.forEach { theme ->
                        val isSelected = activeTheme == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .clickable { viewModel.updateTheme(theme) }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = theme,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active Theme", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Reminders Schedules & Clinical Modes
            Text(text = "Clinical Schedules Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Coach Reminder Modes
                    Column {
                        Text("Coach Reminder Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Gentle", "Standard", "Smart AI", "Aggressive").forEach { rMode ->
                                val isSelected = reminderMode == rMode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            viewModel.updatePreferences(
                                                reminderMode = rMode,
                                                soundEnabled = soundEnabled,
                                                vibrationEnabled = vibrationEnabled,
                                                unitSystem = unitSystem
                                            )
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rMode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Audio feedback Sound enabled
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Coach Feedback Sounds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Audible chime alerts on logging", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                viewModel.updatePreferences(
                                    reminderMode = reminderMode,
                                    soundEnabled = it,
                                    vibrationEnabled = vibrationEnabled,
                                    unitSystem = unitSystem
                                )
                            }
                        )
                    }

                    // Tactile Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tactile Haptic Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Vibrate device screen on log completions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                viewModel.updatePreferences(
                                    reminderMode = reminderMode,
                                    soundEnabled = soundEnabled,
                                    vibrationEnabled = it,
                                    unitSystem = unitSystem
                                )
                            }
                        )
                    }

                    // Measurement Standard units
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Standard Units Scale", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Toggle ratios between Metric and US", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Row {
                            listOf("ml", "oz").forEach { system ->
                                val isSelected = unitSystem == system
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = if (system == "ml") 6.dp else 0.dp, bottomStart = if (system == "ml") 6.dp else 0.dp, topEnd = if (system == "oz") 6.dp else 0.dp, bottomEnd = if (system == "oz") 6.dp else 0.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.updatePreferences(
                                                reminderMode = reminderMode,
                                                soundEnabled = soundEnabled,
                                                vibrationEnabled = vibrationEnabled,
                                                unitSystem = system
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = system,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clean Reset Data Block
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Profile & Database", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                        Toast.makeText(context, "Hydra database completely wiped!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Wipe Database Warning", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear your water intake history, your streaks, levels, profile parameters, and achievements. Ensure backup before confirming.") },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
