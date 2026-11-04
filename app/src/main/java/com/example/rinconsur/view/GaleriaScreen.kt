
package com.example.rinconsur.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.Evidencia
import com.example.rinconsur.viewmodel.ProbadorViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GaleriaScreen(
    probadorViewModel: ProbadorViewModel,
    esAdministrador: Boolean,
    onVolver: () -> Unit
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    val coroutineScope = rememberCoroutineScope()

    var evidenciaEliminar by remember {
        mutableStateOf<Evidencia?>(null)
    }

    var mensajeError by remember {
        mutableStateOf(false)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("GALERÍA DE EVIDENCIAS")

        Button(onClick = onVolver) {
            Text("VOLVER")
        }

        if (mensajeError) {
            Text("No se pudo eliminar la fotografía")
        }

        if (probadorViewModel.evidencias.isEmpty()) {
            Text("Todavía no hay fotografías registradas")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    probadorViewModel.evidencias.sortedByDescending {
                        it.fecha
                    },
                    key = { it.id }
                ) { evidencia ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Probador ${evidencia.numeroProbador}")

                            Text(
                                "Fecha: ${
                                    formatoFecha.format(
                                        Date(evidencia.fecha)
                                    )
                                }"
                            )

                            val fotografia = remember(evidencia.rutaFoto) {
                                probadorViewModel.obtenerFotoHistorial(
                                    evidencia.rutaFoto
                                )
                            }

                            if (fotografia != null) {
                                Image(
                                    bitmap = fotografia.asImageBitmap(),
                                    contentDescription = "Evidencia del probador ${evidencia.numeroProbador}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .padding(top = 8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text("Fotografía no disponible")
                            }

                            if (esAdministrador) {
                                Button(
                                    onClick = {
                                        evidenciaEliminar = evidencia
                                        mensajeError = false
                                    },
                                    enabled = !eliminando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("ELIMINAR FOTO")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (evidenciaEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    evidenciaEliminar = null
                }
            },
            title = {
                Text("Eliminar fotografía")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar esta fotografía? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val evidencia = evidenciaEliminar

                        if (evidencia != null && !eliminando) {
                            eliminando = true
                            mensajeError = false

                            coroutineScope.launch {
                                try {
                                    val eliminada =
                                        probadorViewModel.eliminarEvidencia(
                                            evidencia.rutaFoto
                                        )

                                    mensajeError = !eliminada
                                } catch (e: Exception) {
                                    mensajeError = true
                                } finally {
                                    eliminando = false
                                    evidenciaEliminar = null
                                }
                            }
                        }
                    },
                    enabled = !eliminando
                ) {
                    Text(
                        if (eliminando) {
                            "ELIMINANDO..."
                        } else {
                            "ELIMINAR"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        evidenciaEliminar = null
                    },
                    enabled = !eliminando
                ) {
                    Text("CANCELAR")
                }
            }
        )
    }
}
