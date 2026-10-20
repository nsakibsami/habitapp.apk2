package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Achievement
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.ui.MainViewModel
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.WarningAmber

@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    achievements: List<Achievement>
) {
    var reportMode by remember { mutableIntStateOf(0) } // 0 = Weekly, 1 = Monthly

    val weeklyChartData = remember(habits, completions) {
        viewModel.getWeeklyChartData(habits, completions)
    }

    val categoryStats = remember(habits, completions) {
        viewModel.getCategoryBreakdown(habits, completions)
    }

    val heatmapDays = remember(completions) {
        viewModel.get30DayHeatmap(completions)
    }

    val totalCompletedThisWeek = weeklyChartData.sumOf { it.completedCount }
    val totalScheduledThisWeek = weeklyChartData.sumOf { it.totalScheduled }
    val weeklyRatePct = if (totalScheduledThisWeek == 0) 0 else ((totalCompletedThisWeek.toFloat() / totalScheduledThisWeek) * 100).toInt()

    val bestDay = weeklyChartData.maxByOrNull { it.completedCount }?.dayName ?: "N/A"

    // Find most consistent habit
    val mostConsistentHabit = remember(habits, completions) {
        habits.maxByOrNull { habit ->
            val hComps = completions.filter { it.habitId == habit.id }
            viewModel.repository.calculateHabitCurrentStreak(habit, hComps)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Screen Title
        item {
            Text(
                text = "Insights & Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Track your consistency and rhythm, without guilt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Toggle: Weekly Report vs Monthly Report
        item {
            TabRow(
                selectedTabIndex = reportMode,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = reportMode == 0,
                    onClick = { reportMode = 0 },
                    text = { Text("Weekly Report") }
                )
                Tab(
                    selected = reportMode == 1,
                    onClick = { reportMode = 1 },
                    text = { Text("Monthly Overview") }
                )
            }
        }

        // Highlight Metric Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (reportMode == 0) "7-Day Completion Rate" else "30-Day Completion Rate",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$weeklyRatePct%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryAccent.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Text("Steady", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Best Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bestDay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Done This Week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalCompletedThisWeek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Top Habit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(mostConsistentHabit?.name?.take(12) ?: "None yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Weekly Bar Chart
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Daily Activity (Past 7 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxTarget = weeklyChartData.maxOfOrNull { it.totalScheduled }?.coerceAtLeast(1) ?: 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyChartData.forEach { point ->
                            val heightFraction = (point.completedCount.toFloat() / maxTarget).coerceIn(0.08f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${point.completedCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (point.completedCount > 0) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (point.completedCount > 0) ForestGreen else MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = point.dayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 30-Day Activity Heatmap
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("30-Day Activity Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(PrimaryAccent.copy(alpha = 0.4f)))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(PrimaryAccent))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(ForestGreen))
                            Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 weeks of 7 days grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val index = row * 7 + col
                                    if (index < heatmapDays.size) {
                                        val day = heatmapDays[index]
                                        val bg = when (day.level) {
                                            0 -> MaterialTheme.colorScheme.surfaceVariant
                                            1 -> PrimaryAccent.copy(alpha = 0.4f)
                                            2 -> PrimaryAccent
                                            else -> ForestGreen
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${day.dayOfMonth}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 11.sp,
                                                color = if (day.level >= 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
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

        // Category Breakdown
        if (categoryStats.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Category Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))

                        categoryStats.forEach { stat ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stat.category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${stat.percentage}% (${stat.count})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { stat.percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = try {
                                        Color(android.graphics.Color.parseColor(stat.colorHex))
                                    } catch (_: Exception) {
                                        ForestGreen
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Milestones & Achievements Shelf
        item {
            Text("Milestone Badges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val allMilestones = listOf(
            Triple("FIRST_COMPLETION", "First Step Taken", "Completed your first habit"),
            Triple("STREAK_3", "3-Day Streak", "Kept momentum alive for 3 scheduled days"),
            Triple("STREAK_7", "One Week Strong", "7-day streak achieved"),
            Triple("STREAK_14", "14-Day Consistency", "2 weeks of burnout-free habits"),
            Triple("STREAK_30", "30-Day Master", "One full month of dedication"),
            Triple("COMPLETIONS_100", "Century 100", "100 total habit completions")
        )

        val unlockedTypes = achievements.map { it.type }.toSet()

        items(allMilestones) { (type, title, desc) ->
            val isUnlocked = unlockedTypes.contains(type)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUnlocked) PrimaryAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) ForestGreen else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isUnlocked) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Unlocked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
