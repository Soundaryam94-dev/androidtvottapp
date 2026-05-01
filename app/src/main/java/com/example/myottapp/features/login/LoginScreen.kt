package com.example.myottapp.features.login

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

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
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
            Text(
                text = "Sign in to OTT",
                color = OttWhite,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onLoginSuccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OttBrand
                )
            ) {
                Text("Continue", color = OttWhite, fontSize = 18.sp)
            }
        }
    }
}