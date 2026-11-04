
package com.example.rinconsur.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rinconsur.viewmodel.ProbadorViewModel
import kotlinx.coroutines.delay

@Composable
fun ProbadoresScreen(
    probadorViewModel: ProbadorViewModel,
    esAdministrador: Boolean,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var tiempoActual by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var fotografia by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var probadorFotografiado by remember {
        mutableStateOf<Int?>(null)
    }

    var probadorPendiente by remember {
        mutableStateOf<Int?>(null)
    }

    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { imagen ->
        if (imagen != null) {
            fotografia = imagen
            probadorFotografiado = probadorPendiente
        }

        probadorPendiente = null
    }

    val galeria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val entrada = context.contentResolver.openInputStream(uri)

                val imagen = entrada.use {
                    BitmapFactory.decodeStream(it)
                }

                if (imagen != null) {
                    fotografia = imagen
                    probadorFotografiado = probadorPendiente
                }
            } catch (e: Exception) {
                fotografia = null
            }
        }

        probadorPendiente = null
    }

    LaunchedEffect(Unit) {
        while (true) {
            tiempoActual = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("LISTA DE PROBADORES")

        Button(onClick = onVolver) {
            Text("VOLVER")
        }

        if (esAdministrador) {
            Button(
                onClick = {
                    probadorViewModel.agregarProbador()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("AGREGAR PROBADOR")
            }
        }

        LazyColumn {
            items(probadorViewModel.probadores) { probador ->
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Probador ${probador.numero}")
                    Text("Estado: ${probador.estado}")

                    if (
                        probador.estado == "Ocupado" &&
                        probador.inicioOcupacion != null
                    ) {
                        val minutos = (
                                (tiempoActual - probador.inicioOcupacion)
                                    .coerceAtLeast(0L) / 60000
                                )

                        Text("Tiempo ocupado: $minutos min")
                    }

                    Button(
                        onClick = {
                            fotografia = null
                            probadorFotografiado = null
                            probadorPendiente = probador.id
                            camara.launch(null)
                        }
                    ) {
                        Text("TOMAR FOTO DE ROPA")
                    }

                    Button(
                        onClick = {
                            fotografia = null
                            probadorFotografiado = null
                            probadorPendiente = probador.id
                            galeria.launch("image/*")
                        }
                    ) {
                        Text("SELECCIONAR DE GALERÍA")
                    }

                    if (
                        probadorFotografiado == probador.id &&
                        fotografia != null
                    ) {
                        Text("Vista previa de evidencia")

                        Image(
                            bitmap = fotografia!!.asImageBitmap(),
                            contentDescription = "Fotografía seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(vertical = 8.dp),
                            contentScale = ContentScale.Fit
                        )

                        Button(
                            onClick = {
                                val imagen = fotografia

                                if (imagen != null) {
                                    probadorViewModel.guardarEvidencia(
                                        probador.id,
                                        imagen
                                    )

                                    fotografia = null
                                    probadorFotografiado = null
                                }
                            }
                        ) {
                            Text("CONFIRMAR REPORTE")
                        }
                    }

                    if (probador.fotoEvidencia != null) {
                        val evidencia = remember(probador.fotoEvidencia) {
                            probadorViewModel.obtenerEvidencia(probador.id)
                        }

                        if (evidencia != null) {
                            Text("Evidencia guardada")

                            Image(
                                bitmap = evidencia.asImageBitmap(),
                                contentDescription = "Evidencia del probador",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    if (esAdministrador) {
                        Row {
                            Button(
                                onClick = {
                                    probadorViewModel.cambiarEstado(
                                        probador.id,
                                        "Disponible"
                                    )
                                }
                            ) {
                                Text("LIBRE")
                            }

                            Button(
                                onClick = {
                                    probadorViewModel.cambiarEstado(
                                        probador.id,
                                        "Ocupado"
                                    )
                                }
                            ) {
                                Text("OCUPADO")
                            }
                        }

                        Row {
                            Button(
                                onClick = {
                                    probadorViewModel.cambiarEstado(
                                        probador.id,
                                        "Pendiente de revisión"
                                    )
                                }
                            ) {
                                Text("REVISIÓN")
                            }

                            Button(
                                onClick = {
                                    probadorViewModel.cambiarEstado(
                                        probador.id,
                                        "En mantención"
                                    )
                                }
                            ) {
                                Text("MANTENCIÓN")
                            }
                        }

                        Button(
                            onClick = {
                                probadorViewModel.eliminarProbador(
                                    probador.id
                                )
                            }
                        ) {
                            Text("ELIMINAR")
                        }
                    }
                }
            }
        }
    }
}
