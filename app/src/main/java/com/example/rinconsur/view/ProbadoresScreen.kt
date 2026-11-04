package com.example.rinconsur.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rinconsur.viewmodel.ProbadorViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProbadoresScreen(
    probadorViewModel: ProbadorViewModel,
    esAdministrador: Boolean,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var tiempoActual by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var fotografia by remember { mutableStateOf<Bitmap?>(null) }
    var probadorFotografiado by remember { mutableStateOf<Int?>(null) }
    var probadorPendiente by remember { mutableStateOf<Int?>(null) }

    // Launcher de Cámara
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { imagen ->
        if (imagen != null) {
            fotografia = imagen
            probadorFotografiado = probadorPendiente
        }
        probadorPendiente = null
    }

    // Launcher para solicitar Permiso de Cámara
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) {
            camaraLauncher.launch(null)
        } else {
            probadorPendiente = null
            Toast.makeText(
                context,
                "Se requiere permiso de cámara para tomar la evidencia",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launcher para Galería
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val entrada = context.contentResolver.openInputStream(uri)
                val imagen = entrada.use { BitmapFactory.decodeStream(it) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Probadores",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (esAdministrador) {
                ExtendedFloatingActionButton(
                    onClick = { probadorViewModel.agregarProbador() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nuevo Probador") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            items(
                items = probadorViewModel.probadores,
                key = { it.id } // KEY indispensable para animar elementos de la lista
            ) { probador ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem( // ANIMACIÓN DE REORDENAMIENTO Y ELIMINACIÓN DE TARJETAS
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
                        // Header con número de probador y Badge Animado de Estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Probador #${probador.numero}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Badge con animación de transición de colores
                            EstadoBadge(estado = probador.estado)
                        }

                        // Contador de Tiempo con animación de entrada y salida
                        AnimatedVisibility(
                            visible = probador.estado == "Ocupado" && probador.inicioOcupacion != null,
                            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                        ) {
                            val inicio = probador.inicioOcupacion ?: System.currentTimeMillis()
                            val minutos = ((tiempoActual - inicio).coerceAtLeast(0L) / 60000)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Tiempo transcurrido: $minutos min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Captura de Evidencias
                        Text(
                            text = "Evidencia de Ropa / Estado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón Cámara
                            OutlinedButton(
                                onClick = {
                                    fotografia = null
                                    probadorFotografiado = null
                                    probadorPendiente = probador.id

                                    val tienePermiso = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (tienePermiso) {
                                        camaraLauncher.launch(null)
                                    } else {
                                        permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cámara")
                            }

                            // Botón Galería
                            OutlinedButton(
                                onClick = {
                                    fotografia = null
                                    probadorFotografiado = null
                                    probadorPendiente = probador.id
                                    galeriaLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galería")
                            }
                        }

                        // Vista Previa desplegable con animación suave
                        AnimatedVisibility(
                            visible = probadorFotografiado == probador.id && fotografia != null,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 350)
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 350)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 350)
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 350)
                            )
                        ) {
                            val fotoPreview = fotografia
                            if (fotoPreview != null && !fotoPreview.isRecycled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Vista previa seleccionada:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Image(
                                            bitmap = fotoPreview.asImageBitmap(),
                                            contentDescription = "Vista previa",
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val imagenAGuardar = fotografia
                                            fotografia = null
                                            probadorFotografiado = null

                                            if (imagenAGuardar != null && !imagenAGuardar.isRecycled) {
                                                probadorViewModel.guardarEvidencia(probador.id, imagenAGuardar)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CONFIRMAR REPORTE")
                                    }
                                }
                            }
                        }

                        // Muestra de evidencia guardada
                        if (!probador.fotoEvidencia.isNullOrEmpty()) {
                            val evidenciaBitmap = remember(probador.fotoEvidencia) {
                                probadorViewModel.obtenerEvidencia(probador.id)
                            }

                            if (evidenciaBitmap != null && !evidenciaBitmap.isRecycled) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Evidencia Guardada:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Image(
                                            bitmap = evidenciaBitmap.asImageBitmap(),
                                            contentDescription = "Evidencia guardada",
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }

                        // Opciones de Administrador
                        if (esAdministrador) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Text(
                                text = "Cambiar Estado (Admin):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = probador.estado == "Disponible",
                                        onClick = { probadorViewModel.cambiarEstado(probador.id, "Disponible") },
                                        label = { Text("Libre") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = probador.estado == "Ocupado",
                                        onClick = { probadorViewModel.cambiarEstado(probador.id, "Ocupado") },
                                        label = { Text("Ocupado") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = probador.estado == "Pendiente de revisión",
                                        onClick = { probadorViewModel.cambiarEstado(probador.id, "Pendiente de revisión") },
                                        label = { Text("Revisión") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = probador.estado == "En mantención",
                                        onClick = { probadorViewModel.cambiarEstado(probador.id, "En mantención") },
                                        label = { Text("Mantención") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            TextButton(
                                onClick = { probadorViewModel.eliminarProbador(probador.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar Probador")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun EstadoBadge(estado: String) {
    val (targetBackgroundColor, targetTextColor) = when (estado) {
        "Disponible" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "Ocupado" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "Pendiente de revisión" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 400),
        label = "BadgeBackgroundColor"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 400),
        label = "BadgeTextColor"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = animatedBackgroundColor
    ) {
        Text(
            text = estado.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = animatedTextColor
        )
    }
}