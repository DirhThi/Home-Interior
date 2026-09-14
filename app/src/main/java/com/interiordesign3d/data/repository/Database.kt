package com.interiordesign3d.data.repository

import androidx.room.*
import com.interiordesign3d.data.models.*
import kotlinx.coroutines.flow.Flow

// ─── Type Converters ──────────────────────────────────────────────────────────

class Converters {
    @TypeConverter fun fromStringList(value: List<String>): String =
        value.joinToString(",")
    @TypeConverter fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(",")
    @TypeConverter fun fromFloorMaterial(value: FloorMaterial): String = value.name
    @TypeConverter fun toFloorMaterial(value: String): FloorMaterial =
        FloorMaterial.valueOf(value)
}

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

@Database(
    entities = [DesignRoom::class, PlacedFurniture::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                    // Dev-only app with a single user: the schema is always v1 and a change wipes it.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
