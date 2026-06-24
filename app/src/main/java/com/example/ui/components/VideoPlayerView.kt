package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    streamUrl: String,
    useCustomUserAgent: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recreate the player if the User Agent toggle changes
    val exoPlayer = remember(useCustomUserAgent) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Set source
    LaunchedEffect(streamUrl, useCustomUserAgent) {
        val userAgent = if (useCustomUserAgent) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            "ExoPlayer"
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)

        val isHls = streamUrl.contains(".m3u8") || streamUrl.contains("m3u8")

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .apply {
                if (isHls) {
                    setMimeType(MimeTypes.APPLICATION_M3U8)
                }
            }
            .build()

        val mediaSource = if (isHls) {
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        } else {
            null
        }

        if (mediaSource != null) {
            exoPlayer.setMediaSource(mediaSource)
        } else {
            exoPlayer.setMediaItem(mediaItem)
        }

        exoPlayer.prepare()
    }

    // Lifecycle events
    DisposableEffect(exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        var playerError by remember { mutableStateOf<String?>(null) }
        var isBuffering by remember { mutableStateOf(false) }

        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    playerError = error.localizedMessage ?: "Error playing this channel"
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY) {
                        playerError = null
                    }
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                exoPlayer.removeListener(listener)
            }
        }

        if (playerError != null) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "خطا در پخش استریم",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playerError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
