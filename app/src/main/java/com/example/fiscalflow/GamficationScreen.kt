package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//Colour used on screen 
private val GameDarkBlue = Color(0xFF172A46)

//Background
private val GameLavender = Color(0xFFEDEBFA)

@Composable
fun GamificationScreen(
    modifier: Modifier = Modifier,
    userProgress: UsersProgress
)
{

    // Current XP.
    val xp = userProgress.xp

    // Current streak.
    val streak = userProgress.streak

    // Calculate the current level.
    val level = (xp / 100) + 1

    // Calculate XP inside the current level.
    val levelXp = xp % 100

    // Calculate progress toward the next level.
    val levelProgress = levelXp / 100f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameLavender)
            .padding(20.dp)
    ) {

        Text(
            text = "XP & Milestones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GameDarkBlue
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // XP CARD.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = GameDarkBlue
            )
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Text(
                    text = "⭐ XP",
                    color = Color.White,
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "$xp XP",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Level $level",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "$levelXp / 100 XP to next level",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // STREAK CARD.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "🔥 Current Streak",
                    fontWeight = FontWeight.Bold,
                    color = GameDarkBlue
                )

                Text(
                    text = "$streak",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameDarkBlue
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // MILESTONES TITLE.
        Text(
            text = "🏆 Milestones",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = GameDarkBlue
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (userProgress.milestones.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Text(
                    text = "No milestones unlocked yet.",
                    modifier = Modifier.padding(18.dp),
                    color = Color.DarkGray
                )
            }

        } else {

            userProgress.milestones.forEach { milestone ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Text(
                        text = "✓ $milestone",
                        modifier = Modifier.padding(18.dp),
                        fontWeight = FontWeight.Bold,
                        color = GameDarkBlue
                    )
                }
            }
        }
    }
}