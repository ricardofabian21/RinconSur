
package com.example.rinconsur.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rinconsur.model.Usuario

@Database(
    entities = [Usuario::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
}
