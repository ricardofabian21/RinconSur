
package com.example.rinconsur.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.rinconsur.database.AppDatabase
import com.example.rinconsur.model.Evidencia
import com.example.rinconsur.model.Probador
import com.example.rinconsur.storage.ProbadorStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class ProbadorViewModel(
    application: Application,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val storage = ProbadorStorage(application)
    private val probadorDao = database.probadorDao()
    private val evidenciaDao = database.evidenciaDao()

    val probadores = mutableStateListOf<Probador>()
    val evidencias = mutableStateListOf<Evidencia>()

    private var cargando = true

    init {
        viewModelScope.launch {
            cargarDatos()
        }
    }

    private suspend fun cargarDatos() {
        withContext(Dispatchers.IO) {
            if (!storage.migracionRoomCompletada()) {
                val antiguos = obtenerProbadoresAntiguos()
                val historial = storage.obtenerHistorial().toMutableList()

                if (historial.isEmpty()) {
                    antiguos.forEach { probador ->
                        val ruta = probador.fotoEvidencia

                        if (ruta != null && File(ruta).exists()) {
                            val fecha = System.currentTimeMillis()
                            val nuevoId = maxOf(
                                fecha,
                                (historial.maxOfOrNull { it.id } ?: 0L) + 1L
                            )

                            historial.add(
                                Evidencia(
                                    id = nuevoId,
                                    idProbador = probador.id,
                                    numeroProbador = probador.numero,
                                    rutaFoto = ruta,
                                    fecha = fecha
                                )
                            )
                        }
                    }
                }

                database.withTransaction {
                    val existentes = probadorDao.obtenerProbadores()
                    val evidenciasExistentes = evidenciaDao.obtenerEvidencias()

                    if (existentes.isEmpty()) {
                        val iniciales = if (antiguos.isNotEmpty()) {
                            antiguos
                        } else {
                            crearProbadoresIniciales()
                        }

                        probadorDao.guardarProbadores(iniciales)
                    }

                    if (evidenciasExistentes.isEmpty() && historial.isNotEmpty()) {
                        evidenciaDao.guardarEvidencias(historial)
                    }
                }

                storage.marcarMigracionRoomCompletada()
            }

            val listaProbadores = probadorDao.obtenerProbadores()
            val listaEvidencias = evidenciaDao.obtenerEvidencias()

            withContext(Dispatchers.Main) {
                probadores.clear()
                probadores.addAll(listaProbadores)

                evidencias.clear()
                evidencias.addAll(listaEvidencias)

                cargando = false
            }
        }
    }

    private fun obtenerProbadoresAntiguos(): List<Probador> {
        val datos = storage.obtener()

        if (datos.isEmpty()) {
            return emptyList()
        }

        val lista = JSONArray(datos)
        val resultado = mutableListOf<Probador>()

        for (i in 0 until lista.length()) {
            val item = lista.getJSONObject(i)

            resultado.add(
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

        return resultado
    }

    private fun crearProbadoresIniciales(): List<Probador> {
        return listOf(
            Probador(1, 1, "Disponible"),
            Probador(2, 2, "Disponible"),
            Probador(
                3,
                3,
                "Ocupado",
                System.currentTimeMillis()
            ),
            Probador(4, 4, "Pendiente de revisión"),
            Probador(5, 5, "En mantención")
        )
    }

    fun cambiarEstado(id: Int, nuevoEstado: String) {
        if (cargando) return

        val indice = probadores.indexOfFirst { it.id == id }

        if (indice != -1) {
            val probador = probadores[indice]

            val inicio = if (nuevoEstado == "Ocupado") {
                probador.inicioOcupacion ?: System.currentTimeMillis()
            } else {
                null
            }

            val actualizado = probador.copy(
                estado = nuevoEstado,
                inicioOcupacion = inicio,
                fotoEvidencia = if (nuevoEstado == "Pendiente de revisión") {
                    probador.fotoEvidencia
                } else {
                    null
                }
            )

            probadores[indice] = actualizado

            viewModelScope.launch(Dispatchers.IO) {
                probadorDao.guardarProbador(actualizado)
            }
        }
    }

    fun guardarEvidencia(id: Int, imagen: Bitmap) {
        if (cargando) return

        val indice = probadores.indexOfFirst { it.id == id }

        if (indice != -1) {
            val probador = probadores[indice]
            val ruta = storage.guardarFoto(id, imagen)
            val fecha = System.currentTimeMillis()

            val nuevoId = maxOf(
                fecha,
                (evidencias.maxOfOrNull { it.id } ?: 0L) + 1L
            )

            val evidencia = Evidencia(
                id = nuevoId,
                idProbador = probador.id,
                numeroProbador = probador.numero,
                rutaFoto = ruta,
                fecha = fecha
            )

            val actualizado = probador.copy(
                estado = "Pendiente de revisión",
                inicioOcupacion = null,
                fotoEvidencia = ruta
            )

            evidencias.add(0, evidencia)
            probadores[indice] = actualizado

            viewModelScope.launch(Dispatchers.IO) {
                database.withTransaction {
                    evidenciaDao.guardarEvidencia(evidencia)
                    probadorDao.guardarProbador(actualizado)
                }
            }
        }
    }

    fun eliminarEvidencia(ruta: String): Boolean {
        if (cargando) return false

        val evidencia = evidencias.find {
            it.rutaFoto == ruta
        } ?: return false

        val eliminada = storage.eliminarFoto(evidencia.rutaFoto)

        if (!eliminada) {
            return false
        }

        evidencias.removeAll { it.rutaFoto == ruta }

        val indice = probadores.indexOfFirst {
            it.fotoEvidencia == ruta
        }

        val actualizado = if (indice != -1) {
            probadores[indice].copy(fotoEvidencia = null).also {
                probadores[indice] = it
            }
        } else {
            null
        }

        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                evidenciaDao.eliminarEvidencia(evidencia)

                if (actualizado != null) {
                    probadorDao.guardarProbador(actualizado)
                }
            }
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
        if (cargando) return

        val nuevoId = (probadores.maxOfOrNull { it.id } ?: 0) + 1
        val nuevoNumero = (probadores.maxOfOrNull { it.numero } ?: 0) + 1

        val nuevo = Probador(
            nuevoId,
            nuevoNumero,
            "Disponible"
        )

        probadores.add(nuevo)

        viewModelScope.launch(Dispatchers.IO) {
            probadorDao.guardarProbador(nuevo)
        }
    }

    fun eliminarProbador(id: Int) {
        if (cargando) return

        val probador = probadores.find {
            it.id == id
        } ?: return

        probadores.remove(probador)

        viewModelScope.launch(Dispatchers.IO) {
            probadorDao.eliminarProbador(probador)
        }
    }
}
