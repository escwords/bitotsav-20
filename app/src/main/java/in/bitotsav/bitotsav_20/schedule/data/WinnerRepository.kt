package `in`.bitotsav.bitotsav_20.schedule.data

import androidx.annotation.WorkerThread

class WinnerRepository(private val winnerDao: WinnerDao) {

    @WorkerThread
    suspend fun insertWinner(winner: Winner) = winnerDao.insert(winner)

    @WorkerThread
    suspend fun updateWinner(winner: Winner) = winnerDao.update(winner)

    @WorkerThread
    suspend fun deleteWinner(winner: Winner) = winnerDao.delete(winner)

    @WorkerThread
    suspend fun deleteAllWinners() = winnerDao.deleteAll()

    @WorkerThread
    suspend fun getWinner(id: Int) = winnerDao.getWinners(id)
}