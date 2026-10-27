package com.example.fiscalflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.fiscalflow.ui.theme.FiscalFlowPurple

private val WelcomeBlue = Color(0xFF1E3A8A)
private val WelcomeMuted = Color(0xFF4B5563)
private val WelcomeSoft = Color(0xFF6B7280)

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onGetStarted: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEEEEF2),
                        Color(0xFFE6E8F2),
                        Color(0xFFD5DAEE)
                    )
                )
            )
    ) {
        FiscalFlowBubbles()

        ScrollableScreen(
            modifier = Modifier.fillMaxSize(),
            fillViewport = true,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(36.dp))

                FiscalFlowLogo()

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Welcome To Fiscal Flow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelcomeBlue,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Plan. Track. Achieve",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WelcomeBlue
                )
            }

            Text(
                text = "Take control of your money and\nreach your goals",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WelcomeMuted,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = WelcomeBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "GET STARTED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Already have an account? ",
                        color = WelcomeSoft,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log in",
                        color = Color(0xFF111827),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FiscalFlowLogo() {
    Image(
        painter = painterResource(id = R.drawable.ic_fiscalflow_logo),
        contentDescription = "Fiscal Flow Logo",
        modifier = Modifier.size(108.dp)
    )
}

