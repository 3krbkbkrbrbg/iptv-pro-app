package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IptvViewModel(private val repository: IptvRepository) : ViewModel() {

    val playlists = repository.allPlaylists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentPlays = repository.recentPlays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentPlaylistChannels = MutableStateFlow<List<Channel>>(emptyList())
    val currentPlaylistChannels: StateFlow<List<Channel>> = _currentPlaylistChannels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow("All")
    val selectedGroup: StateFlow<String> = _selectedGroup.asStateFlow()

    val channelGroups = _currentPlaylistChannels.map { channels ->
        val groups = channels.map { it.groupName }.distinct().sorted()
        listOf("All") + groups
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val filteredChannels = combine(
        _currentPlaylistChannels,
        _searchQuery,
        _selectedGroup
    ) { channels, query, group ->
        channels.filter { channel ->
            val matchesGroup = group == "All" || channel.groupName == group
            val matchesQuery = query.isEmpty() || 
                channel.name.contains(query, ignoreCase = true) || 
                channel.groupName.contains(query, ignoreCase = true)
            matchesGroup && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChannel = MutableStateFlow<Channel?>(null)
    val activeChannel: StateFlow<Channel?> = _activeChannel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _useCustomUserAgent = MutableStateFlow(true)
    val useCustomUserAgent: StateFlow<Boolean> = _useCustomUserAgent.asStateFlow()

    init {
        // Default startup playlist is Persian Channels from iptv-org
        loadPlaylistFromUrl("Persian Channels", "https://iptv-org.github.io/iptv/languages/fas.m3u")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedGroup(group: String) {
        _selectedGroup.value = group
    }

    fun toggleUserAgent() {
        _useCustomUserAgent.value = !_useCustomUserAgent.value
    }

    fun selectChannel(channel: Channel) {
        _activeChannel.value = channel
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentPlay(
                name = channel.name,
                streamUrl = channel.streamUrl,
                logoUrl = channel.logoUrl,
                groupName = channel.groupName,
                isRadio = channel.isRadio
            )
        }
    }

    fun selectChannelFromFavorite(fav: FavoriteEntity) {
        val channel = Channel(
            name = fav.name,
            streamUrl = fav.streamUrl,
            logoUrl = fav.logoUrl,
            groupName = fav.groupName,
            isRadio = fav.isRadio
        )
        selectChannel(channel)
    }

    fun selectChannelFromRecent(rec: RecentPlayEntity) {
        val channel = Channel(
            name = rec.name,
            streamUrl = rec.streamUrl,
            logoUrl = rec.logoUrl,
            groupName = rec.groupName,
            isRadio = rec.isRadio
        )
        selectChannel(channel)
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = repository.isFavorite(channel.streamUrl)
            if (isFav) {
                repository.removeFavorite(channel.streamUrl)
            } else {
                repository.addFavorite(
                    name = channel.name,
                    streamUrl = channel.streamUrl,
                    logoUrl = channel.logoUrl,
                    groupName = channel.groupName,
                    isRadio = channel.isRadio
                )
            }
        }
    }

    fun isFavorite(url: String): Flow<Boolean> {
        return repository.isFavoriteFlow(url)
    }

    fun loadPlaylistFromUrl(name: String, url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedGroup.value = "All"
            _searchQuery.value = ""
            try {
                val content = repository.fetchRemotePlaylist(url)
                val channels = repository.parseM3uContent(content)
                _currentPlaylistChannels.value = channels
                if (channels.isEmpty()) {
                    _errorMessage.value = "No channels found."
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Error loading playlist from URL: $url", e)
                _errorMessage.value = "Failed to load playlist: ${e.localizedMessage ?: "Network error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlaylistEntity(playlist: PlaylistEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedGroup.value = "All"
            _searchQuery.value = ""
            try {
                val content = if (playlist.content.isNotEmpty()) {
                    playlist.content
                } else if (playlist.url.isNotEmpty()) {
                    repository.fetchRemotePlaylist(playlist.url)
                } else {
                    ""
                }
                val channels = repository.parseM3uContent(content)
                _currentPlaylistChannels.value = channels
                if (channels.isEmpty()) {
                    _errorMessage.value = "No channels found."
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Error loading playlist entity", e)
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importPlaylistUrl(name: String, url: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val content = repository.fetchRemotePlaylist(url)
                val channels = repository.parseM3uContent(content)
                if (channels.isNotEmpty()) {
                    repository.importPlaylist(name = name, url = url, content = content, isCustom = true)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Error importing URL", e)
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importPlaylistFile(name: String, content: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val channels = repository.parseM3uContent(content)
                if (channels.isNotEmpty()) {
                    repository.importPlaylist(name = name, url = "", content = content, isCustom = true)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Error importing file content", e)
                onComplete(false)
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    class Factory(private val repository: IptvRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IptvViewModel(repository) as T
        }
    }
}
