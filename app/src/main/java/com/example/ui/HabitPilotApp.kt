package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.ui.components.CreateEditHabitDialog
import com.example.ui.components.HabitDetailSheet
import com.example.ui.components.PaywallSheet
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.TermsOfServiceDialog
import com.example.ui.screens.AICoachScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GoalSelectionScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.PersonalizationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PrimaryAccent

enum class AppScreen {
    WELCOME,
    AUTH,
    GOAL_SELECTION,
    PERSONALIZATION,
    MAIN
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HABITS("Habits", Icons.Default.Checklist),
    INSIGHTS("Insights", Icons.Default.BarChart),
    COACH("AI Coach", Icons.Default.Spa),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun HabitPilotApp(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeHabits by viewModel.activeHabits.collectAsStateWithLifecycle()
    val archivedHabits by viewModel.archivedHabits.collectAsStateWithLifecycle()
    val allCompletions by viewModel.allCompletions.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val coachMessages by viewModel.coachMessages.collectAsStateWithLifecycle()
    val isCoachThinking by viewModel.isCoachThinking.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Sync initial screen with onboarding state
    LaunchedEffect(profile?.isOnboarded) {
        if (profile?.isOnboarded == true) {
            currentScreen = AppScreen.MAIN
        } else if (currentScreen == AppScreen.MAIN) {
            currentScreen = AppScreen.WELCOME
        }
    }

    // Modal dialogs state
    var showPaywall by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showCreateHabitDialog by remember { mutableStateOf(false) }
    var habitDetailToShow by remember { mutableStateOf<Habit?>(null) }
    var habitToEditFromDetail by remember { mutableStateOf<Habit?>(null) }

    // Temporary onboarding state
    var onboardingGoals by remember { mutableStateOf<List<String>>(emptyList()) }
    var authEmail by remember { mutableStateOf("alex@habitpilot.app") }
    var authName by remember { mutableStateOf("Alex") }

    when (currentScreen) {
        AppScreen.WELCOME -> {
            WelcomeScreen(
                onGetStarted = { currentScreen = AppScreen.GOAL_SELECTION },
                onSignIn = { currentScreen = AppScreen.AUTH },
                onShowTerms = { showTerms = true },
                onShowPrivacy = { showPrivacy = true }
            )
        }

        AppScreen.AUTH -> {
            BackHandler { currentScreen = AppScreen.WELCOME }
            AuthScreen(
                onAuthSuccess = { email, name ->
                    authEmail = email
                    authName = name
                    viewModel.completeOnboarding(
                        displayName = name,
                        email = email,
                        goals = listOf("Improve productivity", "Build a morning routine"),
                        wakeTime = "07:00",
                        workSchedule = "09:00 - 17:00",
                        reminderHours = "Morning & Evening",
                        coachingTone = "Encouraging",
                        seedStarterHabits = true
                    )
                    currentScreen = AppScreen.MAIN
                },
                onBack = { currentScreen = AppScreen.WELCOME }
            )
        }

        AppScreen.GOAL_SELECTION -> {
            BackHandler { currentScreen = AppScreen.WELCOME }
            GoalSelectionScreen(
                onGoalsSelected = { goals ->
                    onboardingGoals = goals
                    currentScreen = AppScreen.PERSONALIZATION
                }
            )
        }

        AppScreen.PERSONALIZATION -> {
            BackHandler { currentScreen = AppScreen.GOAL_SELECTION }
            PersonalizationScreen(
                onComplete = { wakeTime, workSchedule, reminderHours, coachingTone, seedStarter ->
                    viewModel.completeOnboarding(
                        displayName = authName,
                        email = authEmail,
                        goals = onboardingGoals,
                        wakeTime = wakeTime,
                        workSchedule = workSchedule,
                        reminderHours = reminderHours,
                        coachingTone = coachingTone,
                        seedStarterHabits = seedStarter
                    )
                    currentScreen = AppScreen.MAIN
                }
            )
        }

        AppScreen.MAIN -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ForestGreen,
                                    selectedTextColor = ForestGreen,
                                    indicatorColor = PrimaryAccent.copy(alpha = 0.35f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        NavigationTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                profile = profile,
                                habits = activeHabits,
                                completions = allCompletions,
                                onOpenHabitDetail = { habit -> habitDetailToShow = habit },
                                onCreateHabit = { showCreateHabitDialog = true },
                                onOpenPaywall = { showPaywall = true }
                            )
                        }

                        NavigationTab.HABITS -> {
                            HabitsScreen(
                                viewModel = viewModel,
                                profile = profile,
                                activeHabits = activeHabits,
                                archivedHabits = archivedHabits,
                                completions = allCompletions,
                                onOpenPaywall = { showPaywall = true }
                            )
                        }

                        NavigationTab.INSIGHTS -> {
                            InsightsScreen(
                                viewModel = viewModel,
                                habits = activeHabits,
                                completions = allCompletions,
                                achievements = achievements
                            )
                        }

                        NavigationTab.COACH -> {
                            AICoachScreen(
                                viewModel = viewModel,
                                profile = profile,
                                messages = coachMessages,
                                isThinking = isCoachThinking
                            )
                        }

                        NavigationTab.PROFILE -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                profile = profile,
                                habits = activeHabits,
                                onOpenPaywall = { showPaywall = true },
                                onSignOut = {
                                    currentScreen = AppScreen.WELCOME
                                },
                                onShowTerms = { showTerms = true },
                                onShowPrivacy = { showPrivacy = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Paywall Dialog
    if (showPaywall) {
        PaywallSheet(
            onDismiss = { showPaywall = false },
            onSubscribe = { plan ->
                viewModel.upgradeToPro(plan)
                showPaywall = false
            },
            onShowTerms = {
                showPaywall = false
                showTerms = true
            },
            onShowPrivacy = {
                showPaywall = false
                showPrivacy = true
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacy = false })
    }

    // Terms of Service Dialog
    if (showTerms) {
        TermsOfServiceDialog(onDismiss = { showTerms = false })
    }

    // Create Habit Dialog from Home/FAB
    if (showCreateHabitDialog) {
        CreateEditHabitDialog(
            initialHabit = null,
            onDismiss = { showCreateHabitDialog = false },
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
                showCreateHabitDialog = false
            }
        )
    }

    // Habit Detail Sheet
    habitDetailToShow?.let { habit ->
        HabitDetailSheet(
            habit = habit,
            viewModel = viewModel,
            completions = allCompletions,
            onDismiss = { habitDetailToShow = null },
            onEdit = {
                val h = habitDetailToShow
                habitDetailToShow = null
                habitToEditFromDetail = h
            },
            onArchiveToggle = {
                if (habit.isArchived) viewModel.restoreHabit(habit.id)
                else viewModel.archiveHabit(habit.id)
                habitDetailToShow = null
            },
            onDelete = {
                viewModel.deleteHabit(habit)
                habitDetailToShow = null
            }
        )
    }

    // Edit Habit Dialog opened from detail
    habitToEditFromDetail?.let { habit ->
        CreateEditHabitDialog(
            initialHabit = habit,
            onDismiss = { habitToEditFromDetail = null },
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
                habitToEditFromDetail = null
            }
        )
    }
}
