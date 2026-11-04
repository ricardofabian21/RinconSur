package com.example.rinconsur.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.Evidencia
import com.example.rinconsur.viewmodel.ProbadorViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GaleriaScreen(
    probadorViewModel: ProbadorViewModel,
    esAdministrador: Boolean,
    onVolver: () -> Unit
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
    }

    val coroutineScope = rememberCoroutineScope()

    var evidenciaEliminar by remember { mutableStateOf<Evidencia?>(null) }
    var mensajeError by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }

    val evidenciasOrdenadas = remember(probadorViewModel.evidencias.toList()) {
        probadorViewModel.evidencias.sortedByDescending { it.fecha }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Galería de Evidencias",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver, enabled = !eliminando) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Banner de Error
            AnimatedVisibility(visible = mensajeError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Text(
                            text = "No se pudo eliminar la fotografía de la base de datos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (evidenciasOrdenadas.isEmpty()) {
                // Estado Vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Todavía no hay fotografías registradas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Las capturas guardadas desde los probadores aparecerán aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(evidenciasOrdenadas, key = { it.id }) { evidencia ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem( // <-- ANIMACIÓN DE REORDENAMIENTO
                                    fadeInSpec = tween(durationMillis = 300),
                                    fadeOutSpec = tween(durationMillis = 300),
                                    placementSpec = tween(durationMillis = 350)
                                ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Header: Probador + Marca de tiempo
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Probador #${evidencia.numeroProbador}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatoFecha.format(Date(evidencia.fecha)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Carga y Renderizado Seguro de la Imagen (Sin NPEs)
                                val fotografia = remember(evidencia.rutaFoto) {
                                    probadorViewModel.obtenerFotoHistorial(evidencia.rutaFoto)
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    if (fotografia != null && !fotografia.isRecycled) {
                                        Image(
                                            bitmap = fotografia.asImageBitmap(),
                                            contentDescription = "Evidencia probador ${evidencia.numeroProbador}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Fotografía no disponible",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Opción de Eliminación (Solo Admin)
                                if (esAdministrador) {
                                    OutlinedButton(
                                        onClick = {
                                            evidenciaEliminar = evidencia
                                            mensajeError = false
                                        },
                                        enabled = !eliminando,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ELIMINAR FOTO")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmación
    if (evidenciaEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) evidenciaEliminar = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar fotografía") },
            text = {
                Text("¿Estás seguro de que deseas eliminar esta fotografía de evidencia? Esta acción eliminará la entrada de Room y del almacenamiento.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val evidencia = evidenciaEliminar
                        if (evidencia != null && !eliminando) {
                            eliminando = true
                            mensajeError = false

                            coroutineScope.launch {
                                try {
                                    val eliminada = probadorViewModel.eliminarEvidencia(evidencia.rutaFoto)
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
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ELIMINANDO...")
                    } else {
                        Text("ELIMINAR")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { evidenciaEliminar = null },
                    enabled = !eliminando
                ) {
                    Text("CANCELAR")
                }
            }
        )
    }
}