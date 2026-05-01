package com.example.myottapp.features.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.MovieDto

private val BgCard  = Color(0xFF1F1F1F)
private val Accent  = Color(0xFFFF6A00)
private val Gold    = Color(0xFFFFCC00)

// ═══════════════════════════════════════════════════════════════════════
//  MOVIE CARD — used by MovieRow.kt
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MovieCard(
    movie:          MovieDto,
    onClick:        () -> Unit,
    focusRequester: FocusRequester? = null,
    width:          Dp              = 150.dp,
    height:         Dp              = 215.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "card_${movie.id}"
    )
    val ctx  = LocalContext.current
    val year = movie.release_date.take(4)

    Box(
        modifier = Modifier
            .width(width).height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(
                elevation    = if (isFocused) 22.dp else 2.dp,
                shape        = RoundedCornerShape(10.dp),
                ambientColor = if (isFocused) Accent.copy(0.55f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.90f) else Color.Transparent
            )
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Accent else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .background(BgCard)
            .then(
                if (focusRequester != null)
                    Modifier.focusRequester(focusRequester)
                else Modifier
            )
            .focusable(interactionSource = interaction)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onClick
            )
            .onKeyEvent { e ->
                when {
                    e.type == KeyEventType.KeyDown &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> true
                    e.type == KeyEventType.KeyUp &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            }
    ) {
        if (!movie.poster_path.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(TmdbApiService.posterUrl(movie.poster_path))
                    .crossfade(true).build(),
                contentDescription = movie.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
                    .focusProperties { canFocus = false }
            )
        } else {
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF2A1F0F), BgCard))),
                contentAlignment = Alignment.Center
            ) {
                Text(movie.title.take(2).uppercase(),
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Black,
                    color      = Accent.copy(0.4f))
            }
        }

        Box(Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f    to Color.Transparent,
                0.55f to Color.Transparent,
                1f    to Color.Black.copy(0.93f)))
            .focusProperties { canFocus = false })

        if (isFocused) {
            Box(Modifier.fillMaxWidth().height(3.dp)
                .align(Alignment.TopCenter).background(Accent))
        }

        if (isFocused) {
            Box(
                modifier = Modifier.size(32.dp).align(Alignment.Center)
                    .shadow(10.dp, CircleShape, ambientColor = Accent.copy(0.5f))
                    .clip(CircleShape).background(Accent.copy(0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        if (movie.vote_average > 0) {
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.Star, null,
                    tint = Gold, modifier = Modifier.size(9.dp))
                Text(String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
        }

        if (year.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                .clip(RoundedCornerShape(4.dp)).background(Accent.copy(0.85f))
                .padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(year, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    color = Color.White)
            }
        }

        Text(
            text     = movie.title,
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color    = Color.White, maxLines = 2,
            overflow = TextOverflow.Ellipsis, lineHeight = 13.sp,
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}