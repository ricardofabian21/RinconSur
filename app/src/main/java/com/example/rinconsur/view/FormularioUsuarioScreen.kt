
package com.example.rinconsur.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.Usuario

@Composable
fun FormularioUsuarioScreen(
    usuario: Usuario?,
    onGuardar: (Usuario) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var correo by remember(usuario) { mutableStateOf(usuario?.correo ?: "") }
    var contrasena by remember(usuario) { mutableStateOf(usuario?.contrasena ?: "") }
    var rol by remember(usuario) { mutableStateOf(usuario?.rol ?: "Personal") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            if (usuario == null) "AGREGAR USUARIO" else "EDITAR USUARIO"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Rol del usuario")

        Row {
            Button(onClick = { rol = "Personal" }) {
                Text("Personal")
            }

            Button(onClick = { rol = "Administrador" }) {
                Text("Administrador")
            }
        }

        Text("Rol seleccionado: $rol")

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                    error = "Completa todos los campos"
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    error = "Correo electrónico inválido"
                } else {
                    error = ""
                    onGuardar(
                        Usuario(
                            id = usuario?.id ?: 0,
                            nombre = nombre,
                            correo = correo,
                            contrasena = contrasena,
                            rol = rol
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GUARDAR")
        }

        Button(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CANCELAR")
        }
    }
}
