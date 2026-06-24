/**
 * ====================================================================
 * IPTV Pro Player & Intelligent Streaming Proxy
 * Cloudflare Worker - Single File Deployment
 * Featuring Premium YouTube-Inspired Sleek UI & RTL Support
 * ====================================================================
 */

const HTML_CONTENT = `<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>IPTV Pro Player</title>
    <link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>📺</text></svg>">
    
    <!-- Player Styles & Fonts -->
    <link rel="stylesheet" href="https://cdn.plyr.io/3.7.8/plyr.css" />
    <link href="https://fonts.googleapis.com/css2?family=Vazirmatn:wght@300;400;500;700;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet" />

    <style>
        :root {
            --bg-color: #0F0F0F;
            --surface: #1F1F1F;
            --surface-accent: #2B2B2B;
            --primary: #FF0000;
            --primary-hover: #CC0000;
            --text-color: #FFFFFF;
            --text-dim: #A0A0A0;
            --border: rgba(255, 255, 255, 0.08);
            --success: #10B981;
            --fav-color: #FFB300;
            --radio-color: #F97316;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Vazirmatn', system-ui, -apple-system, sans-serif;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            height: 100vh;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        /* Scrollbars */
        ::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: var(--surface-accent);
            border-radius: 10px;
        }

        /* App Bar */
        header {
            background: var(--bg-color);
            padding: 12px 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid var(--border);
            z-index: 10;
        }

        .header-logo {
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
        }

        .logo-box {
            width: 36px;
            height: 28px;
            background-color: var(--primary);
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .logo-triangle {
            width: 0;
            height: 0;
            border-top: 5px solid transparent;
            border-left: 9px solid white;
            border-bottom: 5px solid transparent;
            margin-left: -2px;
        }

        .header-title {
            font-size: 1.25rem;
            font-weight: 900;
            letter-spacing: -0.5px;
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .search-container {
            position: relative;
            flex: 1;
            max-width: 480px;
            margin: 0 16px;
        }

        .search-input {
            width: 100%;
            background-color: var(--surface);
            border: 1px solid var(--surface-accent);
            color: white;
            padding: 10px 42px 10px 16px;
            border-radius: 20px;
            font-size: 0.9rem;
            outline: none;
            transition: all 0.2s ease;
        }

        .search-input:focus {
            border-color: #555;
            background-color: #151515;
            box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.05);
        }

        .search-icon {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-dim);
            font-size: 20px;
            pointer-events: none;
        }

        /* Main Workspace */
        .main-container {
            display: flex;
            flex: 1;
            overflow: hidden;
        }

        /* Player panel */
        .player-panel {
            flex: 1.4;
            display: flex;
            flex-direction: column;
            overflow-y: auto;
            background: radial-gradient(circle at top, #1A1A1A 0%, var(--bg-color) 100%);
            padding: 16px;
            gap: 16px;
        }

        .video-wrapper {
            width: 100%;
            aspect-ratio: 16/9;
            background-color: #000;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 12px 30px rgba(0,0,0,0.5);
            border: 1px solid var(--border);
            position: relative;
        }

        .video-wrapper video {
            width: 100%;
            height: 100%;
        }

        .media-details {
            background-color: var(--surface);
            border-radius: 16px;
            padding: 16px;
            border: 1px solid var(--border);
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .media-title-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 12px;
        }

        .media-title {
            font-size: 1.15rem;
            font-weight: 700;
            line-height: 1.4;
        }

        .media-subtitle {
            font-size: 0.85rem;
            color: var(--text-dim);
            margin-top: 4px;
        }

        .media-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 4px;
        }

        .action-chip {
            background: var(--surface-accent);
            color: white;
            border: none;
            border-radius: 20px;
            padding: 8px 16px;
            font-size: 0.8rem;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .action-chip:hover {
            background-color: #3A3A3A;
        }

        .action-chip.active-fav {
            background-color: var(--fav-color);
            color: black;
        }

        .action-chip.active-proxy {
            background-color: #4F46E5;
        }

        /* Playlists & Queue Panel */
        .playlist-panel {
            flex: 1;
            border-left: 1px solid var(--border);
            background-color: var(--bg-color);
            display: flex;
            flex-direction: column;
        }

        /* Category Chips row */
        .chips-container {
            display: flex;
            gap: 8px;
            padding: 12px 16px;
            overflow-x: auto;
            white-space: nowrap;
            border-bottom: 1px solid var(--border);
        }

        .chip {
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 500;
            background-color: var(--surface);
            color: white;
            cursor: pointer;
            transition: all 0.2s ease;
            user-select: none;
            border: 1px solid transparent;
        }

        .chip:hover {
            background-color: var(--surface-accent);
        }

        .chip.active {
            background-color: white;
            color: black;
        }

        /* Controls / Imports section */
        .control-section {
            padding: 14px 16px;
            background-color: var(--surface);
            border-bottom: 1px solid var(--border);
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .input-row {
            display: flex;
            gap: 8px;
        }

        .form-select, .form-input {
            background-color: var(--bg-color);
            border: 1px solid var(--surface-accent);
            color: white;
            padding: 10px 12px;
            border-radius: 8px;
            font-size: 0.85rem;
            outline: none;
            flex: 1;
        }

        .btn-primary {
            background-color: var(--primary);
            color: white;
            border: none;
            padding: 10px 16px;
            border-radius: 8px;
            font-weight: 700;
            font-size: 0.85rem;
            cursor: pointer;
            transition: background 0.2s;
            white-space: nowrap;
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
        }

        .btn-file {
            position: relative;
            overflow: hidden;
            background-color: #4F46E5;
            color: white;
            padding: 10px 14px;
            border-radius: 8px;
            font-size: 0.85rem;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 500;
        }

        .btn-file input[type=file] {
            position: absolute;
            font-size: 100px;
            opacity: 0;
            right: 0;
            top: 0;
            cursor: pointer;
        }

        /* Channels list */
        .channels-list {
            flex: 1;
            overflow-y: auto;
            list-style: none;
            padding: 8px 16px;
        }

        .channel-row {
            display: flex;
            align-items: center;
            padding: 10px;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
            margin-bottom: 6px;
            border: 1px solid transparent;
        }

        .channel-row:hover {
            background-color: var(--surface);
        }

        .channel-row.active {
            background-color: rgba(255,0,0,0.08);
            border-color: rgba(255,0,0,0.2);
        }

        .channel-logo-container {
            width: 44px;
            height: 44px;
            border-radius: 8px;
            background-color: var(--surface-accent);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-left: 12px;
            overflow: hidden;
            flex-shrink: 0;
        }

        .channel-logo-container img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        .channel-logo-placeholder {
            font-size: 20px;
            color: var(--text-dim);
        }

        .channel-details {
            flex: 1;
            overflow: hidden;
        }

        .channel-name-line {
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .channel-name {
            font-size: 0.9rem;
            font-weight: 700;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .radio-badge {
            background-color: var(--radio-color);
            color: white;
            font-size: 0.7rem;
            font-weight: 900;
            padding: 2px 6px;
            border-radius: 4px;
        }

        .channel-group-tag {
            font-size: 0.75rem;
            color: var(--text-dim);
            margin-top: 2px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .channel-row-action {
            color: var(--text-dim);
            opacity: 0.6;
            cursor: pointer;
            padding: 6px;
            border-radius: 50%;
            transition: all 0.2s ease;
        }

        .channel-row-action:hover {
            opacity: 1;
            background-color: var(--surface-accent);
            color: var(--fav-color);
        }

        .channel-row-action.active {
            color: var(--fav-color);
            opacity: 1;
        }

        /* Bottom Tab Navigation for Mobile */
        nav.mobile-nav {
            display: none;
            background-color: var(--surface);
            border-top: 1px solid var(--border);
            padding: 8px 0 24px;
            justify-content: space-around;
            align-items: center;
        }

        .nav-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;
            color: var(--text-dim);
            font-size: 0.7rem;
            cursor: pointer;
            transition: color 0.2s;
        }

        .nav-item.active {
            color: var(--primary);
        }

        .nav-item span {
            font-size: 22px;
        }

        /* Generic utilities */
        .empty-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
            color: var(--text-dim);
            text-align: center;
            gap: 12px;
        }

        .empty-state span {
            font-size: 48px;
            color: var(--surface-accent);
        }

        .buffering-spinner {
            position: absolute;
            inset: 0;
            display: none;
            align-items: center;
            justify-content: center;
            background-color: rgba(0,0,0,0.5);
            z-index: 5;
        }

        .spinner {
            width: 50px;
            height: 50px;
            border: 3px solid rgba(255,255,255,0.1);
            border-top: 3px solid var(--primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Media Queries */
        @media (max-width: 992px) {
            .main-container {
                flex-direction: column;
            }
            .player-panel {
                flex: none;
            }
            .playlist-panel {
                flex: 1;
                border-left: none;
                border-top: 1px solid var(--border);
            }
            header {
                padding: 10px 12px;
            }
            .search-container {
                margin: 0 8px;
            }
            nav.mobile-nav {
                display: flex;
            }
        }
    </style>
</head>
<body>
    <!-- App Bar Header -->
    <header>
        <div class="header-logo" onclick="switchTab('explore')">
            <div class="logo-box">
                <div class="logo-triangle"></div>
            </div>
            <span class="header-title">IPTV Pro</span>
        </div>

        <div class="search-container">
            <input type="text" id="searchInput" class="search-input" placeholder="جستجو در بین شبکه‌ها..." oninput="handleSearch()">
            <span class="material-symbols-outlined search-icon">search</span>
        </div>

        <div class="header-actions">
            <!-- Dynamic channel count -->
            <span id="channelCount" style="font-size: 0.8rem; color: var(--text-dim); font-weight: 500;">0 شبکه</span>
        </div>
    </header>

    <!-- Main Container -->
    <div class="main-container">
        <!-- Video Player & Metadata Pane -->
        <div class="player-panel">
            <div class="video-wrapper">
                <video id="player" controls crossorigin="anonymous" playsinline></video>
                <div class="buffering-spinner" id="playerBuffering">
                    <div class="spinner"></div>
                </div>
            </div>

            <!-- Active Station Watch Details -->
            <div class="media-details" id="playbackDetails" style="display: none;">
                <div class="media-title-row">
                    <div>
                        <h2 class="media-title" id="activeChannelName">نام شبکه در حال پخش</h2>
                        <p class="media-subtitle" id="activeChannelGroup">دسته‌بندی شبکه</p>
                    </div>
                </div>
                <div class="media-actions">
                    <button class="action-chip" id="favToggleBtn" onclick="toggleActiveFavorite()">
                        <span class="material-symbols-outlined" id="favIcon">favorite</span>
                        <span id="favText">افزودن به علاقه‌مندی‌ها</span>
                    </button>
                    <button class="action-chip" id="proxyToggleBtn" onclick="toggleSmartProxy()">
                        <span class="material-symbols-outlined">security</span>
                        <span id="proxyText">پروکسی هوشمند: روشن</span>
                    </button>
                </div>
            </div>
        </div>

        <!-- Channels & Playlists Menu Section -->
        <div class="playlist-panel">
            <!-- YouTube Category chips -->
            <div class="chips-container" id="chipsContainer">
                <!-- Loaded dynamically -->
            </div>

            <!-- Settings / Imports Controls -->
            <div class="control-section">
                <div class="input-row">
                    <select id="playlistSelector" class="form-select" onchange="handlePlaylistSelection()">
                        <option value="preset:persian">🇮🇷 لیست شبکه‌های فارسی</option>
                        <option value="preset:sports">⚽ شبکه‌های ورزشی جهان</option>
                        <option value="preset:movies">🎬 فیلم و سریال جهانی</option>
                        <option value="preset:music">🎵 کانال‌های موزیک</option>
                        <option value="preset:news">📰 اخبار بین‌المللی</option>
                        <option value="preset:all">🌍 همه‌ی شبکه‌های جهان</option>
                        <optgroup id="userPlaylistsGroup" label="لیست‌های پخش شما"></optgroup>
                    </select>
                    
                    <button class="btn-primary" onclick="showAddUrlPrompt()">
                        <span class="material-symbols-outlined" style="font-size: 16px; vertical-align: middle;">add</span>
                        آدرس اینترنتی
                    </button>

                    <div class="btn-file">
                        <span class="material-symbols-outlined" style="font-size: 16px; margin-left: 4px;">upload_file</span>
                        فایل M3U
                        <input type="file" id="fileUploadInput" accept=".m3u,.m3u8,text/plain" onchange="importLocalFile(event)">
                    </div>
                </div>
            </div>

            <!-- Channel List Scroll Area -->
            <ul class="channels-list" id="channelListContainer">
                <!-- Rendered dynamically -->
            </ul>
        </div>
    </div>

    <!-- Mobile Bottom Navigation Bar -->
    <nav class="mobile-nav">
        <div class="nav-item active" id="nav-explore" onclick="switchTab('explore')">
            <span class="material-symbols-outlined">explore</span>
            <div>کاووش</div>
        </div>
        <div class="nav-item" id="nav-favorites" onclick="switchTab('favorites')">
            <span class="material-symbols-outlined">favorite</span>
            <div>علاقه‌مندی‌ها</div>
        </div>
        <div class="nav-item" id="nav-history" onclick="switchTab('history')">
            <span class="material-symbols-outlined">history</span>
            <div>تاریخچه تماشا</div>
        </div>
    </nav>

    <!-- Libs and Player Core Engine -->
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
    <script src="https://cdn.plyr.io/3.7.8/plyr.polyfilled.js"></script>

    <script>
        // Applet State
        let fullChannels = [];
        let filteredChannels = [];
        let channelGroups = ["همه"];
        let selectedGroup = "همه";
        let currentTab = "explore"; // explore, favorites, history
        let favorites = [];
        let history = [];
        let customPlaylists = [];
        let activeChannel = null;
        let useCustomProxy = true;
        let plyrPlayer = null;
        let hlsEngine = null;

        const PRESETS = {
            'preset:persian': 'https://iptv-org.github.io/iptv/languages/fas.m3u',
            'preset:sports': 'https://iptv-org.github.io/iptv/categories/sports.m3u',
            'preset:movies': 'https://iptv-org.github.io/iptv/categories/movies.m3u',
            'preset:music': 'https://iptv-org.github.io/iptv/categories/music.m3u',
            'preset:news': 'https://iptv-org.github.io/iptv/categories/news.m3u',
            'preset:all': 'https://iptv-org.github.io/iptv/index.m3u'
        };

        // Initialize App
        document.addEventListener("DOMContentLoaded", () => {
            loadSavedData();
            initPlayer();
            loadPlaylist('preset:persian');
        });

        function loadSavedData() {
            try {
                favorites = JSON.parse(localStorage.getItem('iptv_favorites_v9') || '[]');
                history = JSON.parse(localStorage.getItem('iptv_history_v9') || '[]');
                customPlaylists = JSON.parse(localStorage.getItem('iptv_playlists_v9') || '[]');
                renderCustomPlaylistsInSelector();
            } catch (e) {
                console.error("Error loading localStorage", e);
            }
        }

        function initPlayer() {
            const video = document.getElementById("player");
            plyrPlayer = new Plyr(video, {
                controls: ["play-large", "play", "progress", "current-time", "mute", "volume", "settings", "pip", "fullscreen"],
                settings: ["quality", "speed"],
                i18n: { speed: "سرعت پخش", quality: "کیفیت", normal: "عادی" }
            });

            // Bind error & buffering listeners
            video.addEventListener('waiting', () => {
                document.getElementById('playerBuffering').style.display = 'flex';
            });
            video.addEventListener('playing', () => {
                document.getElementById('playerBuffering').style.display = 'none';
            });
        }

        // Fetch & Parser M3U
        async function loadPlaylist(sourceKey) {
            document.getElementById('channelListContainer').innerHTML = \`
                <div class="empty-state">
                    <span class="material-symbols-outlined spinner" style="animation: spin 1.5s linear infinite;">sync</span>
                    <div>در حال بارگیری و تحلیل لیست شبکه ها...</div>
                </div>
            \`;

            let m3uText = "";
            let playlistName = "";

            if (sourceKey.startsWith('preset:')) {
                const url = PRESETS[sourceKey];
                try {
                    const response = await fetch(\`/proxy-m3u?url=\${encodeURIComponent(url)}\`);
                    if (!response.ok) throw new Error("HTTP " + response.status);
                    m3uText = await response.text();
                } catch (e) {
                    showErrorState("خطا در ارتباط با سرور. لطفا اتصال اینترنت خود را بررسی کنید.");
                    return;
                }
            } else if (sourceKey.startsWith('custom:')) {
                const id = sourceKey.split('custom:')[1];
                const playlist = customPlaylists.find(p => p.id === id);
                if (playlist) {
                    m3uText = playlist.content;
                }
            }

            parseM3UAndRender(m3uText);
        }

        function parseM3UAndRender(text) {
            const lines = text.split(/\\r?\\n/);
            fullChannels = [];
            channelGroups = ["همه"];

            let currentItem = null;

            for (let i = 0; i < lines.length; i++) {
                const line = lines[i].trim();
                if (line.startsWith("#EXTINF:")) {
                    const logoMatch = line.match(/tvg-logo="([^"]+)"/);
                    const groupMatch = line.match(/group-title="([^"]+)"/);
                    const parts = line.split(",");
                    const name = parts.pop().trim() || "شبکه ناشناس";
                    const logo = logoMatch ? logoMatch[1] : "";
                    const group = groupMatch ? groupMatch[1] : "عمومی";

                    currentItem = {
                        name: name,
                        logoUrl: logo,
                        groupName: group,
                        streamUrl: "",
                        isRadio: name.toLowerCase().includes("radio") || name.toLowerCase().includes(" fm") || group.toLowerCase().includes("radio")
                    };
                } else if (line.startsWith("http://") || line.startsWith("https://")) {
                    if (currentItem) {
                        currentItem.streamUrl = line;
                        fullChannels.push(currentItem);
                        if (!channelGroups.includes(currentItem.groupName)) {
                            channelGroups.push(currentItem.groupName);
                        }
                        currentItem = null;
                    }
                }
            }

            applyFilters();
            renderCategoryChips();
        }

        // Filters and Rendering
        function applyFilters() {
            const query = document.getElementById('searchInput').value.trim().toLowerCase();

            if (currentTab === 'explore') {
                filteredChannels = fullChannels.filter(c => {
                    const matchesGroup = (selectedGroup === "همه" || c.groupName === selectedGroup);
                    const matchesQuery = (!query || c.name.toLowerCase().includes(query) || c.groupName.toLowerCase().includes(query));
                    return matchesGroup && matchesQuery;
                });
            } else if (currentTab === 'favorites') {
                filteredChannels = favorites.filter(c => !query || c.name.toLowerCase().includes(query));
            } else if (currentTab === 'history') {
                filteredChannels = history.filter(c => !query || c.name.toLowerCase().includes(query));
            }

            document.getElementById('channelCount').innerText = \`\${filteredChannels.length} شبکه\`;
            renderChannelsList();
        }

        function renderCategoryChips() {
            const container = document.getElementById('chipsContainer');
            if (currentTab !== 'explore') {
                container.style.display = 'none';
                return;
            }
            container.style.display = 'flex';
            container.innerHTML = "";

            // Dynamic grouping chips similar to YouTube dynamic home tabs
            channelGroups.slice(0, 15).forEach(group => {
                const activeClass = group === selectedGroup ? "active" : "";
                const el = document.createElement('div');
                el.className = \`chip \${activeClass}\`;
                el.innerText = group;
                el.onclick = () => {
                    selectedGroup = group;
                    renderCategoryChips();
                    applyFilters();
                };
                container.appendChild(el);
            });
        }

        function renderChannelsList() {
            const container = document.getElementById('channelListContainer');
            container.innerHTML = "";

            if (filteredChannels.length === 0) {
                container.innerHTML = \`
                    <div class="empty-state">
                        <span class="material-symbols-outlined">search_off</span>
                        <div>هیچ شبکه‌ای پیدا نشد</div>
                    </div>
                \`;
                return;
            }

            // Render first 100 channels for performance, with lazy rendering
            filteredChannels.slice(0, 150).forEach(channel => {
                const isActive = activeChannel && activeChannel.streamUrl === channel.streamUrl;
                const isFav = favorites.some(f => f.streamUrl === channel.streamUrl);

                const li = document.createElement('li');
                li.className = \`channel-row \${isActive ? 'active' : ''}\`;
                li.onclick = () => startPlayback(channel);

                // Lazy load logos
                let logoHtml = \`<span class="material-symbols-outlined channel-logo-placeholder">\${channel.isRadio ? 'radio' : 'tv'}</span>\`;
                if (channel.logoUrl) {
                    logoHtml = \`<img src="\${channel.logoUrl}" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                                <span class="material-symbols-outlined channel-logo-placeholder" style="display:none;">\${channel.isRadio ? 'radio' : 'tv'}</span>\`;
                }

                li.innerHTML = \`
                    <div class="channel-logo-container">
                        \${logoHtml}
                    </div>
                    <div class="channel-details">
                        <div class="channel-name-line">
                            <span class="channel-name">\${channel.name}</span>
                            \${channel.isRadio ? '<span class="radio-badge">رادیو</span>' : ''}
                        </div>
                        <div class="channel-group-tag">\${channel.groupName}</div>
                    </div>
                    <span class="material-symbols-outlined channel-row-action \${isFav ? 'active' : ''}" 
                          onclick="toggleFavorite(event, \${JSON.stringify(channel).replace(/"/g, '&quot;')})">
                        favorite
                    </span>
                \`;
                container.appendChild(li);
            });
        }

        // Playback Engine
        function startPlayback(channel) {
            activeChannel = channel;
            
            // Set dynamic titles
            document.getElementById('playbackDetails').style.display = 'flex';
            document.getElementById('activeChannelName').innerText = channel.name;
            document.getElementById('activeChannelGroup').innerText = channel.groupName;

            // Add to history
            addToHistory(channel);

            // Set favorite button active state
            updateFavDetailsBtn();

            const video = document.getElementById("player");
            const targetUrl = useCustomProxy ? \`/proxy-stream?url=\${encodeURIComponent(channel.streamUrl)}\` : channel.streamUrl;

            document.getElementById('playerBuffering').style.display = 'flex';

            if (Hls.isSupported() && (channel.streamUrl.includes('.m3u8') || channel.streamUrl.includes('m3u8'))) {
                if (hlsEngine) hlsEngine.destroy();
                hlsEngine = new Hls({ maxBufferLength: 30 });
                hlsEngine.loadSource(targetUrl);
                hlsEngine.attachMedia(video);
                hlsEngine.on(Hls.Events.MANIFEST_PARSED, () => {
                    video.play().catch(e => console.log("Play failed", e));
                });
            } else {
                video.src = targetUrl;
                video.play().catch(e => console.log("Play failed", e));
            }

            // Update UI highlighted row
            renderChannelsList();
        }

        function toggleActiveFavorite() {
            if (!activeChannel) return;
            toggleFavorite(null, activeChannel);
            updateFavDetailsBtn();
        }

        function updateFavDetailsBtn() {
            if (!activeChannel) return;
            const isFav = favorites.some(f => f.streamUrl === activeChannel.streamUrl);
            const btn = document.getElementById('favToggleBtn');
            const icon = document.getElementById('favIcon');
            const text = document.getElementById('favText');

            if (isFav) {
                btn.classList.add('active-fav');
                icon.innerText = "favorite";
                text.innerText = "نشان شده";
            } else {
                btn.classList.remove('active-fav');
                icon.innerText = "favorite_border";
                text.innerText = "افزودن به علاقه‌مندی‌ها";
            }
        }

        function toggleSmartProxy() {
            useCustomProxy = !useCustomProxy;
            const btn = document.getElementById('proxyToggleBtn');
            const text = document.getElementById('proxyText');

            if (useCustomProxy) {
                btn.classList.add('active-proxy');
                text.innerText = "پروکسی هوشمند: روشن";
            } else {
                btn.classList.remove('active-proxy');
                text.innerText = "پروکسی: خاموش";
            }

            if (activeChannel) {
                startPlayback(activeChannel);
            }
        }

        // Favorites & History Controllers
        function toggleFavorite(event, channel) {
            if (event) event.stopPropagation();

            const idx = favorites.findIndex(f => f.streamUrl === channel.streamUrl);
            if (idx > -1) {
                favorites.splice(idx, 1);
            } else {
                favorites.unshift(channel);
            }

            localStorage.setItem('iptv_favorites_v9', JSON.stringify(favorites));
            applyFilters();

            if (activeChannel && activeChannel.streamUrl === channel.streamUrl) {
                updateFavDetailsBtn();
            }
        }

        function addToHistory(channel) {
            history = history.filter(h => h.streamUrl !== channel.streamUrl);
            history.unshift(channel);
            if (history.length > 50) history.pop();
            localStorage.setItem('iptv_history_v9', JSON.stringify(history));
        }

        // Search and Tabs navigation
        function handleSearch() {
            applyFilters();
        }

        function switchTab(tab) {
            currentTab = tab;
            document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
            document.getElementById(\`nav-\${tab}\`).classList.add('active');

            applyFilters();
            renderCategoryChips();
        }

        // Form imports
        function handlePlaylistSelection() {
            const val = document.getElementById('playlistSelector').value;
            loadPlaylist(val);
        }

        function showAddUrlPrompt() {
            const name = prompt("نام لیست پخش:");
            if (!name) return;
            const url = prompt("آدرس اینترنتی فایل M3U:");
            if (!url) return;

            importPlaylistFromUrl(name, url);
        }

        async function importPlaylistFromUrl(name, url) {
            try {
                const res = await fetch(\`/proxy-m3u?url=\${encodeURIComponent(url)}\`);
                if (!res.ok) throw new Error("HTTP " + res.status);
                const text = await res.text();

                const newId = Math.random().toString(36).substring(7);
                customPlaylists.push({ id: newId, name: name, content: text });
                localStorage.setItem('iptv_playlists_v9', JSON.stringify(customPlaylists));

                renderCustomPlaylistsInSelector();
                document.getElementById('playlistSelector').value = \`custom:\${newId}\`;
                handlePlaylistSelection();
            } catch (e) {
                alert("امکان دریافت لیست پخش وجود نداشت. لطفا صحت آدرس را مجددا بررسی فرمایید.");
            }
        }

        function importLocalFile(event) {
            const file = event.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = (e) => {
                const text = e.target.result;
                const newId = Math.random().toString(36).substring(7);
                customPlaylists.push({ id: newId, name: file.name, content: text });
                localStorage.setItem('iptv_playlists_v9', JSON.stringify(customPlaylists));

                renderCustomPlaylistsInSelector();
                document.getElementById('playlistSelector').value = \`custom:\${newId}\`;
                handlePlaylistSelection();
            };
            reader.readAsText(file);
        }

        function renderCustomPlaylistsInSelector() {
            const optGroup = document.getElementById('userPlaylistsGroup');
            optGroup.innerHTML = "";
            customPlaylists.forEach(p => {
                const opt = document.createElement('option');
                opt.value = \`custom:\${p.id}\`;
                opt.innerText = "📁 " + p.name;
                optGroup.appendChild(opt);
            });
        }

        function showErrorState(msg) {
            document.getElementById('channelListContainer').innerHTML = \`
                <div class="empty-state">
                    <span class="material-symbols-outlined" style="color: var(--primary);">error</span>
                    <div>\${msg}</div>
                </div>
            \`;
        }
    </script>
</body>
</html>`;

