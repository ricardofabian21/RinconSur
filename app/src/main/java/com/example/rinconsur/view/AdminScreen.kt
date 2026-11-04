
package com.example.rinconsur.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rinconsur.viewmodel.ProbadorViewModel
import kotlinx.coroutines.delay

@Composable
fun AdminScreen(
    probadorViewModel: ProbadorViewModel,
    onCerrarSesion: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onGestionarProbadores: () -> Unit,
    onVerGaleria: () -> Unit
) {
    val probadores = probadorViewModel.probadores

    val disponibles = probadores.count { it.estado == "Disponible" }
    val ocupados = probadores.count { it.estado == "Ocupado" }
    val revision = probadores.count { it.estado == "Pendiente de revisión" }
    val mantencion = probadores.count { it.estado == "En mantención" }

    var tiempoActual by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(Unit) {
        while (true) {
            tiempoActual = System.currentTimeMillis()
            delay(1000)
        }
    }

    val probadoresOcupados = probadores
        .filter {
            it.estado == "Ocupado" &&
                    it.inicioOcupacion != null
        }
        .sortedBy { it.inicioOcupacion }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PANEL ADMINISTRADOR")

        Spacer(modifier = Modifier.height(24.dp))

        Text("RESUMEN DE PROBADORES")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Disponibles: $disponibles")
        Text("Ocupados: $ocupados")
        Text("Pendientes de revisión: $revision")
        Text("En mantención: $mantencion")
        Text("Total de probadores: ${probadores.size}")

        Spacer(modifier = Modifier.height(24.dp))

        Text("PROBADORES CON MAYOR OCUPACIÓN")

        Spacer(modifier = Modifier.height(12.dp))

        if (probadoresOcupados.isEmpty()) {
            Text("No hay probadores ocupados")
        } else {
            probadoresOcupados.forEach { probador ->
                val minutos = (
                        (tiempoActual - probador.inicioOcupacion!!)
                            .coerceAtLeast(0L) / 60000
                        )

                Text(
                    "Probador ${probador.numero}: $minutos min"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGestionarUsuarios,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GESTIONAR USUARIOS")
        }

        Button(
            onClick = onGestionarProbadores,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GESTIONAR PROBADORES")
        }

        Button(
            onClick = onVerGaleria,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("VER GALERÍA DE EVIDENCIAS")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CERRAR SESIÓN")
        }
    }
}
