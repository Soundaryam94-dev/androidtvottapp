package com.example.myottapp.features.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myottapp.core.ui.theme.*
import com.example.myottapp.core.ui.theme.OttBrand
import com.example.myottapp.core.ui.theme.OttWhite
@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("⚠️", fontSize = 80.sp)
            Text(
                "Something went wrong",
                color = OttWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OttBrand
                )
            ) {
                Text("Try Again", color = OttWhite)
            }
        }
    }
}