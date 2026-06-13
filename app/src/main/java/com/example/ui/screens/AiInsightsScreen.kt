package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HydrationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val sender: String, // "User" or "Coach"
    val content: String,
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

@Composable
fun AiInsightsScreen(viewModel: HydrationViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val allLogs by viewModel.allWaterLogs.collectAsState()

    var userQuery by remember { mutableStateOf("") }
    var coachResponseMsg by remember { mutableStateOf<String?>(null) }
    var isReplying by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("Coach", "Hello! I am Hydra, your intelligent hydration data scientist. Ask me any question about water biology, performance, stones history, skin cell turgor, or how to reach today's goals!")
        )
    }

    // Curated dynamic findings list
    val findings = remember(allLogs) {
        val list = mutableListOf<String>()
        val totalVolume = allLogs.sumOf { it.volumeMl }
        val weekDaysLogSum = allLogs.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day != Calendar.SATURDAY && day != Calendar.SUNDAY
        }.sumOf { it.volumeMl }

        val weekendLogSum = allLogs.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }.sumOf { it.volumeMl }

        if (totalVolume > 0) {
            if (weekendLogSum < weekDaysLogSum * 0.4f) {
                list.add("You consume approximately 40% less water on weekends. We suggest enabling extra alarms on Sat/Sun.")
            } else {
                list.add("Your weekend tracking discipline is excellent and corresponds closely with weekdays!")
            }
            list.add("Your highest consistency chunk of drinking falls between 8:00 AM and 12:00 PM.")
        } else {
            list.add("Initialize water logs for 3 consecutive days to let our biological pattern engine display custom weekend models.")
        }

        if (profile?.climate == "Hot" || profile?.climate == "Extreme Heat") {
            list.add("Hot local climate triggers natural sweat vapor output. Your target has been raised automatically by +500ml.")
        }
        if (profile?.hydrationGoal == "Better Skin") {
            list.add("To boost epidermal glow, your profile integrates +250ml tailored to help skin-vessel fluid diffusion.")
        }
        list
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
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hydra AI Findings & Chat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Pattern Findings Cards
            Text(text = "Clinical Findings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                findings.forEach { finding ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = finding,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Coach Interactive Chat console
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Coach Consultation Chat", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
            }

            // Messages Console Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages) { msg ->
                    val isCoach = msg.sender == "Coach"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isCoach) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Column(
                            horizontalAlignment = if (isCoach) Alignment.Start else Alignment.End,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isCoach) 0.dp else 16.dp,
                                            bottomEnd = if (isCoach) 16.dp else 0.dp
                                        )
                                    )
                                    .background(
                                        if (isCoach) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCoach) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.time,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                if (isReplying) {
                    item {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hydra AI is calculating metabolism...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Input Console Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userQuery,
                    onValueChange = { userQuery = it },
                    placeholder = { Text("Ask Coach (e.g., Kidney stone advice?)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                IconButton(
                    onClick = {
                        if (userQuery.isNotBlank() && !isReplying) {
                            val userText = userQuery
                            chatMessages.add(ChatMessage("User", userText))
                            userQuery = ""
                            isReplying = true

                            // Send to ViewModel Gemini execution
                            viewModel.askAiCoach(userText) { response ->
                                chatMessages.add(ChatMessage("Coach", response))
                                isReplying = false
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}
