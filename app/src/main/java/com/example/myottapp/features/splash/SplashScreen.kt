package com.example.myottapp.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myottapp.core.ui.theme.OttBlack
import com.example.myottapp.core.ui.theme.OttBrand
import com.example.myottapp.core.ui.theme.OttWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onNavigateToHome()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OttBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "▶  OTT",
                color = OttBrand,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Entertainment Hub",
                color = OttWhite,
                fontSize = 20.sp
            )
        }
    }
}