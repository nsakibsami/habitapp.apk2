package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.components.CreateEditHabitDialog
import com.example.ui.components.HabitDetailSheet
import com.example.ui.components.HabitIcons
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.WarningAmber

@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    profile: UserProfile?,
    activeHabits: List<Habit>,
    archivedHabits: List<Habit>,
    completions: List<HabitCompletion>,
    onOpenPaywall: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = Archived
    var selectedCategory by remember { mutableStateOf("All") }
    var habitToDetail by remember { mutableStateOf<Habit?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val isPro = profile?.isPro == true
    val activeCount = activeHabits.size
    val freeLimitReached = !isPro && activeCount >= 5

    val currentList = if (selectedTab == 0) activeHabits else archivedHabits

    val filteredList = remember(currentList, selectedCategory) {
        if (selectedCategory == "All") currentList
        else currentList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (freeLimitReached) {
                            onOpenPaywall()
                        } else {
                            showCreateDialog = true
                        }
                    },
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("habits_screen_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Habit")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habit Library",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPro) "HabitPilot Pro • Unlimited Habits" else "$activeCount of 5 habits used on Free",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (freeLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isPro) {
                    TextButton(onClick = onOpenPaywall) {
                        Text("Get Unlimited", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active (${activeHabits.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Archived (${archivedHabits.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Productivity", "Physical wellness", "Mental wellness", "Learning").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.replace(" wellness", ""), fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Habit List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedTab == 0) "No active habits found" else "No archived habits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedTab == 0) "Create a new habit to start building your routine." else "Archived habits will be stored here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(filteredList, key = { it.id }) { habit ->
                        val habitCompletions = remember(completions, habit.id) {
                            completions.filter { it.habitId == habit.id }
                        }
                        val streak = remember(habit, habitCompletions) {
                            viewModel.repository.calculateHabitCurrentStreak(habit, habitCompletions)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { habitToDetail = habit }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                try {
                                                    Color(android.graphics.Color.parseColor(habit.colorHex)).copy(alpha = 0.2f)
                                                } catch (_: Exception) {
                                                    PrimaryAccent.copy(alpha = 0.2f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = HabitIcons.getIcon(habit.iconName),
                                            contentDescription = null,
                                            tint = try {
                                                Color(android.graphics.Color.parseColor(habit.colorHex))
                                            } catch (_: Exception) {
                                                ForestGreen
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(habit.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (habit.reminderEnabled && !habit.reminderTime.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(habit.reminderTime ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (streak > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WarningAmber.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                                                Text("$streak", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WarningAmber)
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (habit.isArchived) viewModel.restoreHabit(habit.id)
                                            else viewModel.archiveHabit(habit.id)
                                        }
                                    ) {
                                        Icon(
                                            if (habit.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                            contentDescription = if (habit.isArchived) "Restore" else "Archive",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Habit Dialog
    if (showCreateDialog) {
        CreateEditHabitDialog(
            initialHabit = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, desc, cat, icon, color, freq, days, rem, time, target, unit, streakContr ->
                viewModel.createHabit(
                    name = name,
                    description = desc,
                    category = cat,
                    iconName = icon,
                    colorHex = color,
                    frequencyType = freq,
                    weekdays = days,
                    reminderEnabled = rem,
                    reminderTime = time,
                    targetValue = target,
                    targetUnit = unit,
                    contributesToStreaks = streakContr
                )
                showCreateDialog = false
            }
        )
    }

    // Edit Habit Dialog
    habitToEdit?.let { habit ->
        CreateEditHabitDialog(
            initialHabit = habit,
            onDismiss = { habitToEdit = null },
            onSave = { name, desc, cat, icon, color, freq, days, rem, time, target, unit, streakContr ->
                viewModel.updateHabit(
                    habit.copy(
                        name = name,
                        description = desc,
                        category = cat,
                        iconName = icon,
                        colorHex = color,
                        frequencyType = freq,
                        scheduledWeekdays = days,
                        reminderEnabled = rem,
                        reminderTime = time,
                        targetValue = target,
                        targetUnit = unit,
                        contributesToStreaks = streakContr
                    )
                )
                habitToEdit = null
            }
        )
    }

    // Detail Sheet
    habitToDetail?.let { habit ->
        HabitDetailSheet(
            habit = habit,
            viewModel = viewModel,
            completions = completions,
            onDismiss = { habitToDetail = null },
            onEdit = {
                val h = habitToDetail
                habitToDetail = null
                habitToEdit = h
            },
            onArchiveToggle = {
                if (habit.isArchived) viewModel.restoreHabit(habit.id)
                else viewModel.archiveHabit(habit.id)
                habitToDetail = null
            },
            onDelete = {
                viewModel.deleteHabit(habit)
                habitToDetail = null
            }
        )
    }
}
