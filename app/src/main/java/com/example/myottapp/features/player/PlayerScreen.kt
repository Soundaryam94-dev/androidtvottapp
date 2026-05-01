package com.example.myottapp.features.player

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.SideEffect
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────
//  PlayerScreen.kt — Full TV-style video player with controls
//  • ExoPlayer + HLS support
//  • Custom TV controls overlay (Play/Pause, Seek, Time)
//  • D-pad navigation (LEFT=rewind, RIGHT=forward, CENTER=play/pause)
//  • Auto-hide controls after 5 seconds
//  • Progress saving to Room DB
// ─────────────────────────────────────────────────────────────────────

private val AccentOrange = Color(0xFFFF6A00)
private val BgDeep       = Color(0xFF060404)
private val BgOverlay    = Color(0xCC000000)   // 80% black
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFF8A8280)
private val ProgressBg   = Color(0xFF3A3530)

private const val SEEK_INCREMENT_MS    = 10_000L   // 10 seconds
private const val CONTROLS_TIMEOUT_MS  = 5_000L    // auto-hide after 5s
private const val SAVE_INTERVAL_MS     = 30_000L   // save progress every 30s

// ═══════════════════════════════════════════════════════════════════════
//  ENTRY POINT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun PlayerScreen(
    movieId:       Int,
    title:         String  = "",
    playerMode:    String  = "trailer",
    posterPath:    String? = null,
    startPosition: Long    = 0L,
    onBack:        () -> Unit = {},
    onRetry:       () -> Unit = {}
) {
    // Full-screen immersive mode
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, view).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler { onBack() }

    when (playerMode) {
        "movie"  -> MoviePlayerScreen(
            movieId       = movieId,
            title         = title,
            posterPath    = posterPath,
            startPosition = startPosition,
            onBack        = onBack,
            onRetry       = onRetry
        )
        else     -> TrailerPlayerScreen(
            movieId = movieId,
            title   = title,
            onBack  = onBack,
            onRetry = onRetry
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MOVIE PLAYER — ExoPlayer with full TV controls
// ═══════════════════════════════════════════════════════════════════════
@OptIn(UnstableApi::class)
@Composable
fun MoviePlayerScreen(
    movieId:       Int,
    title:         String,
    posterPath:    String?,
    startPosition: Long,
    onBack:        () -> Unit,
    onRetry:       () -> Unit
) {
    val context    = LocalContext.current
    val repository = remember { VideoRepository(context) }
    val scope      = rememberCoroutineScope()
    val streamUrl  = remember(movieId) { repository.getStreamUrl(movieId) }

    // ── Player state ──────────────────────────────────────────────────
    var isPlaying       by remember { mutableStateOf(true) }
    var isBuffering     by remember { mutableStateOf(true) }
    var hasError        by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    var showControls    by remember { mutableStateOf(true) }
    var currentPos      by remember { mutableStateOf(0L) }
    var totalDuration   by remember { mutableStateOf(0L) }
    var controlsTimer   by remember { mutableStateOf(0L) }

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                    if (state == Player.STATE_READY) {
                        totalDuration = duration.coerceAtLeast(0L)
                    }
                    if (state == Player.STATE_ENDED) {
                        isPlaying = false
                        scope.launch {
                            repository.saveWatchProgress(movieId, title, posterPath,
                                duration, duration)
                        }
                    }
                }
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlayerError(error: PlaybackException) {
                    hasError    = true
                    isBuffering = false
                    errorMsg    = when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "Network error. Check connection."
                        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                            "Video not found."
                        else -> "Playback error: ${error.message}"
                    }
                }
            })
            if (startPosition > 0L) seekTo(startPosition)
            prepare()
            playWhenReady = true
        }
    }

    // ── Track position every 500ms ────────────────────────────────────
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(500L)
            currentPos    = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
        }
    }

    // ── Save progress every 30s ───────────────────────────────────────
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(SAVE_INTERVAL_MS)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (dur > 0L && pos > 0L) {
                repository.saveWatchProgress(movieId, title, posterPath, pos, dur)
            }
        }
    }

    // ── Auto-hide controls ────────────────────────────────────────────
    LaunchedEffect(showControls, controlsTimer) {
        if (showControls && isPlaying) {
            delay(CONTROLS_TIMEOUT_MS)
            showControls = false
        }
    }

    // ── Save on exit ──────────────────────────────────────────────────
    DisposableEffect(exoPlayer) {
        onDispose {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (dur > 0L && pos > 0L) {
                scope.launch {
                    repository.saveWatchProgress(movieId, title, posterPath, pos, dur)
                }
            }
            exoPlayer.release()
        }
    }

    // ── Helper: show controls + reset timer ───────────────────────────
    fun revealControls() {
        showControls  = true
        controlsTimer = System.currentTimeMillis()
    }

    // ── Helper: toggle play/pause ─────────────────────────────────────
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        revealControls()
    }

    // ── Helper: seek ──────────────────────────────────────────────────
    fun seekForward()  {
        exoPlayer.seekTo((exoPlayer.currentPosition + SEEK_INCREMENT_MS)
            .coerceAtMost(exoPlayer.duration))
        revealControls()
    }
    fun seekBackward() {
        exoPlayer.seekTo((exoPlayer.currentPosition - SEEK_INCREMENT_MS)
            .coerceAtLeast(0L))
        revealControls()
    }

    when {
        hasError -> PlayerErrorScreen(
            title   = title,
            message = errorMsg,
            onBack  = onBack,
            onRetry = {
                hasError    = false
                isBuffering = true
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        )
        else -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                // ✅ D-pad key handling at top level
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.DirectionCenter, Key.Enter -> {
                                if (showControls) togglePlayPause()
                                else revealControls()
                                true
                            }
                            Key.DirectionRight -> { seekForward();  true }
                            Key.DirectionLeft  -> { seekBackward(); true }
                            Key.DirectionUp, Key.DirectionDown -> {
                                revealControls(); false  // let focus move
                            }
                            else -> { revealControls(); false }
                        }
                    } else false
                }
                .focusable()
        ) {
            // ── ExoPlayer surface ─────────────────────────────────────
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player                   = exoPlayer
                        // ✅ Disable built-in controller — we use custom
                        useController            = false
                        resizeMode               = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        layoutParams             = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ── Buffering overlay ─────────────────────────────────────
            if (isBuffering) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TvBufferingSpinner()
                        Text(title, color = Color.White, fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                            maxLines = 1)
                        Text("Loading...", color = TextMid, fontSize = 12.sp)
                    }
                }
            }

            // ── Custom TV controls overlay ────────────────────────────
            AnimatedVisibility(
                visible  = showControls && !isBuffering,
                enter    = fadeIn(tween(250)),
                exit     = fadeOut(tween(400))
            ) {
                TvPlayerControls(
                    title         = title,
                    isPlaying     = isPlaying,
                    currentPos    = currentPos,
                    totalDuration = totalDuration,
                    onBack        = onBack,
                    onPlayPause   = { togglePlayPause() },
                    onSeekForward = { seekForward() },
                    onSeekBack    = { seekBackward() },
                    onSeek        = { fraction ->
                        exoPlayer.seekTo((fraction * totalDuration).toLong())
                        revealControls()
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TV PLAYER CONTROLS OVERLAY
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TvPlayerControls(
    title:         String,
    isPlaying:     Boolean,
    currentPos:    Long,
    totalDuration: Long,
    onBack:        () -> Unit,
    onPlayPause:   () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack:    () -> Unit,
    onSeek:        (Float) -> Unit
) {
    val progress = if (totalDuration > 0L)
        (currentPos.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    else 0f

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Top bar — back button + title ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(Brush.verticalGradient(
                    listOf(BgOverlay, Color.Transparent)))
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back button
            Box(modifier = Modifier
                .size(40.dp).clip(CircleShape)
                .background(Color.White.copy(0.12f))
                .clickable { onBack() },
                contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, maxLines = 1)
                Text("Now Playing", fontSize = 11.sp, color = TextMid)
            }
        }

        // ── Bottom controls — progress + buttons ──────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, BgOverlay)))
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Progress row: time + bar + total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Current time
                Text(
                    text       = formatMs(currentPos),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    modifier   = Modifier.width(52.dp)
                )

                // ✅ Seek bar
                TvSeekBar(
                    progress  = progress,
                    modifier  = Modifier.weight(1f)
                )

                // Total duration
                Text(
                    text     = formatMs(totalDuration),
                    fontSize = 13.sp,
                    color    = TextMid,
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.End
                )
            }

            // Playback buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // ⏪ Rewind 10s
                TvControlBtn(
                    icon        = Icons.Default.FastRewind,
                    label       = "-10s",
                    onClick     = onSeekBack,
                    isLarge     = false
                )
                Spacer(Modifier.width(24.dp))

                // ▶/⏸ Play Pause — large center button
                TvControlBtn(
                    icon    = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    label   = if (isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    isLarge = true
                )
                Spacer(Modifier.width(24.dp))

                // ⏩ Forward 10s
                TvControlBtn(
                    icon    = Icons.Default.FastForward,
                    label   = "+10s",
                    onClick = onSeekForward,
                    isLarge = false
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TV SEEK BAR — custom progress bar
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TvSeekBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(6.dp).clip(RoundedCornerShape(3.dp))
            .background(ProgressBg)
    ) {
        // Filled portion
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(Brush.horizontalGradient(
                    listOf(AccentOrange, Color(0xFFFF8C30))
                ))
        )
        // Scrubber dot
        if (progress > 0f) {
            Box(modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (progress * 1f).coerceIn(0f, 1f).let {
                    // position roughly — simplified without full measurement
                    0.dp
                })
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TV CONTROL BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TvControlBtn(
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    label:   String,
    onClick: () -> Unit,
    isLarge: Boolean = false
) {
    val size   = if (isLarge) 64.dp else 48.dp
    val iconSz = if (isLarge) 32.dp else 22.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    if (isLarge) AccentOrange
                    else Color.White.copy(0.15f)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(iconSz))
        }
        Text(label, fontSize = 10.sp, color = TextMid)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TRAILER PLAYER — YouTube WebView (unchanged)
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TrailerPlayerScreen(
    movieId: Int,
    title:   String,
    onBack:  () -> Unit,
    onRetry: () -> Unit
) {
    val context    = LocalContext.current
    val repository = remember { VideoRepository(context) }
    var trailerKey by remember { mutableStateOf("") }
    var isLoading  by remember { mutableStateOf(true) }
    var hasError   by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("Trailer not available") }

    LaunchedEffect(movieId) {
        isLoading = true; hasError = false; trailerKey = ""
        val key = repository.getTrailerKey(movieId).getOrNull()
        if (!key.isNullOrEmpty()) { trailerKey = key; isLoading = false }
        else { errorMsg = "No trailer found"; hasError = true; isLoading = false }
    }

    when {
        isLoading -> PlayerLoadingScreen(title = title, mode = "trailer")
        hasError  -> PlayerErrorScreen(title = title, message = errorMsg,
            onBack = onBack, onRetry = onRetry)
        else      -> YouTubeWebPlayer(trailerKey = trailerKey, title = title, onBack = onBack)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  YOUTUBE WEB PLAYER
// ═══════════════════════════════════════════════════════════════════════
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWebPlayer(trailerKey: String, title: String = "", onBack: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                    settings.apply {
                        javaScriptEnabled                = true
                        domStorageEnabled                = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode             = true
                        useWideViewPort                  = true
                        builtInZoomControls              = false
                        cacheMode                        = WebSettings.LOAD_NO_CACHE
                        mixedContentMode                 = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString                  = "Mozilla/5.0 (Linux; Android 14; TV)"
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient   = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView, request: WebResourceRequest
                        ): Boolean = false
                    }
                    val html = """<!DOCTYPE html><html><head>
                        <meta name="viewport" content="width=device-width,initial-scale=1">
                        <style>*{margin:0;padding:0;background:#000}html,body{width:100%;height:100%;overflow:hidden}
                        iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:none}
                        </style></head><body>
                        <iframe src="https://www.youtube-nocookie.com/embed/$trailerKey?autoplay=1&controls=1&rel=0&fs=1"
                        allow="autoplay;fullscreen;encrypted-media" allowfullscreen></iframe>
                        </body></html>""".trimIndent()
                    loadDataWithBaseURL("https://www.youtube-nocookie.com",
                        html, "text/html", "utf-8", null)
                }
            })
        PlayerBackButton(modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            onClick = onBack)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun PlayerBackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.size(44.dp).clip(RoundedCornerShape(22.dp))
        .background(Color.Black.copy(0.6f)).focusable().clickable { onClick() },
        contentAlignment = Alignment.Center) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
            tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun PlayerLoadingScreen(title: String, mode: String = "trailer") {
    Box(
        modifier         = Modifier.fillMaxSize().background(BgDeep).padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CircularProgressIndicator(color = AccentOrange,
                modifier = Modifier.size(52.dp), strokeWidth = 3.dp)
            if (title.isNotEmpty()) Text(title, color = Color.White,
                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            Text(if (mode == "movie") "Loading movie..." else "Loading trailer...",
                color = TextMid, fontSize = 13.sp)
        }
    }
}

