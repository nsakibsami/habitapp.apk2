package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ForestGreen

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
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
                Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Last Updated: October 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(
                        "HabitPilot ('we', 'us', or 'our') values your personal privacy. This Privacy Policy describes how we collect, use, and protect your information.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("1. Information We Store", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "We store your habit names, categories, schedules, completion records, streak histories, and AI coaching preferences locally on your device in a secure SQLite database.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("2. AI Habit Coaching Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "When using our AI Coach, your messages and high-level habit context are processed securely. We do not sell your personal data or habit history to third-party advertisers.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("3. Notifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Scheduled habit reminders are managed using on-device alarms. You can disable reminders or configure Quiet Hours in Settings at any time.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("4. Data Retention & Deletion", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "You maintain full ownership of your data. You may export your complete habit data or permanently erase your account at any time via the Settings menu.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
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
                Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Last Updated: October 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(
                        "Welcome to HabitPilot. By using our application, you agree to these Terms of Service.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("1. Wellness & Productivity Disclaimer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "HabitPilot and its AI Habit Coach are designed solely for personal productivity and habit consistency. They do not constitute medical, psychiatric, or healthcare advice. Consult qualified professionals for health conditions.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("2. Subscriptions & Billing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "HabitPilot Pro subscriptions are billed monthly or annually as selected. Subscriptions renew automatically unless canceled before the end of the current billing cycle.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("3. Acceptable Use", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "You agree not to use the application or AI coach to generate harmful, abusive, or unlawful content.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Agree")
                }
            }
        }
    }
}
