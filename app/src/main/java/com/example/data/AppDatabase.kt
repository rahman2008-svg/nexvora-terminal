package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE path = :path LIMIT 1")
    suspend fun getProjectByPath(path: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Recent Files
    @Query("SELECT * FROM recent_files ORDER BY lastOpenedTimestamp DESC LIMIT 20")
    fun getRecentFiles(): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteRecentFile(path: String)

    // Terminal Sessions
    @Query("SELECT * FROM terminal_sessions ORDER BY lastActiveTimestamp DESC")
    fun getAllTerminalSessions(): Flow<List<TerminalSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminalSession(session: TerminalSessionEntity)

    @Query("DELETE FROM terminal_sessions WHERE id = :id")
    suspend fun deleteTerminalSession(id: String)

    // Packages
    @Query("SELECT * FROM packages WHERE installed = 1 ORDER BY name ASC")
    fun getInstalledPackages(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE type = :type ORDER BY name ASC")
    fun getPackagesByType(type: String): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE type = :type ORDER BY name ASC")
    suspend fun getPackagesByTypeList(type: String): List<PackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity)

    @Query("DELETE FROM packages WHERE id = :id")
    suspend fun deletePackage(id: String)
}

@Database(
    entities = [
        ProjectEntity::class,
        RecentFileEntity::class,
        TerminalSessionEntity::class,
        PackageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexvora_terminal.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
