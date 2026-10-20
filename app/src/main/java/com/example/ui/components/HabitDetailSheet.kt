package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.ui.MainViewModel
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitDetailSheet(
    habit: Habit,
    viewModel: MainViewModel,
    completions: List<HabitCompletion>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val habitCompletions = remember(completions, habit.id) {
        completions.filter { it.habitId == habit.id }
    }
    val currentStreak = remember(habit, habitCompletions) {
        viewModel.repository.calculateHabitCurrentStreak(habit, habitCompletions)
    }
    val bestStreak = remember(habit, habitCompletions) {
        viewModel.repository.calculateHabitBestStreak(habit, habitCompletions)
    }
    val totalCount = habitCompletions.size

    val completionDates = remember(habitCompletions) {
        habitCompletions.map { it.scheduledDate }.toSet()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(habit.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(habit.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (habit.description.isNotBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stat Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                                    Text("$currentStreak", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                }
                                Text("Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                    Text("$bestStreak", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                }
                                Text("Best Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$totalCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text("Total Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Schedule Info
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Schedule & Target", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = "• Frequency: ${if (habit.frequencyType == "DAILY") "Every Day" else "Specific Weekdays"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (habit.reminderEnabled && !habit.reminderTime.isNullOrBlank()) {
                                Text(
                                    text = "• Daily Reminder: ${habit.reminderTime}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (habit.targetValue > 1f) {
                                Text(
                                    text = "• Target: ${habit.targetValue.toInt()} ${habit.targetUnit}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // 28-day Activity Matrix
                    Text("Recent 28 Days", style = MaterialTheme.typography.labelLarge)
                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                    val cal = Calendar.getInstance()
                    val daysList = remember {
                        val list = mutableListOf<Pair<String, Boolean>>()
                        for (i in 0 until 28) {
                            val dayCal = cal.clone() as Calendar
                            dayCal.add(Calendar.DAY_OF_YEAR, -i)
                            val dStr = sdf.format(dayCal.time)
                            list.add(dStr to completionDates.contains(dStr))
                        }
                        list.reversed()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (week in 0 until 4) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (day in 0 until 7) {
                                    val index = week * 7 + day
                                    if (index < daysList.size) {
                                        val (_, completed) = daysList[index]
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (completed) ForestGreen else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = onArchiveToggle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (habit.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (habit.isArchived) "Restore" else "Archive")
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Habit?") },
            text = { Text("Are you sure you want to delete '${habit.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
