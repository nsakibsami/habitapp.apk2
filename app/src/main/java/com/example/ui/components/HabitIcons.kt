package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val iconMap: Map<String, ImageVector> = mapOf(
        "water" to Icons.Default.LocalDrink,
        "book" to Icons.Default.Book,
        "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "fitness" to Icons.Default.FitnessCenter,
        "timer" to Icons.Default.Timer,
        "meditation" to Icons.Default.SelfImprovement,
        "bed" to Icons.Default.Bedtime,
        "sun" to Icons.Default.WbSunny,
        "star" to Icons.Default.Star,
        "spa" to Icons.Default.Spa,
        "pencil" to Icons.Default.Edit,
        "check" to Icons.Default.CheckCircle
    )

    fun getIcon(name: String): ImageVector {
        return iconMap[name] ?: Icons.Default.CheckCircle
    }

    val availableIconKeys = listOf(
        "water", "book", "walk", "fitness", "timer", "meditation", "bed", "sun", "star", "spa", "pencil"
    )
}
