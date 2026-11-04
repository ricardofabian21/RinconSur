
package com.example.rinconsur.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidencias")
data class Evidencia(
    @PrimaryKey
    val id: Long,
    val idProbador: Int,
    val numeroProbador: Int,
    val rutaFoto: String,
    val fecha: Long
)
