package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.HydrationViewModel
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(navController: NavController, viewModel: HydrationViewModel) {
    val step = viewModel.onboardingStep
    val tempProfile = viewModel.tempProfile

    // Slide transition based on steps moving forward or backward
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    IconButton(onClick = { viewModel.previousOnboardingStep() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = "Step $step of 11",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { step / 11f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Body Card Content with Animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> Step1UserName(viewModel)
                    2 -> Step2Gender(viewModel)
                    3 -> Step3Age(viewModel)
                    4 -> Step4Height(viewModel)
                    5 -> Step5Weight(viewModel)
                    6 -> Step6ActivityLevel(viewModel)
                    7 -> Step7GoalSelection(viewModel)
                    8 -> Step8ClimateSelection(viewModel)
                    9 -> Step9HealthConditions(viewModel)
                    10 -> Step10WakeTime(viewModel)
                    11 -> Step11SleepTime(viewModel)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation actions
            Button(
                onClick = {
                    if (step == 11) {
                        viewModel.nextOnboardingStep()
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        viewModel.nextOnboardingStep()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (step == 11) "Calculate & Onboard" else "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Proceed"
                )
            }
        }
    }
}

// Step UI subcomponents

@Composable
fun Step1UserName(viewModel: HydrationViewModel) {
    var rawText by remember { mutableStateOf(TextFieldValue(viewModel.tempProfile.name)) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to Hydra AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "What should your biological AI coach call you?",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = rawText,
            onValueChange = {
                rawText = it
                viewModel.updateName(it.text)
            },
            placeholder = { Text("Enter your name") },
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun Step2Gender(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.gender
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select Gender",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("Male", "Female").forEach { gender ->
                val isSelected = selected == gender
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.updateGender(gender) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gender,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun Step3Age(viewModel: HydrationViewModel) {
    var ageSliderValue by remember { mutableFloatStateOf(viewModel.tempProfile.age.toFloat()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your Age",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Age affects metabolic hydration efficiency.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "${ageSliderValue.roundToInt()} Years Old",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = ageSliderValue,
            onValueChange = {
                ageSliderValue = it
                viewModel.updateAge(it.roundToInt())
            },
            valueRange = 10f..100f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Step4Height(viewModel: HydrationViewModel) {
    var heightSliderValue by remember { mutableFloatStateOf(viewModel.tempProfile.heightCm.toFloat()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Specify Height",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "${heightSliderValue.roundToInt()} cm",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = heightSliderValue,
            onValueChange = {
                heightSliderValue = it
                viewModel.updateHeight(it.roundToInt())
            },
            valueRange = 100f..230f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Step5Weight(viewModel: HydrationViewModel) {
    var weightSliderValue by remember { mutableFloatStateOf(viewModel.tempProfile.weightKg.toFloat()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Specify Weight",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Core variable for base water requirement calculation.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "${weightSliderValue.roundToInt()} kg",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = weightSliderValue,
            onValueChange = {
                weightSliderValue = it
                viewModel.updateWeight(it.roundToInt())
            },
            valueRange = 30f..180f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Step6ActivityLevel(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.activityLevel
    val levels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Athlete")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sweat output directly changes your hydration target.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            levels.forEach { level ->
                val isSelected = selected == level
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.updateActivityLevel(level) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = level,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun Step7GoalSelection(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.hydrationGoal
    val goals = listOf("General Health", "Better Skin", "Weight Loss", "Muscle Growth", "Athletic Performance")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Hydration Goal",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI adjusts water intake to support physical objectives.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            goals.forEach { goal ->
                val isSelected = selected == goal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.updateHydrationGoal(goal) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun Step8ClimateSelection(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.climate
    val climates = listOf("Cold", "Mild", "Hot", "Extreme Heat")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your Climate",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Heat increases perspiration and dehydration speeds.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(climates) { climate ->
                val isSelected = selected == climate
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.updateClimate(climate) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = climate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun Step9HealthConditions(viewModel: HydrationViewModel) {
    val selectedStr = viewModel.tempProfile.healthConditions
    val conditionsList = listOf("None", "Kidney Stone History", "Diabetes", "High Blood Pressure", "Athlete", "Other")

    // Parse selection
    val selectedSet = remember {
        mutableStateListOf<String>().apply {
            if (selectedStr.isNotEmpty()) addAll(selectedStr.split(", "))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Health Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select any conditions that apply (Multi-select).",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(conditionsList) { condition ->
                val isSelected = selectedSet.contains(condition)
                Box(
                    modifier = Modifier
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (condition == "None") {
                                selectedSet.clear()
                                selectedSet.add("None")
                            } else {
                                selectedSet.remove("None")
                                if (isSelected) {
                                    selectedSet.remove(condition)
                                    if (selectedSet.isEmpty()) selectedSet.add("None")
                                } else {
                                    selectedSet.add(condition)
                                }
                            }
                            viewModel.updateHealthConditions(selectedSet.joinToString(", "))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = condition,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Step10WakeTime(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.wakeUpTime
    var hour by remember { mutableIntStateOf(selected.split(":")[0].toInt()) }
    var min by remember { mutableIntStateOf(selected.split(":")[1].toInt()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Wake-up Time",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Schedules and dynamic hydration cycles start here.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            NumberScrollColumn(value = hour, limit = 24, label = "Hour") {
                hour = it
                viewModel.updateWakeTime(String.format("%02d:%02d", hour, min))
            }
            Text(text = ":", fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            NumberScrollColumn(value = min, limit = 60, label = "Min") {
                min = it
                viewModel.updateWakeTime(String.format("%02d:%02d", hour, min))
            }
        }
    }
}

@Composable
fun Step11SleepTime(viewModel: HydrationViewModel) {
    val selected = viewModel.tempProfile.sleepTime
    var hour by remember { mutableIntStateOf(selected.split(":")[0].toInt()) }
    var min by remember { mutableIntStateOf(selected.split(":")[1].toInt()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Sleep Time",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Water coaching shuts off sleep hours to prevent disruptions.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            NumberScrollColumn(value = hour, limit = 24, label = "Hour") {
                hour = it
                viewModel.updateSleepTime(String.format("%02d:%02d", hour, min))
            }
            Text(text = ":", fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            NumberScrollColumn(value = min, limit = 60, label = "Min") {
                min = it
                viewModel.updateSleepTime(String.format("%02d:%02d", hour, min))
            }
        }
    }
}

@Composable
fun NumberScrollColumn(value: Int, limit: Int, label: String, onChosen: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChosen((value + limit - 1) % limit) }) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = String.format("%02d", value),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { onChosen((value + 1) % limit) }) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
