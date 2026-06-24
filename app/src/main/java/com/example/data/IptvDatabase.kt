package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String = "",
    val content: String = "", // M3U plain text content
    val isCustom: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val logoUrl: String = "",
    val groupName: String = "",
    val streamUrl: String,
    val isRadio: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_plays")
data class RecentPlayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val logoUrl: String = "",
    val groupName: String = "",
    val streamUrl: String,
    val isRadio: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface IptvDao {
    // Playlists
    @Query("SELECT * FROM playlists ORDER BY timestamp DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamUrl = :url LIMIT 1)")
    fun isFavoriteFlow(url: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamUrl = :url LIMIT 1)")
    suspend fun isFavorite(url: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE streamUrl = :url")
    suspend fun deleteFavoriteByUrl(url: String)

    // Recent plays
    @Query("SELECT * FROM recent_plays ORDER BY timestamp DESC LIMIT 50")
    fun getRecentPlaysFlow(): Flow<List<RecentPlayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentPlay(recentPlay: RecentPlayEntity)

    @Query("DELETE FROM recent_plays WHERE streamUrl = :url")
    suspend fun deleteRecentPlayByUrl(url: String)

    @Query("DELETE FROM recent_plays")
    suspend fun clearHistory()
}

@Database(entities = [PlaylistEntity::class, FavoriteEntity::class, RecentPlayEntity::class], version = 1, exportSchema = false)
abstract class IptvDatabase : RoomDatabase() {
    abstract fun iptvDao(): IptvDao
}
