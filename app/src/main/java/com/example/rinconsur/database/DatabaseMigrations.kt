
package com.example.rinconsur.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `probadores` (
                `id` INTEGER NOT NULL,
                `numero` INTEGER NOT NULL,
                `estado` TEXT NOT NULL,
                `inicioOcupacion` INTEGER,
                `fotoEvidencia` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `evidencias` (
                `id` INTEGER NOT NULL,
                `idProbador` INTEGER NOT NULL,
                `numeroProbador` INTEGER NOT NULL,
                `rutaFoto` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
