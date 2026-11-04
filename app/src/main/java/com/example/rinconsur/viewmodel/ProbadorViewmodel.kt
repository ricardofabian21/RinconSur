
package com.example.rinconsur.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.model.Evidencia
import com.example.rinconsur.model.Probador
import com.example.rinconsur.storage.ProbadorStorage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProbadorViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = ProbadorStorage(application)

    val probadores = mutableStateListOf<Probador>()
    val evidencias = mutableStateListOf<Evidencia>()

    init {
        val datos = storage.obtener()

        if (datos.isEmpty()) {
            probadores.addAll(
                listOf(
                    Probador(1, 1, "Disponible"),
                    Probador(2, 2, "Disponible"),
                    Probador(3, 3, "Ocupado", System.currentTimeMillis()),
                    Probador(4, 4, "Pendiente de revisión"),
                    Probador(5, 5, "En mantención")
                )
            )
            guardarProbadores()
        } else {
            val lista = JSONArray(datos)

            for (i in 0 until lista.length()) {
                val item = lista.getJSONObject(i)

                probadores.add(
                    Probador(
                        id = item.getInt("id"),
                        numero = item.getInt("numero"),
                        estado = item.getString("estado"),
                        inicioOcupacion = if (item.isNull("inicio")) {
                            null
                        } else {
                            item.getLong("inicio")
                        },
                        fotoEvidencia = if (item.isNull("foto")) {
                            null
                        } else {
                            item.getString("foto")
                        }
                    )
                )
            }
        }

        evidencias.addAll(storage.obtenerHistorial())

        if (evidencias.isEmpty()) {
            probadores.forEach { probador ->
                val ruta = probador.fotoEvidencia

                if (ruta != null && File(ruta).exists()) {
                    val fecha = System.currentTimeMillis()

                    evidencias.add(
                        Evidencia(
                            id = fecha + evidencias.size,
                            idProbador = probador.id,
                            numeroProbador = probador.numero,
                            rutaFoto = ruta,
                            fecha = fecha
                        )
                    )
                }
            }

            if (evidencias.isNotEmpty()) {
                storage.guardarHistorial(evidencias)
            }
        }
    }

    private fun guardarProbadores() {
        val lista = JSONArray()

        probadores.forEach { probador ->
            val item = JSONObject()

            item.put("id", probador.id)
            item.put("numero", probador.numero)
            item.put("estado", probador.estado)
            item.put(
                "inicio",
                probador.inicioOcupacion ?: JSONObject.NULL
            )
            item.put(
                "foto",
                probador.fotoEvidencia ?: JSONObject.NULL
            )

            lista.put(item)
        }

        storage.guardar(lista.toString())
    }

    fun cambiarEstado(id: Int, nuevoEstado: String) {
        val indice = probadores.indexOfFirst { it.id == id }

        if (indice != -1) {
            val probador = probadores[indice]

            val inicio = if (nuevoEstado == "Ocupado") {
                probador.inicioOcupacion ?: System.currentTimeMillis()
            } else {
                null
            }

            probadores[indice] = probador.copy(
                estado = nuevoEstado,
                inicioOcupacion = inicio,
                fotoEvidencia = if (nuevoEstado == "Pendiente de revisión") {
                    probador.fotoEvidencia
                } else {
                    null
                }
            )

            guardarProbadores()
        }
    }

    fun guardarEvidencia(id: Int, imagen: Bitmap) {
        val indice = probadores.indexOfFirst { it.id == id }

        if (indice != -1) {
            val probador = probadores[indice]
            val ruta = storage.guardarFoto(id, imagen)
            val fecha = System.currentTimeMillis()

            evidencias.add(
                Evidencia(
                    id = fecha,
                    idProbador = probador.id,
                    numeroProbador = probador.numero,
                    rutaFoto = ruta,
                    fecha = fecha
                )
            )

            storage.guardarHistorial(evidencias)

            probadores[indice] = probador.copy(
                estado = "Pendiente de revisión",
                inicioOcupacion = null,
                fotoEvidencia = ruta
            )

            guardarProbadores()
        }
    }

    fun eliminarEvidencia(ruta: String): Boolean {
        val evidencia = evidencias.find { it.rutaFoto == ruta }
            ?: return false

        val eliminada = storage.eliminarFoto(evidencia.rutaFoto)

        if (!eliminada) {
            return false
        }

        evidencias.removeAll { it.rutaFoto == ruta }
        storage.guardarHistorial(evidencias)

        val indice = probadores.indexOfFirst {
            it.fotoEvidencia == ruta
        }

        if (indice != -1) {
            val probador = probadores[indice]

            probadores[indice] = probador.copy(
                fotoEvidencia = null
            )

            guardarProbadores()
        }

        return true
    }

    fun obtenerEvidencia(id: Int): Bitmap? {
        val probador = probadores.find { it.id == id }
        val ruta = probador?.fotoEvidencia ?: return null

        return storage.obtenerFoto(ruta)
    }

    fun obtenerFotoHistorial(ruta: String): Bitmap? {
        return storage.obtenerFoto(ruta)
    }

    fun agregarProbador() {
        val nuevoId = (probadores.maxOfOrNull { it.id } ?: 0) + 1
        val nuevoNumero = (probadores.maxOfOrNull { it.numero } ?: 0) + 1

        probadores.add(
            Probador(nuevoId, nuevoNumero, "Disponible")
        )

        guardarProbadores()
    }

    fun eliminarProbador(id: Int) {
        probadores.removeAll { it.id == id }
        guardarProbadores()
    }
}
