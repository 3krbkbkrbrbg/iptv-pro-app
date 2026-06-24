package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Channel
import com.example.data.PlaylistEntity
import com.example.ui.components.VideoPlayerView
import kotlinx.coroutines.launch

// Custom Dark Theme Palette inspired by Premium YouTube Dark Mode
val DarkBg = Color(0xFF0F0F0F)
val SurfaceDark = Color(0xFF1F1F1F)
val SurfaceAccent = Color(0xFF2B2B2B)
val YouTubeRed = Color(0xFFE50914)
val SecondaryText = Color(0xAAA0A0A0)

@Composable
fun IptvApp(viewModel: IptvViewModel) {
    var currentTab by remember { mutableStateOf("explore") }
    val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
    val useCustomUserAgent by viewModel.useCustomUserAgent.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        bottomBar = {
            if (!isLandscape) {
                BottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isLandscape) 0.dp else innerPadding.calculateBottomPadding())
        ) {
            // Main content column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // If there's an active channel, we show the Player at the top
                if (activeChannel != null) {
                    val active = activeChannel!!
                    if (isLandscape) {
                        // Landscape view is purely the player
                        VideoPlayerView(
                            streamUrl = active.streamUrl,
                            useCustomUserAgent = useCustomUserAgent,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Portrait View displays player, info, and remaining channels below
                        val isFav by viewModel.isFavorite(active.streamUrl).collectAsStateWithLifecycle(initialValue = false)
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            VideoPlayerView(
                                streamUrl = active.streamUrl,
                                useCustomUserAgent = useCustomUserAgent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                            
                            // Watch Details (YouTube-style details below video)
                            WatchDetailsSection(
                                active = active,
                                isFav = isFav,
                                onFavToggle = { viewModel.toggleFavorite(active) },
                                useCustomUserAgent = useCustomUserAgent,
                                onToggleUserAgent = { viewModel.toggleUserAgent() },
                                onClosePlayer = { viewModel.selectChannel(activeChannel!!) /* Toggle/Hide could be here, but let's allow just closing active stream by making it null */ }
                            )

                            Divider(color = SurfaceAccent, thickness = 1.dp)

                            // Queue/Related section title
                            Text(
                                text = "کانال‌های مرتبط در این لیست",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            // Display current list of channels as "Next videos"
                            val relatedChannels by viewModel.filteredChannels.collectAsStateWithLifecycle()
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(relatedChannels) { channel ->
                                    ChannelItemRow(
                                        channel = channel,
                                        isActive = channel.streamUrl == active.streamUrl,
                                        onClick = { viewModel.selectChannel(channel) },
                                        onFavClick = { viewModel.toggleFavorite(channel) },
                                        isFav = false // Handled internally or can be queried, but simpler in rows
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // No active channel playing, show the standard tab view
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppBar(onClosePlayer = { })
                        
                        when (currentTab) {
                            "explore" -> ExploreScreen(viewModel)
                            "playlists" -> PlaylistsScreen(viewModel)
                            "favorites" -> FavoritesScreen(viewModel)
                            "history" -> HistoryScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(onClosePlayer: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(YouTubeRed, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "IPTV Pro",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        IconButton(onClick = onClosePlayer) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "اطلاعات",
                tint = Color.White
            )
        }
    }
}

@Composable
fun WatchDetailsSection(
    active: Channel,
    isFav: Boolean,
    onFavToggle: () -> Unit,
    useCustomUserAgent: Boolean,
    onToggleUserAgent: () -> Unit,
    onClosePlayer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = active.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = active.groupName,
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Favorite, User-Agent proxying, close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite Button
            Button(
                onClick = onFavToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFav) YouTubeRed else SurfaceAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isFav) "نشان شده" else "افزودن به نشان‌ها", fontSize = 12.sp)
            }

            // User-Agent toggle button (Smart Proxy)
            Button(
                onClick = onToggleUserAgent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (useCustomUserAgent) Color(0xFF4F46E5) else SurfaceAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (useCustomUserAgent) "پروکسی هوشمند: روشن" else "پروکسی: خاموش",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == "explore",
            onClick = { onTabSelected("explore") },
            label = { Text("کاووش", fontSize = 11.sp) },
            icon = {
                Icon(
                    imageVector = if (currentTab == "explore") Icons.Filled.Explore else Icons.Outlined.Explore,
                    contentDescription = "کاووش"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = YouTubeRed,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentTab == "playlists",
            onClick = { onTabSelected("playlists") },
            label = { Text("لیست‌های من", fontSize = 11.sp) },
            icon = {
                Icon(
                    imageVector = if (currentTab == "playlists") Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                    contentDescription = "لیست‌ها"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = YouTubeRed,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentTab == "favorites",
            onClick = { onTabSelected("favorites") },
            label = { Text("نشان‌شده‌ها", fontSize = 11.sp) },
            icon = {
                Icon(
                    imageVector = if (currentTab == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "نشان‌شده‌ها"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = YouTubeRed,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentTab == "history",
            onClick = { onTabSelected("history") },
            label = { Text("تاریخچه", fontSize = 11.sp) },
            icon = {
                Icon(
                    imageVector = if (currentTab == "history") Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "تاریخچه"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = YouTubeRed,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
    }
}

// EXPLORE / HOME SCREEN
@Composable
fun ExploreScreen(viewModel: IptvViewModel) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredChannels by viewModel.filteredChannels.collectAsStateWithLifecycle()
    val groups by viewModel.channelGroups.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()

    val presets = remember {
        listOf(
            PresetCategory("کانال‌های فارسی", "https://iptv-org.github.io/iptv/languages/fas.m3u", Icons.Filled.Language),
            PresetCategory("ورزشی", "https://iptv-org.github.io/iptv/categories/sports.m3u", Icons.Filled.SportsSoccer),
            PresetCategory("موزیک", "https://iptv-org.github.io/iptv/categories/music.m3u", Icons.Filled.MusicNote),
            PresetCategory("فیلم و سریال", "https://iptv-org.github.io/iptv/categories/movies.m3u", Icons.Filled.Movie),
            PresetCategory("اخبار جهان", "https://iptv-org.github.io/iptv/categories/news.m3u", Icons.Filled.Newspaper),
            PresetCategory("تلویزیون آمریکا", "https://iptv-org.github.io/iptv/countries/us.m3u", Icons.Filled.Public)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner/Featured Section (Glassmorphic look)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(YouTubeRed, Color(0xFF8B0000), SurfaceDark)
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تلویزیون آنلاین پیشرفته",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پخش روان هزاران شبکه زنده جهان و شبکه‌های فارسی بدون محدودیت",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Horizontal Preset Categories (Like YouTube Home Chips)
        Text(
            text = "دسته‌بندی‌های پیشنهادی",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                PresetChip(
                    preset = preset,
                    onClick = { viewModel.loadPlaylistFromUrl(preset.title, preset.url) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("جستجو در بین شبکه‌ها...", color = SecondaryText, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SecondaryText) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = SecondaryText)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        // Subgroups Chips row (Dynamic from parsed list)
        if (groups.size > 1 && !isLoading) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups) { group ->
                    val isSelected = group == selectedGroup
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color.White else SurfaceDark)
                            .clickable { viewModel.setSelectedGroup(group) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = group,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // List / Loading Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = YouTubeRed)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("در حال بارگیری و تحلیل شبکه ها...", color = Color.White, fontSize = 14.sp)
                }
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage!!, color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp)
                }
            } else if (filteredChannels.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("هیچ شبکه‌ای پیدا نشد", color = SecondaryText, fontSize = 14.sp)
                }
            } else {
                // Display Grid / List of channels
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredChannels) { channel ->
                        ChannelItemRow(
                            channel = channel,
                            isActive = false,
                            onClick = { viewModel.selectChannel(channel) },
                            onFavClick = { viewModel.toggleFavorite(channel) },
                            isFav = false // Handled internally
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(preset: PresetCategory, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .border(1.dp, SurfaceAccent, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = preset.icon,
                contentDescription = null,
                tint = YouTubeRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = preset.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class PresetCategory(val title: String, val url: String, val icon: ImageVector)

// CHANNEL ITEM COMPONENT
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelItemRow(
    channel: Channel,
    isActive: Boolean,
    onClick: () -> Unit,
    onFavClick: () -> Unit,
    isFav: Boolean,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) SurfaceAccent else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo / Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            if (channel.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(channel.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = if (channel.isRadio) Icons.Filled.Radio else Icons.Filled.Tv,
                    contentDescription = null,
                    tint = if (channel.isRadio) Color(0xFFF97316) else YouTubeRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (channel.isRadio) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF97316).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("رادیو", color = Color(0xFFF97316), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = channel.groupName,
                color = SecondaryText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Bookmark toggle
        IconButton(onClick = onFavClick) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder, // Star/Favorite simplified view
                contentDescription = "علاقه‌مندی",
                tint = SecondaryText
            )
        }
    }
}

// PLAYLISTS / MY LISTS SCREEN
@Composable
fun PlaylistsScreen(viewModel: IptvViewModel) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var playlistNameFile by remember { mutableStateOf("لیست فایل شخصی") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val text = stream.bufferedReader().readText()
                    viewModel.importPlaylistFile(playlistNameFile, text) { success ->
                        // Callback showing it worked
                    }
                }
            } catch (e: Exception) {
                // error handling
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "لیست‌های پخش سفارشی شما",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "آدرس‌های M3U یا فایل‌های خود را اضافه کنید تا در هر زمان به آن‌ها دسترسی داشته باشید",
                color = SecondaryText,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistAdd,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هنوز لیست پخشی اضافه نکرده‌اید",
                            color = SecondaryText,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistItemRow(
                            playlist = playlist,
                            onSelect = { viewModel.loadPlaylistEntity(playlist) },
                            onDelete = { viewModel.deletePlaylist(playlist.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to add playlist
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // File Upload option
            FloatingActionButton(
                onClick = {
                    playlistNameFile = "لیست فایل ${playlists.size + 1}"
                    filePickerLauncher.launch("*/*")
                },
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = "آپلود فایل")
            }

            // URL Add option
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = YouTubeRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن لیست")
            }
        }
    }

    if (showAddDialog) {
        AddPlaylistDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, url ->
                viewModel.importPlaylistUrl(name, url)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PlaylistItemRow(
    playlist: PlaylistEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(YouTubeRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playlist.url.isNotEmpty()) Icons.Filled.Link else Icons.Filled.Description,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (playlist.url.isNotEmpty()) playlist.url else "فایل محلی M3U",
                        color = SecondaryText,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "حذف لیست",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن لیست پخش M3U جدید", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام لیست پخش") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = SurfaceAccent,
                        focusedLabelColor = YouTubeRed,
                        unfocusedLabelColor = SecondaryText,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("آدرس اینترنتی M3U") },
                    placeholder = { Text("https://example.com/playlist.m3u") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = SurfaceAccent,
                        focusedLabelColor = YouTubeRed,
                        unfocusedLabelColor = SecondaryText,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty() && url.isNotEmpty()) onConfirm(name, url) },
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
            ) {
                Text("افزودن", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color.White)
            }
        },
        containerColor = SurfaceDark
    )
}

// FAVORITES SCREEN
@Composable
fun FavoritesScreen(viewModel: IptvViewModel) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "کانال‌های مورد علاقه شما",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "دسترسی سریع و آنی به برنامه‌ها و رادیوهای نشان شده",
            color = SecondaryText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هنوز کانالی نشان نکرده‌اید",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(favorites) { favorite ->
                    val channel = Channel(
                        name = favorite.name,
                        streamUrl = favorite.streamUrl,
                        logoUrl = favorite.logoUrl,
                        groupName = favorite.groupName,
                        isRadio = favorite.isRadio
                    )
                    ChannelItemRow(
                        channel = channel,
                        isActive = false,
                        onClick = { viewModel.selectChannelFromFavorite(favorite) },
                        onFavClick = { viewModel.toggleFavorite(channel) },
                        isFav = true
                    )
                }
            }
        }
    }
}

// HISTORY / RECENT PLAYS SCREEN
@Composable
fun HistoryScreen(viewModel: IptvViewModel) {
    val recentPlays by viewModel.recentPlays.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "تاریخچه تماشا",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "لیست آخرین شبکه‌های پخش شده شما",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            if (recentPlays.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.textButtonColors(contentColor = YouTubeRed)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پاک کردن همه", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recentPlays.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تاریخچه شما خالی است",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(recentPlays) { recent ->
                    val channel = Channel(
                        name = recent.name,
                        streamUrl = recent.streamUrl,
                        logoUrl = recent.logoUrl,
                        groupName = recent.groupName,
                        isRadio = recent.isRadio
                    )
                    ChannelItemRow(
                        channel = channel,
                        isActive = false,
                        onClick = { viewModel.selectChannelFromRecent(recent) },
                        onFavClick = { viewModel.toggleFavorite(channel) },
                        isFav = false
                    )
                }
            }
        }
    }
}
