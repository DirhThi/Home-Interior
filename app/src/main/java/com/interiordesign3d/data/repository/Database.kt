package com.interiordesign3d.data.repository

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

// ─── Migrations ───────────────────────────────────────────────────────────────

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rooms ADD COLUMN wallPointsJson TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE placed_furniture ADD COLUMN isWallMounted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE placed_furniture ADD COLUMN wallMountHeight REAL NOT NULL DEFAULT 120.0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rooms ADD COLUMN floorPlanJson TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE placed_furniture ADD COLUMN customWidthCm REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE placed_furniture ADD COLUMN customDepthCm REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE placed_furniture ADD COLUMN customHeightCm REAL NOT NULL DEFAULT 0.0")
    }
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [DesignRoom::class, PlacedFurniture::class],
    version = 5,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
    }
}
