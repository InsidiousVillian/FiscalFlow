package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Profile screen colour
private val ProfileDarkBlue = Color(0xFF172A46)

// Background colour
private val ProfileLavender = Color(0xFFEDEBFA)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,

    // Name of the user
    username: String,

    // Profile image
    profileImageUri: String?,

    // Return to dashboard
    onBackToDashboard: () -> Unit,

    // Log out
    onLogout: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileLavender)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Title screen
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileDarkBlue
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Show the user's selected image
                if (profileImageUri != null) {

                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                } else {

                    // Placeholder when the user has no image
                    Column(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(ProfileDarkBlue),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = username
                                .take(1)
                                .uppercase(),
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Space between image and username
                Spacer(modifier = Modifier.height(12.dp))

                // USERNAME UNDER PROFILE IMAGE
                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileDarkBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Profile description
                Text(
                    text = "Manage your FiscalFlow account and preferences.",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dashboard button
        Button(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Dashboard")
        }

        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log Out")
        }
    }
}