
package com.example.rinconsur.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.rinconsur.model.Evidencia
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ProbadorStorage(private val context: Context) {

    private val preferencias = context.getSharedPreferences(
        "probadores",
        Context.MODE_PRIVATE
    )

    fun migracionRoomCompletada(): Boolean {
        return preferencias.getBoolean(
            "migracion_room_v2_completada",
            false
        )
    }

    fun marcarMigracionRoomCompletada() {
        val guardado = preferencias.edit()
            .putBoolean("migracion_room_v2_completada", true)
            .commit()

        check(guardado) {
            "No se pudo registrar la migración a Room"
        }
    }

    fun guardar(datos: String) {
        preferencias.edit()
            .putString("lista", datos)
            .apply()
    }

    fun obtener(): String {
        return preferencias.getString("lista", "") ?: ""
    }

    fun guardarFoto(idProbador: Int, imagen: Bitmap): String {
        val archivo = File(
            context.filesDir,
            "evidencia_probador_${idProbador}_${System.nanoTime()}.jpg"
        )

        FileOutputStream(archivo).use { salida ->
            imagen.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                salida
            )
        }

        return archivo.absolutePath
    }

    fun obtenerFoto(ruta: String): Bitmap? {
        val archivo = File(ruta)

        if (!archivo.exists()) {
            return null
        }

        return BitmapFactory.decodeFile(archivo.absolutePath)
    }

    fun guardarHistorial(evidencias: List<Evidencia>) {
        val lista = JSONArray()

        evidencias.forEach { evidencia ->
            val item = JSONObject()

            item.put("id", evidencia.id)
            item.put("idProbador", evidencia.idProbador)
            item.put("numeroProbador", evidencia.numeroProbador)
            item.put("rutaFoto", evidencia.rutaFoto)
            item.put("fecha", evidencia.fecha)

            lista.put(item)
        }

        preferencias.edit()
            .putString("historial", lista.toString())
            .apply()
    }

    fun obtenerHistorial(): List<Evidencia> {
        val datos = preferencias.getString(
            "historial",
            "[]"
        ) ?: "[]"

        val lista = JSONArray(datos)
        val evidencias = mutableListOf<Evidencia>()

        for (i in 0 until lista.length()) {
            val item = lista.getJSONObject(i)

            evidencias.add(
                Evidencia(
                    id = item.getLong("id"),
                    idProbador = item.getInt("idProbador"),
                    numeroProbador = item.getInt("numeroProbador"),
                    rutaFoto = item.getString("rutaFoto"),
                    fecha = item.getLong("fecha")
                )
            )
        }

        return evidencias
    }

    fun eliminarFoto(ruta: String): Boolean {
        val archivo = File(
            context.filesDir,
            File(ruta).name
        )

        if (!archivo.exists()) {
            return true
        }

        return archivo.delete()
    }
}