@Composable
fun PlayerErrorScreen(
    title: String, message: String,
    onBack: () -> Unit, onRetry: () -> Unit
) {
    Box(
        modifier         = Modifier.fillMaxSize().background(BgDeep).padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 48.dp)) {
            Text("📽️", fontSize = 48.sp)
            if (title.isNotEmpty()) Text(title, color = Color.White,
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            Text(message, color = TextMid, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(AccentOrange).focusable().clickable { onRetry() }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White,
                            modifier = Modifier.size(18.dp))
                        Text("Retry", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(0.10f)).focusable().clickable { onBack() }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                            tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Go Back", color = Color.White,
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NoVideoScreen(title: String, onBack: () -> Unit) {
    PlayerErrorScreen(title = title, message = "No video available",
        onBack = onBack, onRetry = onBack)
}

@Composable
fun TvBufferingSpinner() {
    val transition = rememberInfiniteTransition(label = "buf")
    val rotation by transition.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "rot"
    )
    Canvas(modifier = Modifier.size(52.dp)) {
        drawArc(color = AccentOrange, startAngle = rotation, sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
    }
}

// Keep old name for backward compat
@Composable
fun BufferingSpinner() = TvBufferingSpinner()

// ═══════════════════════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════════════════════
private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val hours   = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0)
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}
