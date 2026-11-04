
package com.example.rinconsur.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rinconsur.model.Probador

@Dao
interface ProbadorDao {

    @Query("SELECT * FROM probadores ORDER BY numero ASC")
    suspend fun obtenerProbadores(): List<Probador>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProbador(probador: Probador)

    @Update
    suspend fun actualizarProbador(probador: Probador)

    @Delete
    suspend fun eliminarProbador(probador: Probador)
}


