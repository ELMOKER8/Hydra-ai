package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaterLog
import com.example.ui.viewmodel.HydrationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(viewModel: HydrationViewModel) {
    val allLogs by viewModel.allWaterLogs.collectAsState()
    val todayLogs by viewModel.todayWaterLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var activeViewType by remember { mutableStateOf("Weekly") } // Daily, Weekly, Monthly, Yearly

    // Derived values for analytics and cards
    val totalVolume = allLogs.sumOf { it.volumeMl }
    val totalGoal = profile?.dailyGoalMl ?: 2500

    val averageIntake = if (allLogs.isEmpty()) 0f else {
        // Find how many unique days logged
        val uniqueDays = allLogs.map { it.logDate }.distinct().size
        (totalVolume.toFloat() / maxOf(1, uniqueDays)).coerceAtLeast(0f)
    }

    val completionRate = if (allLogs.isEmpty()) 0f else {
        val uniqueDays = allLogs.map { it.logDate }.distinct()
        var completedDays = 0
        uniqueDays.forEach { date ->
            val dailySum = allLogs.filter { it.logDate == date }.sumOf { it.volumeMl }
            if (dailySum >= totalGoal) completedDays++
        }
        (completedDays.toFloat() / uniqueDays.size.toFloat() * 100).coerceIn(0f, 100f)
    }

    val trackingFrequencyByHour = if (allLogs.isEmpty()) "Morning" else {
        val hours = allLogs.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY)
        }
        val morningCount = hours.count { it in 6..11 }
        val afternoonCount = hours.count { it in 12..17 }
        val eveningCount = hours.count { it in 18..23 }
        val nightCount = hours.count { it in 0..5 }

        val max = maxOf(morningCount, afternoonCount, eveningCount, nightCount)
        when (max) {
            morningCount -> "8 AM - 12 PM"
            afternoonCount -> "12 PM - 4 PM"
            eveningCount -> "6 PM - 10 PM"
            else -> "Late Night Log"
        }
    }

    Scaffold(
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Clinical Hydration Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { item ->
                    val isSelected = activeViewType == item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { activeViewType = item }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Central Canvas Drawing Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "$activeViewType Hydration Path",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (activeViewType) {
                            "Daily" -> DailyTrendHourlyLineChart(todayLogs, totalGoal)
                            "Weekly" -> SevenDaysIntakeBarChart(allLogs, totalGoal)
                            "Monthly" -> FourWeeksSparkProgressChart(allLogs, totalGoal)
                            "Yearly" -> YearlyMonthsVolumeBarChart(allLogs)
                        }
                    }
                }
            }

            // Key Hydration Metrics Section
            Text(
                text = "Primary Biological Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                // Metric A: Average Intake
                item {
                    MetricCard(
                        title = "Average Daily",
                        value = "${averageIntake.toInt()} ml",
                        sub = "Overall logged size",
                        icon = Icons.Default.Water,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Metric B: Completion Rate
                item {
                    MetricCard(
                        title = "Completion Pacing",
                        value = "${completionRate.toInt()}%",
                        sub = "Days goal achieved",
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Metric C: Peak Log Frequency Hour
                item {
                    MetricCard(
                        title = "Peak Log Slot",
                        value = trackingFrequencyByHour,
                        sub = "Most consistent tracking",
                        icon = Icons.Default.Timelapse,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Metric D: Total Consumed
                item {
                    MetricCard(
                        title = "Lifetime Intake",
                        value = "${totalVolume} ml",
                        sub = "Total amount tracking",
                        icon = Icons.Default.Opacity,
                        color = Color(0xFFFF6D00)
                    )
                }
            }

            // Streak & Legend Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Consistency Recommendation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (completionRate < 60) {
                                "Try drinking 200ml every hour during your peak morning block to naturally expand your goal completion!"
                            } else {
                                "Excellent job staying hydration efficient! Keep logging consistently to keep your Droppy excited."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, sub: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
    }
}

// --- Canvas Drawings for Charts ---

@Composable
fun DailyTrendHourlyLineChart(logs: List<WaterLog>, goal: Int) {
    val contextColor = MaterialTheme.colorScheme.primary

    // Map logs group by hour intervals
    val hoursArray = IntArray(24) { 0 }
    logs.forEach { log ->
        val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        hoursArray[h] += log.volumeMl
    }

    // Cumulative sum
    val cumulativeIntakes = FloatArray(24) { 0f }
    var runningSum = 0f
    for (i in 0..23) {
        runningSum += hoursArray[i]
        cumulativeIntakes[i] = runningSum
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val pointCount = 24
        val xInterval = width / pointCount
        val maxVolumeVal = maxOf(goal.toFloat(), cumulativeIntakes.maxOrNull() ?: 1000f)

        // Draw guideline for user target goal
        val goalY = height - (goal.toFloat() / maxVolumeVal * height)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, goalY),
            end = Offset(width, goalY),
            strokeWidth = 3f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        val path = Path()
        path.moveTo(0f, height)

        for (i in 0..23) {
            val cx = i * xInterval
            val cy = height - (cumulativeIntakes[i] / maxVolumeVal * height)
            path.lineTo(cx, cy)
        }

        drawPath(
            path = path,
            color = contextColor,
            style = Stroke(width = 6f)
        )
    }
}

@Composable
fun SevenDaysIntakeBarChart(allLogs: List<WaterLog>, goal: Int) {
    val barColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.secondary

    // Calculate volume logged for each of the last 7 calendar days
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = List(7) { offset ->
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        sdf.format(cal.time)
    }.reversed()

    val volumes = dates.map { d ->
        allLogs.filter { it.logDate == d }.sumOf { it.volumeMl }.toFloat()
    }

    val daysLabel = List(7) { offset ->
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        val shortName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
        shortName.take(1) // Single character representations, e.g. M, T, W, T, F, S, S
    }.reversed()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val barCount = 7
        val barWidth = width / (barCount * 2)
        val spacing = width / barCount
        val maxVal = maxOf(goal.toFloat() * 1.25f, volumes.maxOrNull() ?: 1000f)

        for (i in 0 until barCount) {
            val cx = i * spacing + barWidth / 2f
            val barHeight = (volumes[i] / maxVal) * height
            val topY = height - barHeight

            // Draw shadow card background
            drawRoundRect(
                color = barColor.copy(alpha = 0.08f),
                topLeft = Offset(cx, 0f),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Draw active cylinder
            drawRoundRect(
                color = if (volumes[i] >= goal) goalColor else barColor,
                topLeft = Offset(cx, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
    }
}

@Composable
fun FourWeeksSparkProgressChart(allLogs: List<WaterLog>, goal: Int) {
    val barColor = MaterialTheme.colorScheme.secondary

    // Aggregate values for last 4 weeks
    val today = Calendar.getInstance()
    val weeklyVolumes = FloatArray(4) { 0f }
    for (weekOffset in 0 until 4) {
        val weekStart = today.clone() as Calendar
        weekStart.add(Calendar.WEEK_OF_YEAR, -weekOffset)
        // filter dates inside this week
        val datesInWeek = List(7) { d ->
            val cal = weekStart.clone() as Calendar
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.add(Calendar.DAY_OF_YEAR, d)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }
        val volume = allLogs.filter { datesInWeek.contains(it.logDate) }.sumOf { it.volumeMl }
        weeklyVolumes[3 - weekOffset] = volume.toFloat()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val xInterval = width / 3f
        val maxVal = weeklyVolumes.maxOrNull() ?: 1000f

        val path = Path()
        path.moveTo(0f, height - (weeklyVolumes[0] / maxVal * height))

        for (i in 1..3) {
            path.lineTo(i * xInterval, height - (weeklyVolumes[i] / maxVal * height))
        }

        drawPath(
            path = path,
            color = barColor,
            style = Stroke(width = 6f)
        )
    }
}

@Composable
fun YearlyMonthsVolumeBarChart(allLogs: List<WaterLog>) {
    val barColor = MaterialTheme.colorScheme.primary
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Sum logs for last 12 months
    val monthlyVolumes = FloatArray(12) { 0f }
    allLogs.forEach { log ->
        val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
        val yr = cal.get(Calendar.YEAR)
        if (yr == currentYear) {
            val mn = cal.get(Calendar.MONTH) // 0 to 11
            monthlyVolumes[mn] += log.volumeMl
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val barCount = 12
        val barWidth = width / (barCount * 1.8f)
        val spacing = width / barCount
        val maxVal = monthlyVolumes.maxOrNull() ?: 1000f

        for (i in 0 until 12) {
            val cx = i * spacing + barWidth / 2f
            val barHeight = (monthlyVolumes[i] / maxVal) * height
            val topY = height - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(cx, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
}
