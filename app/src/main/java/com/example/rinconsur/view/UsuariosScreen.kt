
package com.example.rinconsur.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.viewmodel.UsuarioViewModel

@Composable
fun UsuariosScreen(
    usuarioViewModel: UsuarioViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    onVolver: () -> Unit
) {
    var usuarioEliminar by remember { mutableStateOf<Usuario?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("GESTIÓN DE USUARIOS")

        Button(onClick = onVolver) {
            Text("VOLVER")
        }

        Button(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AGREGAR USUARIO")
        }

        LazyColumn {
            items(usuarioViewModel.usuarios) { usuario ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(usuario.nombre)
                    Text(usuario.correo)
                    Text("Rol: ${usuario.rol}")

                    Row {
                        Button(
                            onClick = { onEditar(usuario.id) }
                        ) {
                            Text("EDITAR")
                        }

                        if (usuario.correo != "admin@rinconsur.cl") {
                            Button(
                                onClick = { usuarioEliminar = usuario }
                            ) {
                                Text("ELIMINAR")
                            }
                        }
                    }
                }
            }
        }
    }

    if (usuarioEliminar != null) {
        AlertDialog(
            onDismissRequest = { usuarioEliminar = null },
            title = { Text("Eliminar usuario") },
            text = {
                Text("¿Seguro que deseas eliminar a ${usuarioEliminar?.nombre}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        usuarioEliminar?.let {
                            usuarioViewModel.eliminarUsuario(it.id)
                        }
                        usuarioEliminar = null
                    }
                ) {
                    Text("ELIMINAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioEliminar = null }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}
