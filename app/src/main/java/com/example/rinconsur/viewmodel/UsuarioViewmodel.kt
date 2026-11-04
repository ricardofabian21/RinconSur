
package com.example.rinconsur.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.database.UsuarioDao
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsuarioViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    val usuarios = mutableStateListOf<Usuario>()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            val lista = withContext(Dispatchers.IO) {
                usuarioDao.obtenerUsuarios()
            }
            usuarios.clear()
            usuarios.addAll(lista)
        }
    }

    fun agregarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                usuarioDao.agregarUsuario(usuario)
            }
            cargarUsuarios()
        }
    }

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                usuarioDao.actualizarUsuario(usuario)
            }
            cargarUsuarios()
        }
    }

    fun eliminarUsuario(id: Int) {
        viewModelScope.launch {
            val usuario = usuarios.find { it.id == id }

            if (usuario != null) {
                withContext(Dispatchers.IO) {
                    usuarioDao.eliminarUsuario(usuario)
                }
                cargarUsuarios()
            }
        }
    }

    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onResultado: (Usuario?) -> Unit
    ) {
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                usuarioDao.iniciarSesion(correo, contrasena)
            }
            onResultado(usuario)
        }
    }

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val registrado = withContext(Dispatchers.IO) {
                val correoLimpio = correo.trim().lowercase()

                val usuarioExistente =
                    usuarioDao.buscarPorCorreo(correoLimpio)

                if (usuarioExistente != null) {
                    false
                } else {
                    val nuevoUsuario = Usuario(
                        nombre = nombre.trim(),
                        correo = correoLimpio,
                        contrasena = contrasena,
                        rol = "Personal"
                    )

                    usuarioDao.agregarUsuario(nuevoUsuario)
                    true
                }
            }

            if (registrado) {
                cargarUsuarios()
            }

            onResultado(registrado)
        }
    }
}
