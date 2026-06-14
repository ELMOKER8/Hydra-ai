package com.example.ui.screens

import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.ui.viewmodel.HydrationViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HydrationViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val todayLogs by viewModel.todayWaterLogs.collectAsState()
    val context = LocalContext.current

    val currentIntake = todayLogs.sumOf { it.volumeMl }
    val userGoal = profile?.dailyGoalMl ?: 2500
    val progressPercent = if (userGoal > 0) (currentIntake.toFloat() / userGoal.toFloat()).coerceIn(0f, 2f) else 0f

    var showLogSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.gamificationMessage) {
        val msg = viewModel.gamificationMessage
        if (!msg.isNullOrBlank()) {
            // Trigger feedback sound/vibrator if enabled
            if (profile?.vibrationEnabled == true) {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    if (viewModel.gamificationMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearGamificationMessage() },
            confirmButton = {
                Button(onClick = { viewModel.clearGamificationMessage() }) {
                    Text("Awesome!")
                }
            },
            title = { Text("Hydration Update", fontWeight = FontWeight.Bold) },
            text = { Text(viewModel.gamificationMessage ?: "") },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp) // Offset for custom navigation
            ) {
                Icon(Icons.Default.LocalActivity, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I Drank Water", fontWeight = FontWeight.Bold)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Welcome Hub
            UserProfileHeader(profile)

            // Gamification Streak & Levels Row
            GamificationTrackerHeader(profile)

            // Dynamic Animated Wave Hydration Ring
            HydrationWaveRing(
                progressPercent = progressPercent,
                currentIntake = currentIntake,
                userGoal = userGoal
            )

            // Dynamic Mascot Droppy Card
            MascotDroppyGuidance(progressPercent, viewModel.aiInsightText, viewModel.isLoadingInsight) {
                viewModel.generateDailyCoachInsight()
            }

            // Quick Stats Row & Weather Card
            GridWidgetsRow(profile, todayLogs)

            // Recent Drink History Mini-List
            RecentLogTimeline(todayLogs) { deletedLog ->
                viewModel.deleteWaterLog(deletedLog)
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for floating layout
        }
    }

    if (showLogSheet) {
        HydrationLogBottomSheet(
            onDismiss = { showLogSheet = false },
            onLogAdd = { amount, type ->
                viewModel.logWaterIntake(amount, type)
                showLogSheet = false
            }
        )
    }
}

@Composable
fun UserProfileHeader(profile: UserProfile?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome, ${profile?.name ?: "User"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Let's stay pure and hydrated today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun GamificationTrackerHeader(profile: UserProfile?) {
    val xpProgress = (profile?.currentXp ?: 0) / 100f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Level block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${profile?.currentLevel ?: 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = getRankTitle(profile?.currentLevel ?: 1),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile?.currentXp ?: 0}/100 XP to next level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Streak Block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF6D00),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${profile?.currentStreak ?: 0} Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6D00)
                    )
                    Text(
                        text = "Streak Record",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

fun getRankTitle(level: Int): String {
    return when {
         level < 3 -> "Beginner"
         level < 8 -> "Explorer"
         level < 15 -> "Hydration Apprentice"
         level < 25 -> "Hydration Expert"
         level < 40 -> "Hydration Master"
         level < 60 -> "Hydration Elite"
         else -> "Hydration Legend"
    }
}

@Composable
fun HydrationWaveRing(progressPercent: Float, currentIntake: Int, userGoal: Int) {
    // Wave offsets and pulse animation states
    val waveOffsetTransition = rememberInfiniteTransition(label = "waveOffset")
    val wavePhase by waveOffsetTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .size(240.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring shadow glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(6.dp, primaryColor.copy(alpha = 0.12f), CircleShape)
        )

        // Custom canvas wave ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate active height level based on progress percent
            val fillHeightY = canvasHeight - (canvasHeight * progressPercent.coerceIn(0f, 1f))

            // 1. Draw Liquid sin Waves if there is any progress > 0
            if (progressPercent > 0.01f) {
                val wavePath = Path()
                val waveSecondPath = Path()

                wavePath.moveTo(0f, canvasHeight)
                waveSecondPath.moveTo(0f, canvasHeight)

                for (x in 0..canvasWidth.toInt()) {
                    // Two waves superimposed with offset phase for realistic dynamic 2.5D liquid feel
                    val y = fillHeightY + (10f * sin((x / 50f) + wavePhase))
                    wavePath.lineTo(x.toFloat(), y)

                    val y2 = fillHeightY + (7f * sin((x / 75f) - wavePhase + 1f))
                    waveSecondPath.lineTo(x.toFloat(), y2)
                }

                wavePath.lineTo(canvasWidth, canvasHeight)
                wavePath.close()

                waveSecondPath.lineTo(canvasWidth, canvasHeight)
                waveSecondPath.close()

                // Draw secondary deeper wave (underlay) with some translucency
                drawPath(
                    path = waveSecondPath,
                    color = secondaryColor.copy(alpha = 0.35f)
                )

                // Draw primary top wave
                drawPath(
                    path = wavePath,
                    color = primaryColor.copy(alpha = 0.65f)
                )
            }

            // Draw clean circle rim border over top
            drawCircle(
                color = primaryColor,
                radius = (canvasWidth / 2f) - 3f,
                style = Stroke(width = 6f)
            )
        }

        // Inside Ring metrics display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(progressPercent * 100).toInt()}%",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = if (progressPercent > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$currentIntake / $userGoal ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (progressPercent > 0.65f) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MascotDroppyGuidance(progress: Float, coachText: String, isLoading: Boolean, onRefresh: () -> Unit) {
    // Resolve Droppy's mascot visual expression icon
    val expression = when {
        progress >= 1.0f -> "🥳" // Celebrating
        progress >= 0.8f -> "🤩" // Excited
        progress >= 0.5f -> "😊" // Happy
        progress >= 0.3f -> "🙂" // Motivating
        else -> "🥺" // Sad
    }

    val stateLabel = when {
        progress >= 1.0f -> "Droppy is Celebrating!"
        progress >= 0.8f -> "Droppy is Excited!"
        progress >= 0.5f -> "Droppy is Happy!"
        progress >= 0.3f -> "Droppy is Motivating!"
        else -> "Droppy is Sad & Thirsty!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = expression,
                    fontSize = 48.sp,
                    modifier = Modifier.shadow(2.dp, CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Droppy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ask Dynamic Coach",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (isLoading) {
                    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                } else {
                    Text(
                        text = "\"$coachText\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun GridWidgetsRow(profile: UserProfile?, logs: List<WaterLog>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Widget A: Deficit / Remaining ml
        val totalLogged = logs.sumOf { it.volumeMl }
        val goal = profile?.dailyGoalMl ?: 2500
        val remaining = maxOf(0, goal - totalLogged)

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (remaining > 0) "$remaining ml" else "Completed!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Remaining To Go",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Widget B: Weather Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            val climate = profile?.climate ?: "Mild"
            val icon = when (climate) {
                "Cold" -> Icons.Default.AcUnit
                "Mild" -> Icons.Default.WbCloudy
                "Hot" -> Icons.Default.WbSunny
                "Extreme Heat" -> Icons.Default.Thermostat
                else -> Icons.Default.WbCloudy
            }
            val desc = when (climate) {
                "Cold" -> "No climate extra ml"
                "Mild" -> "+200ml added"
                "Hot" -> "+500ml added"
                "Extreme Heat" -> "+1000ml added"
                else -> ""
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = climate,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun RecentLogTimeline(logs: List<WaterLog>, onDelete: (WaterLog) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Log Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No water logs today. Grab your glass and track some!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(logs) { log ->
                        WaterLogMiniCard(log, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun WaterLogMiniCard(log: WaterLog, onDelete: (WaterLog) -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val containerIcon = getContainerIcon(log.containerType)

    Box(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sdf.format(Date(log.timestamp)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete(log) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = containerIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${log.volumeMl} ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = log.containerType,
                fontSize = 10.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

fun getContainerIcon(containerType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (containerType) {
        "Espresso Cup" -> Icons.Default.Coffee
        "Tea Cup" -> Icons.Default.LocalCafe
        "Glass", "Large Glass" -> Icons.Default.LocalBar
        "Bottle", "Sports Bottle", "Large Bottle" -> Icons.Default.LocalDrink
        else -> Icons.Default.LocalCafe
    }
}

// --- Container logging sheets ---

data class LogContainerOption(
    val name: String,
    val amount: Int,
    val description: String
)

@Composable
fun HydrationLogBottomSheet(
    onDismiss: () -> Unit,
    onLogAdd: (Int, String) -> Unit
) {
    val containers = listOf(
        LogContainerOption("Espresso Cup", 50, "Quick espresso shot size"),
        LogContainerOption("Tea Cup", 100, "Morning warm brew"),
        LogContainerOption("Glass", 200, "Standard office cup"),
        LogContainerOption("Large Glass", 300, "Heavier glass size"),
        LogContainerOption("Bottle", 500, "Plastic or thermal container"),
        LogContainerOption("Sports Bottle", 750, "Active workout companion"),
        LogContainerOption("Large Bottle", 1000, "Total hydration flask")
    )

    var customAmount by remember { mutableFloatStateOf(250f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Sliding bottom sheet dialog style in Compose
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pill drag handle
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
            )

            Text(
                text = "Log Your Hydration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic grid of containers
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                itemsIndexed(containers) { index, item ->
                    val isSelected = selectedIndex == index
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedIndex = index
                                onLogAdd(item.amount, item.name)
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getContainerIcon(item.name),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.amount} ml",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Custom log slot
                item {
                    val isSelected = selectedIndex == 99
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedIndex = 99 }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Slider",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Custom slider option if 99 is highlighted
            if (selectedIndex == 99) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custom Intake Amount:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${customAmount.roundToInt()} ml", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    }
                    Slider(
                        value = customAmount,
                        onValueChange = { customAmount = it },
                        valueRange = 50f..1500f,
                        steps = 29
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onLogAdd(customAmount.roundToInt(), "Custom Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log ${customAmount.roundToInt()} ml", fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    }
}
