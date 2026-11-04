package com.example.rinconsur.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rinconsur.model.Evidencia

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencias ORDER BY fecha DESC")
    suspend fun obtenerEvidencias(): List<Evidencia>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEvidencia(evidencia: Evidencia)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEvidencias(evidencias: List<Evidencia>)

    @Delete
    suspend fun eliminarEvidencia(evidencia: Evidencia)
}
