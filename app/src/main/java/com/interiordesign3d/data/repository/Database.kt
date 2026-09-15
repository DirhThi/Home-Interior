package com.interiordesign3d.data.repository

import androidx.room.*
import com.interiordesign3d.data.models.*
import kotlinx.coroutines.flow.Flow

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
    fun getAllRooms(): Flow<List<DesignRoom>>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: String): DesignRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: DesignRoom)

    @Update
    suspend fun updateRoom(room: DesignRoom)

    @Delete
    suspend fun deleteRoom(room: DesignRoom)
}

@Dao
interface PlacedFurnitureDao {
    @Query("SELECT * FROM placed_furniture WHERE roomId = :roomId")
    fun getFurnitureForRoom(roomId: String): Flow<List<PlacedFurniture>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedFurniture(item: PlacedFurniture)

    @Update
    suspend fun updatePlacedFurniture(item: PlacedFurniture)

    @Delete
    suspend fun deletePlacedFurniture(item: PlacedFurniture)

    @Query("DELETE FROM placed_furniture WHERE roomId = :roomId")
    suspend fun clearRoomFurniture(roomId: String)
}

// ─── Database ─────────────────────────────────────────────────────────────────

/**
 * Bump [version] on EVERY schema change. `fallbackToDestructiveMigration` only runs when the version
 * moves — at an unchanged version Room compares a schema hash instead and throws "cannot verify the
 * data integrity", which crashes the app on launch for anyone holding an older database. The version
 * is a schema fingerprint here, not a migration count; there are still no Migration objects.
 */
@Database(
    entities = [DesignRoom::class, PlacedFurniture::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun placedFurnitureDao(): PlacedFurnitureDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interior_design_db"
                )
                    // Dev-only app with a single user: a schema change wipes rather than migrates.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
