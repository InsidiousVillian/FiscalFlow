package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colours used on screen
private val GameDarkBlue = Color(0xFF172A46)
private val GameLavender = Color(0xFFEDEBFA)
private val AccentGreen = Color(0xFF10B981)
private val GoldYellow = Color(0xFFF59E0B)
private val UnlockedBgColor = Color(0xFFF0FDF4)
private val UnlockedBorderColor = Color(0xFF86EFAC)

data class MilestoneItem(
    val title: String,
    val description: String,
    val icon: String,
    val targetXp: Int = 0,
    val targetStreak: Int = 0
)

private val ALL_MILESTONES = listOf(
    MilestoneItem(
        "First Goal Set",
        description = "Earn 10 XP by setting your first budget goal",
        icon = "🎯",
        targetXp = 10
    ),
    MilestoneItem(
        "Streak Starter",
        description = "Maintain a 1-day active logging streak",
        icon = "🔥",
        targetStreak = 1
    ),
    MilestoneItem(
        "Active Tracker",
        description = "Reach 50 total XP by managing your finances",
        icon = "📊",
        targetXp = 50
    ),
    MilestoneItem(
        "Level 2 Explorer",
        description = "Reach 100 XP to unlock Level 2 status",
        icon = "⚡",
        targetXp = 100
    ),
    MilestoneItem(
        "Streak Master",
        description = "Achieve a 7-day active usage streak",
        icon = "🏆",
        targetStreak = 7
    ),
    MilestoneItem(
        "Fiscal Legend",
        description = "Reach 250 XP to master your financial goals",
        icon = "👑",
        targetXp = 250
    )
)

@Composable
fun GamificationScreen(
    modifier: Modifier = Modifier,
    userProgress: UsersProgress,
    onBackToDashboard: () -> Unit = {}
)
{
    // Current XP
    val xp = userProgress.xp
    // Current streak
    val streak = userProgress.streak
    // Calculate current level
    val level = (xp / 100) + 1
    // Calculate XP inside current level
    val levelXp = xp % 100
    // Calculate progress toward next level
    val levelProgress = levelXp / 100f
    // Determine level rank name
    val rankTitle = when (level) {
        1 -> "Budget Starter"
        2 -> "Smart Saver"
        3 -> "Expense Expert"
        else -> "Financial Master"
    }

    // Determine how many milestones are unlocked
    val unlockedCount = ALL_MILESTONES.count { isMilestoneUnlocked(it, userProgress) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    )
    {
        // Screen Header
        Text(
            "XP & Milestones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GameDarkBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Track your achievements and level up your habits",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // XP LEVEL CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = GameDarkBlue
            )
        )
        {
            Column(
                modifier = Modifier.padding(22.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⭐", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                               "Level $level",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                rankTitle,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        "$xp XP",
                        color = GoldYellow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GoldYellow,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        "$levelXp / 100 XP",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${100 - levelXp} XP to Level ${level + 1}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics row (contains streak and milestone)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            // Streak Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEDD5)),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text("🔥", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            "$streak Day${if (streak == 1) "" else "s"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameDarkBlue
                        )
                        Text(
                            "Streak",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Milestones Unlocked Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text("🏆", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            "$unlockedCount / ${ALL_MILESTONES.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameDarkBlue
                        )
                        Text(
                            "Unlocked",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Milestones section
        Text(
            "🏆 Milestones & Achievements",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GameDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Milestone card list
        ALL_MILESTONES.forEach { milestone ->
            val unlocked = isMilestoneUnlocked(milestone, userProgress)
            val progress = getMilestoneProgress(milestone, userProgress)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .then(
                        if (unlocked) Modifier.border(
                            1.dp,
                            UnlockedBorderColor,
                            RoundedCornerShape(18.dp)
                        )
                        else Modifier
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (unlocked) UnlockedBgColor else Color.White
                )
            )
            {
                Column(
                    modifier = Modifier.padding(16.dp)
                )
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (unlocked) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                if (unlocked) milestone.icon else "🔒",
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Text(
                                    milestone.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GameDarkBlue
                                )

                                if (unlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AccentGreen)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    {
                                        Text(
                                            "✓ UNLOCKED",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                milestone.description,
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    if (!unlocked)
                    {
                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GameDarkBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val progressLabel = when {
                            milestone.targetXp > 0 -> "$xp / ${milestone.targetXp} XP"
                            milestone.targetStreak > 0 -> "$streak / ${milestone.targetStreak} Days"
                            else -> "In Progress"
                        }

                        Text(
                            progressLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Return to dashboard
        Button(
            onClick = onBackToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GameDarkBlue)
        )
        {
            Text(
                "← Back to Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun isMilestoneUnlocked(item: MilestoneItem, userProgress: UsersProgress): Boolean {
    if (userProgress.milestones.any { it.equals(item.title, ignoreCase = true) })
    {
        return true
    }
    val xpMet = item.targetXp > 0 && userProgress.xp >= item.targetXp
    val streakMet = item.targetStreak > 0 && userProgress.streak >= item.targetStreak
    return xpMet || streakMet
}

private fun getMilestoneProgress(item: MilestoneItem, userProgress: UsersProgress): Float {
    if (isMilestoneUnlocked(item, userProgress)) return 1f
    if (item.targetXp > 0)
    {
        return (userProgress.xp.toFloat() / item.targetXp).coerceIn(0f, 1f)
    }
    if (item.targetStreak > 0)
    {
        return (userProgress.streak.toFloat() / item.targetStreak).coerceIn(0f, 1f)
    }
    return 0f
}
