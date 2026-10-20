package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Habit
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.HabitColorOptions
import com.example.ui.theme.PrimaryAccent

data class HabitTemplate(
    val name: String,
    val description: String,
    val category: String,
    val icon: String,
    val color: String,
    val targetVal: Float,
    val targetUnit: String,
    val time: String
)

val STARTER_TEMPLATES = listOf(
    HabitTemplate("Read 10 pages", "Read a non-fiction book or insightful article", "Learning", "book", "#3898EC", 10f, "pages", "21:00"),
    HabitTemplate("Walk 15 minutes", "Get outside for fresh air and light movement", "Physical wellness", "walk", "#8BCF42", 15f, "min", "12:30"),
    HabitTemplate("Stretch 5 minutes", "Gentle mobility stretch to release neck/back tension", "Physical wellness", "fitness", "#20B2AA", 5f, "min", "08:00"),
    HabitTemplate("25 min Deep Focus", "Single-tasking Pomodoro without distractions", "Productivity", "timer", "#27452D", 25f, "min", "10:00"),
    HabitTemplate("Journal 5 minutes", "Brief reflection on wins and gratitude", "Mental wellness", "pencil", "#8860D0", 5f, "min", "21:30"),
    HabitTemplate("Drink water", "Stay hydrated throughout the day", "Physical wellness", "water", "#3898EC", 8f, "glasses", "09:00"),
    HabitTemplate("Screen break", "Step away from all screens to rest eyes", "Lifestyle", "spa", "#E7AA43", 10f, "min", "15:00"),
    HabitTemplate("Bedtime routine", "Wind down with dim lighting and calm music", "Mental wellness", "bed", "#8860D0", 1f, "routine", "22:30")
)

@Composable
fun CreateEditHabitDialog(
    initialHabit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        frequencyType: String,
        weekdays: List<Int>,
        reminderEnabled: Boolean,
        reminderTime: String?,
        targetValue: Float,
        targetUnit: String,
        contributesToStreaks: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "Productivity") }
    var iconName by remember { mutableStateOf(initialHabit?.iconName ?: "check") }
    var colorHex by remember { mutableStateOf(initialHabit?.colorHex ?: "#8BCF42") }
    var frequencyType by remember { mutableStateOf(initialHabit?.frequencyType ?: "DAILY") }
    val selectedWeekdays = remember {
        mutableStateListOf<Int>().apply {
            addAll(initialHabit?.scheduledWeekdays ?: listOf(1, 2, 3, 4, 5, 6, 7))
        }
    }
    var reminderEnabled by remember { mutableStateOf(initialHabit?.reminderEnabled ?: false) }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "08:00") }
    var targetValueStr by remember { mutableStateOf((initialHabit?.targetValue ?: 1f).toInt().toString()) }
    var targetUnit by remember { mutableStateOf(initialHabit?.targetUnit ?: "times") }
    var contributesToStreaks by remember { mutableStateOf(initialHabit?.contributesToStreaks ?: true) }

    val categories = listOf("Productivity", "Physical wellness", "Mental wellness", "Learning", "Lifestyle", "Custom")
    val units = listOf("times", "min", "pages", "glasses", "reps")
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
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
                    Text(
                        text = if (initialHabit == null) "Create Habit" else "Edit Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Starter Templates (if new habit)
                    if (initialHabit == null) {
                        Text("Starter Templates", style = MaterialTheme.typography.labelLarge)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(STARTER_TEMPLATES) { template ->
                                SuggestionChip(
                                    onClick = {
                                        name = template.name
                                        description = template.description
                                        category = template.category
                                        iconName = template.icon
                                        colorHex = template.color
                                        targetValueStr = template.targetVal.toInt().toString()
                                        targetUnit = template.targetUnit
                                        reminderTime = template.time
                                        reminderEnabled = true
                                    },
                                    label = { Text(template.name) }
                                )
                            }
                        }
                    }

                    // Habit Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Habit Name *") },
                        placeholder = { Text("e.g. Read 10 pages") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_form_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Why does this habit matter to you?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )

                    // Category
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace(" wellness", ""), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.takeLast(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace(" wellness", ""), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Icon Selection
                    Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(HabitIcons.availableIconKeys) { key ->
                            val isSelected = iconName == key
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) ForestGreen else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { iconName = key },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = HabitIcons.getIcon(key),
                                    contentDescription = key,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Color Selection
                    Text("Theme Color", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HabitColorOptions.forEach { color ->
                            val hexString = String.format("#%06X", (0xFFFFFF and color.value.toLong().toInt()))
                            val isSelected = colorHex.equals(hexString, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { colorHex = hexString }
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) ForestGreen else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Frequency
                    Text("Frequency", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = frequencyType == "DAILY",
                            onClick = {
                                frequencyType = "DAILY"
                                selectedWeekdays.clear()
                                selectedWeekdays.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                            },
                            label = { Text("Every Day") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = frequencyType == "SPECIFIC_DAYS",
                            onClick = { frequencyType = "SPECIFIC_DAYS" },
                            label = { Text("Specific Days") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (frequencyType == "SPECIFIC_DAYS") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNames.forEachIndexed { index, nameStr ->
                                val dayNum = index + 1
                                val isSelected = selectedWeekdays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ForestGreen else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            if (isSelected) {
                                                if (selectedWeekdays.size > 1) selectedWeekdays.remove(dayNum)
                                            } else {
                                                selectedWeekdays.add(dayNum)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = nameStr,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Target value & unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetValueStr,
                            onValueChange = { targetValueStr = it.filter { char -> char.isDigit() } },
                            label = { Text("Target Goal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = targetUnit,
                            onValueChange = { targetUnit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("min, pages...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Reminder
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = ForestGreen)
                                    Text("Reminder", style = MaterialTheme.typography.titleSmall)
                                }
                                Switch(
                                    checked = reminderEnabled,
                                    onCheckedChange = { reminderEnabled = it }
                                )
                            }

                            if (reminderEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = reminderTime,
                                    onValueChange = { reminderTime = it },
                                    label = { Text("Reminder Time (HH:mm)") },
                                    placeholder = { Text("08:00") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Streak Contribution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Track in Streaks", style = MaterialTheme.typography.titleSmall)
                            Text("Contributes to your streak records and achievements.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = contributesToStreaks,
                            onCheckedChange = { contributesToStreaks = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val targetVal = targetValueStr.toFloatOrNull() ?: 1f
                            onSave(
                                name,
                                description,
                                category,
                                iconName,
                                colorHex,
                                frequencyType,
                                selectedWeekdays.toList(),
                                reminderEnabled,
                                if (reminderEnabled) reminderTime else null,
                                targetVal,
                                targetUnit.ifBlank { "times" },
                                contributesToStreaks
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("habit_form_save_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (initialHabit == null) "Create Habit" else "Save Changes", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
