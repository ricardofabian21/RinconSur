
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rinconsur.viewmodel.UsuarioViewModel

@Composable
fun RegistroScreen(
    usuarioViewModel: UsuarioViewModel,
    onVolverLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RINCÓN SUR",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "TIENDA DEPARTAMENTAL",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "CREAR CUENTA",
            style = MaterialTheme.typography.titleLarge
        )

        Text("Completa tus datos para registrarte")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val nombreLimpio = nombre.trim()
                val correoLimpio = correo.trim()

                when {
                    nombreLimpio.isEmpty() ||
                            correoLimpio.isEmpty() ||
                            contrasena.isEmpty() ||
                            confirmarContrasena.isEmpty() -> {
                        mensajeError = "Complete todos los campos"
                    }

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correoLimpio).matches() -> {
                        mensajeError = "Correo electrónico inválido"
                    }

                    contrasena != confirmarContrasena -> {
                        mensajeError = "Las contraseñas no coinciden"
                    }

                    else -> {
                        mensajeError = ""
                        cargando = true

                        usuarioViewModel.registrarUsuario(
                            nombreLimpio,
                            correoLimpio,
                            contrasena
                        ) { registrado ->
                            cargando = false

                            if (registrado) {
                                onVolverLogin()
                            } else {
                                mensajeError = "Este correo ya está registrado"
                            }
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargando) "REGISTRANDO..." else "REGISTRARSE"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onVolverLogin,
            enabled = !cargando
        ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }
    }
}
