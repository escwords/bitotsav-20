package `in`.bitotsav.bitotsav_20.db

import `in`.bitotsav.bitotsav_20.dao.EventDao
import `in`.bitotsav.bitotsav_20.dao.WinnerDao
import `in`.bitotsav.bitotsav_20.entity.Event
import `in`.bitotsav.bitotsav_20.entity.Winner
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Event::class, Winner::class], version = 1, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao() : EventDao
    abstract fun winnerDao() : WinnerDao

    companion object {
        private const val TAG = "EventDatabase"

        @Volatile private var instance : EventDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            EventDatabase::class.java,
            "event_db.db"
        ).build()
    }
}