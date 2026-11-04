
package com.example.rinconsur.model

data class Probador(
    val id: Int,
    val numero: Int,
    val estado: String,
    val inicioOcupacion: Long? = null,
    val fotoEvidencia: String? = null
)
