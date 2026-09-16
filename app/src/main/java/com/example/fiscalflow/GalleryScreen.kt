package com.example.fiscalflow

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Theme colors
private val GalleryDarkBlue = Color(0xFF172A46)
private val GalleryLavender = Color(0xFFEDEBFA)

data class GalleryImageItem(
    val title: String,
    val imageUrl: String,
    val category: String
)

val GALLERY_SAMPLE_IMAGES = listOf(
    GalleryImageItem(
        title = "Transport & Tolls",
        imageUrl = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800",
        category = "Transport"
    ),
    GalleryImageItem(
        title = "Groceries Market",
        imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800",
        category = "Groceries"
    ),
    GalleryImageItem(
        title = "Food & Dining",
        imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800",
        category = "Food"
    ),
    GalleryImageItem(
        title = "Housing & Rent",
        imageUrl = "https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=800",
        category = "Rent"
    ),
    GalleryImageItem(
        title = "Utilities & Energy",
        imageUrl = "https://images.unsplash.com/photo-1508873696983-2df515122519?w=800",
        category = "Utilities"
    ),
    GalleryImageItem(
        title = "Gadgets & Tech",
        imageUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
        category = "Electronics"
    ),
    GalleryImageItem(
        title = "Healthcare & Medical",
        imageUrl = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=800",
        category = "Healthcare"
    ),
    GalleryImageItem(
        title = "Travel & Flights",
        imageUrl = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800",
        category = "Travel"
    ),
    GalleryImageItem(
        title = "Movies & Cinema",
        imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800",
        category = "Entertainment"
    ),
    GalleryImageItem(
        title = "Education & Books",
        imageUrl = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800",
        category = "Education"
    ),
    GalleryImageItem(
        title = "Savings & Investment",
        imageUrl = "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?w=800",
        category = "Savings"
    ),
    GalleryImageItem(
        title = "Clothing & Fashion",
        imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800",
        category = "Shopping"
    )
)

// Gallery Screen allowing user to select a receipt image with title shown at the bottom
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    onImageSelected: (title: String, imageUrl: String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryLavender)
            .padding(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GalleryDarkBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = "Receipt Image Gallery",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GalleryDarkBlue
                )
                Text(
                    text = "Tap an image to attach it to your expense",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(GALLERY_SAMPLE_IMAGES) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onImageSelected(item.title, item.imageUrl)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GalleryDarkBlue)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GalleryDarkBlue)
        ) {
            Text(
                text = "← Return to Form",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
