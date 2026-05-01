package com.example.myottapp.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import android.content.Context
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────
//  Room Database — Local Cache
//  Place at: data/local/OttDatabase.kt
// ─────────────────────────────────────────────────────────────────────

// ── Entities ──────────────────────────────────────────────────────────

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val releaseDate: String,
    val category: String,          // "trending" | "popular" | "top_rated"
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterPath: String?,
    val progress: Float,           // 0.0 - 1.0
    val watchedDuration: Long,     // seconds
    val totalDuration: Long,
    val lastWatched: Long = System.currentTimeMillis()
)

@Entity(tableName = "my_list")
data class MyListEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterPath: String?,
    val addedAt: Long = System.currentTimeMillis()
)

// ── DAOs ──────────────────────────────────────────────────────────────

@Dao
interface MovieCacheDao {
    @Query("SELECT * FROM cached_movies WHERE category = :category ORDER BY voteAverage DESC")
    fun getMoviesByCategory(category: String): Flow<List<CachedMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<CachedMovieEntity>)

    @Query("DELETE FROM cached_movies WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Query("SELECT * FROM cached_movies WHERE cachedAt < :expiryTime")
    suspend fun getExpiredCache(expiryTime: Long): List<CachedMovieEntity>

    @Query("DELETE FROM cached_movies WHERE cachedAt < :expiryTime")
    suspend fun clearExpiredCache(expiryTime: Long)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC LIMIT 20")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId")
    suspend fun getProgress(movieId: Int): WatchHistoryEntity?

    @Query("DELETE FROM watch_history WHERE movieId = :movieId")
    suspend fun removeFromHistory(movieId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface MyListDao {
    @Query("SELECT * FROM my_list ORDER BY addedAt DESC")
    fun getMyList(): Flow<List<MyListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToList(item: MyListEntity)

    @Query("DELETE FROM my_list WHERE movieId = :movieId")
    suspend fun removeFromList(movieId: Int)

    @Query("SELECT COUNT(*) FROM my_list WHERE movieId = :movieId")
    suspend fun isInMyList(movieId: Int): Int

    @Query("DELETE FROM my_list")
    suspend fun clearAll()
}

// ── Database ──────────────────────────────────────────────────────────

@Database(
    entities = [
        CachedMovieEntity::class,
        WatchHistoryEntity::class,
        MyListEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OttDatabase : RoomDatabase() {
    abstract fun movieCacheDao(): MovieCacheDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun myListDao(): MyListDao

    companion object {
        @Volatile private var INSTANCE: OttDatabase? = null

        fun getInstance(context: Context): OttDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    OttDatabase::class.java,
                    "ott_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
