package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.notification.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.components.HabitIcons
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    profile: UserProfile?,
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    onOpenHabitDetail: (Habit) -> Unit,
    onCreateHabit: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val todayDateStr = viewModel.todayDateString

    // Request notification permission on Android 13+
    var hasNotificationPermission by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasNotificationPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Determine current day of week (1=Mon, ..., 7=Sun)
    val cal = Calendar.getInstance()
    val todayDow = cal.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }

    // Filter habits scheduled for today
    val todayScheduledHabits = remember(habits, todayDow) {
        habits.filter { habit ->
            viewModel.repository.isHabitScheduledOnDay(habit, todayDow)
        }
    }

    val todayCompletedHabitIds = remember(completions, todayDateStr) {
        completions.filter { it.scheduledDate == todayDateStr }.map { it.habitId }.toSet()
    }

    val completedCount = todayScheduledHabits.count { todayCompletedHabitIds.contains(it.id) }
    val totalScheduled = todayScheduledHabits.size
    val progressPct = if (totalScheduled == 0) 0 else ((completedCount.toFloat() / totalScheduled) * 100).toInt()

    // Greeting logic
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    val formattedDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredHabits = remember(todayScheduledHabits, selectedFilter) {
        when (selectedFilter) {
            "All" -> todayScheduledHabits
            "Productivity" -> todayScheduledHabits.filter { it.category == "Productivity" }
            "Wellness" -> todayScheduledHabits.filter { it.category.contains("wellness", ignoreCase = true) }
            "Learning" -> todayScheduledHabits.filter { it.category == "Learning" }
            else -> todayScheduledHabits
        }
    }

    val upcomingWithReminders = remember(todayScheduledHabits) {
        todayScheduledHabits.filter { it.reminderEnabled && !it.reminderTime.isNullOrBlank() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHabit,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("home_create_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Habit")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${profile?.displayName ?: "Pilot"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Avatar & Pro Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (profile?.isPro == true) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ForestGreen,
                                contentColor = PrimaryAccent
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryAccent.copy(alpha = 0.2f),
                                contentColor = ForestGreen,
                                modifier = Modifier.clickable { onOpenPaywall() }
                            ) {
                                Text(
                                    text = "Upgrade",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ForestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.displayName?.take(1) ?: "P").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Daily Progress Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_daily_progress_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Routine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount of $totalScheduled habits completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = when {
                                    totalScheduled == 0 -> "No habits scheduled today. Enjoy your restful day."
                                    progressPct == 100 -> "All done! Rest easy and recharge without burnout."
                                    progressPct >= 50 -> "Halfway there. One mindful step at a time."
                                    else -> "You're making progress. Keep it simple today."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Circular indicator
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { if (totalScheduled == 0) 0f else completedCount.toFloat() / totalScheduled },
                                modifier = Modifier.size(68.dp),
                                color = PrimaryAccent,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "$progressPct%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Productivity", "Wellness", "Learning").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${todayScheduledHabits.size} scheduled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Habit Cards or Empty State
            if (filteredHabits.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Spa,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (habits.isEmpty()) "No habits yet" else "No habits match this filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (habits.isEmpty()) "Build your first burnout-free habit to get started." else "Switch filters or enjoy your day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (habits.isEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCreateHabit,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForestGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Create Your First Habit")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
                    val isCompleted = todayCompletedHabitIds.contains(habit.id)
                    val streak = viewModel.repository.calculateHabitCurrentStreak(
                        habit,
                        completions.filter { it.habitId == habit.id }
                    )

                    TodayHabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        streak = streak,
                        onToggle = { viewModel.toggleHabitCompletion(habit.id) },
                        onCardClick = { onOpenHabitDetail(habit) }
                    )
                }
            }

            // Upcoming Reminders
            if (upcomingWithReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upcoming Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            upcomingWithReminders.take(3).forEach { habit ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(habit.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = habit.reminderTime ?: "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Motivation Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Spa,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Burnout-Free Tip",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Consistency beats intensity. Two minutes done consistently beats an hour done once a month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayHabitCard(
    habit: Habit,
    isCompleted: Boolean,
    streak: Int,
    onToggle: () -> Unit,
    onCardClick: () -> Unit
) {
    val checkColor by animateColorAsState(
        targetValue = if (isCompleted) ForestGreen else MaterialTheme.colorScheme.surfaceVariant,
        label = "check_color"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) ForestGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("today_habit_card_${habit.id.take(8)}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Colored icon box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(habit.colorHex)).copy(alpha = 0.18f)
                            } catch (_: Exception) {
                                PrimaryAccent.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HabitIcons.getIcon(habit.iconName),
                        contentDescription = habit.name,
                        tint = try {
                            Color(android.graphics.Color.parseColor(habit.colorHex))
                        } catch (_: Exception) {
                            ForestGreen
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (habit.targetValue > 1f) {
                            Text(
                                text = "• ${habit.targetValue.toInt()} ${habit.targetUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Streak & Complete Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarningAmber.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$streak",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }
                }

                // Check button
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(checkColor)
                        .testTag("toggle_complete_${habit.id.take(8)}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (isCompleted) "Completed" else "Mark Complete",
                        tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
