
package com.example.rinconsur.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "probadores")
data class Probador(
    @PrimaryKey
    val id: Int,
    val numero: Int,
    val estado: String,
    val inicioOcupacion: Long? = null,
    val fotoEvidencia: String? = null
)
