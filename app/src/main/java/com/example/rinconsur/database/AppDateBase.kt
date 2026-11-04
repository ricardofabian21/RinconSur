
package com.example.rinconsur.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.model.Probador
import com.example.rinconsur.model.Evidencia

@Database(
    entities = [
        Usuario::class,
        Probador::class,
        Evidencia::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun probadorDao(): ProbadorDao

    abstract fun evidenciaDao(): EvidenciaDao
}