export default {
    async fetch(request) {
        const corsHeaders = {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, HEAD, POST, OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type, Range, User-Agent, X-Requested-With, Origin, Accept",
            "Access-Control-Expose-Headers": "Content-Length, Content-Range, Content-Type"
        };

        if (request.method === "OPTIONS") {
            return new Response(null, { headers: corsHeaders });
        }

        try {
            const requestUrl = new URL(request.url);
            const path = requestUrl.pathname;

            // Route 1: Render the Sleek YouTube-Inspired Web App HTML UI
            if (path === "/" || path === "") {
                return new Response(HTML_CONTENT, {
                    headers: {
                        "Content-Type": "text/html; charset=utf-8",
                        "Cache-Control": "no-store, no-cache, must-revalidate",
                    }
                });
            }

            // Route 2: Proxy CORS/geo-blocked M3U playlists
            if (path === "/proxy-m3u") {
                const targetUrl = requestUrl.searchParams.get("url");
                if (!targetUrl || !targetUrl.startsWith("http")) {
                    return new Response("Invalid URL", { status: 400, headers: corsHeaders });
                }

                const m3uRes = await fetch(targetUrl, {
                    headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" }
                });
                return new Response(m3uRes.body, {
                    headers: Object.assign({}, corsHeaders, {
                        "Content-Type": "text/plain",
                        "Cache-Control": "public, max-age=3600"
                    })
                });
            }

            // Route 3: Deep Intelligent Media Stream Proxying
            if (path === "/proxy-stream") {
                const targetUrlString = requestUrl.searchParams.get("url");
                if (!targetUrlString) {
                    return new Response("Missing url", { status: 400, headers: corsHeaders });
                }

                const tUrl = new URL(targetUrlString);
                const queryString = tUrl.search || "";
                const safeHeaders = new Headers();
                safeHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                safeHeaders.set("Accept", "*/*");
                safeHeaders.set("Referer", tUrl.origin + "/");
                safeHeaders.set("Origin", tUrl.origin);

                const clientRange = request.headers.get("Range");
                if (clientRange) {
                    safeHeaders.set("Range", clientRange);
                }

                const upstreamRes = await fetch(tUrl.toString(), { headers: safeHeaders, redirect: "follow" });
                const proxyStatus = upstreamRes.status;

                if (!upstreamRes.ok && proxyStatus !== 206) {
                    return new Response("Upstream Error: " + proxyStatus, { status: proxyStatus, headers: corsHeaders });
                }

                let contentType = upstreamRes.headers.get("content-type") || "application/octet-stream";
                let body = upstreamRes.body;

                // Support subtitle tracks
                if (tUrl.pathname.includes(".vtt") || contentType.includes("text/vtt")) {
                    contentType = "text/vtt; charset=utf-8";
                }

                // If stream index file (HLS playlist / m3u8 file), rewrite internal segment URLs to pass through the proxy!
                if (contentType.includes("mpegurl") || tUrl.pathname.includes(".m3u8") || tUrl.pathname.includes(".m3u")) {
                    let text = await upstreamRes.text();
                    const finalUrl = upstreamRes.url;
                    const finalBase = finalUrl.substring(0, finalUrl.lastIndexOf("/") + 1);

                    // Rewrite Relative URLs of segments and keys
                    text = text.replace(/^([^#][^\r\n]*)/gm, function(line) {
                        if (!line.trim()) return line;
                        const absUrl = new URL(line, finalBase);
                        if (queryString && !absUrl.search) absUrl.search = queryString;
                        return requestUrl.origin + "/proxy-stream?url=" + encodeURIComponent(absUrl.toString());
                    });

                    text = text.replace(/URI="([^"]+)"/g, function(match, p1) {
                        if (p1.startsWith("data:")) return match;
                        const absUrl = new URL(p1, finalBase);
                        if (queryString && !absUrl.search && !p1.includes("key")) absUrl.search = queryString;
                        return 'URI="' + requestUrl.origin + "/proxy-stream?url=" + encodeURIComponent(absUrl.toString()) + '"';
                    });

                    body = text;
                }

                return new Response(body, {
                    status: proxyStatus,
                    headers: Object.assign({}, corsHeaders, { "Content-Type": contentType })
                });
            }

            return new Response("Not found", { status: 404, headers: corsHeaders });
        } catch (err) {
            return new Response("Worker Error: " + err.message, { status: 500, headers: corsHeaders });
        }
    }
}
