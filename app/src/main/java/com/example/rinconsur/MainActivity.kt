package com.example.rinconsur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.rinconsur.database.AppDatabase
import com.example.rinconsur.database.MIGRATION_1_2
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.view.*
import com.example.rinconsur.viewmodel.ProbadorViewModel
import com.example.rinconsur.viewmodel.UsuarioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "rinconsur_db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

        val usuarioDao = database.usuarioDao()

        CoroutineScope(Dispatchers.IO).launch {
            if (usuarioDao.obtenerUsuarios().isEmpty()) {
                usuarioDao.agregarUsuario(
                    Usuario(
                        nombre = "Administrador",
                        correo = "admin@rinconsur.cl",
                        contrasena = "admin123",
                        rol = "Administrador"
                    )
                )

                usuarioDao.agregarUsuario(
                    Usuario(
                        nombre = "Personal Tienda",
                        correo = "personal@rinconsur.cl",
                        contrasena = "personal123",
                        rol = "Personal"
                    )
                )
            }
        }

        setContent {
            RinconSurTheme {

                val usuarioViewModel: UsuarioViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(
                            modelClass: Class<T>
                        ): T {
                            return UsuarioViewModel(usuarioDao) as T
                        }
                    }
                )

                val probadorViewModel: ProbadorViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(
                            modelClass: Class<T>
                        ): T {
                            return ProbadorViewModel(
                                application,
                                database
                            ) as T
                        }
                    }
                )

                var pantalla by remember { mutableStateOf("Login") }
                var usuarioEditar by remember { mutableStateOf<Usuario?>(null) }
                var volverGaleria by remember { mutableStateOf("Admin") }

                // ANIMACIÓN DE TRANSICIÓN DE PANTALLAS
                AnimatedContent(
                    targetState = pantalla,
                    transitionSpec = {
                        // Si volvemos hacia Login o presionamos Volver
                        val esVolver = targetState == "Login" ||
                                (initialState == "Galeria" && (targetState == "Admin" || targetState == "Personal")) ||
                                (initialState == "ProbadoresAdmin" && targetState == "Admin") ||
                                (initialState == "ProbadoresPersonal" && targetState == "Personal") ||
                                (initialState == "Usuarios" && targetState == "Admin") ||
                                (initialState == "Formulario" && targetState == "Usuarios") ||
                                (initialState == "Registro" && targetState == "Login")

                        if (esVolver) {
                            (slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(tween(300))).togetherWith(
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                            )
                        } else {
                            (slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(tween(300))).togetherWith(
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                            )
                        }
                    },
                    label = "PantallaTransition"
                ) { targetPantalla ->

                    when (targetPantalla) {

                        "Registro" -> RegistroScreen(
                            usuarioViewModel = usuarioViewModel,
                            onVolverLogin = {
                                pantalla = "Login"
                            }
                        )

                        "Admin" -> AdminScreen(
                            probadorViewModel = probadorViewModel,
                            onCerrarSesion = {
                                pantalla = "Login"
                            },
                            onGestionarUsuarios = {
                                pantalla = "Usuarios"
                            },
                            onGestionarProbadores = {
                                pantalla = "ProbadoresAdmin"
                            },
                            onVerGaleria = {
                                volverGaleria = "Admin"
                                pantalla = "Galeria"
                            }
                        )

                        "Personal" -> PersonalScreen(
                            probadorViewModel = probadorViewModel,
                            onCerrarSesion = {
                                pantalla = "Login"
                            },
                            onVerProbadores = {
                                pantalla = "ProbadoresPersonal"
                            },
                            onVerGaleria = {
                                volverGaleria = "Personal"
                                pantalla = "Galeria"
                            }
                        )

                        "ProbadoresAdmin" -> ProbadoresScreen(
                            probadorViewModel = probadorViewModel,
                            esAdministrador = true,
                            onVolver = {
                                pantalla = "Admin"
                            }
                        )

                        "ProbadoresPersonal" -> ProbadoresScreen(
                            probadorViewModel = probadorViewModel,
                            esAdministrador = false,
                            onVolver = {
                                pantalla = "Personal"
                            }
                        )

                        "Galeria" -> GaleriaScreen(
                            probadorViewModel = probadorViewModel,
                            esAdministrador = volverGaleria == "Admin",
                            onVolver = {
                                pantalla = volverGaleria
                            }
                        )

                        "Usuarios" -> UsuariosScreen(
                            usuarioViewModel = usuarioViewModel,
                            onAgregar = {
                                usuarioEditar = null
                                pantalla = "Formulario"
                            },
                            onEditar = { id ->
                                usuarioEditar = usuarioViewModel.usuarios.find {
                                    it.id == id
                                }
                                pantalla = "Formulario"
                            },
                            onVolver = {
                                pantalla = "Admin"
                            }
                        )

                        "Formulario" -> FormularioUsuarioScreen(
                            usuario = usuarioEditar,
                            onGuardar = { usuario ->
                                if (usuarioEditar == null) {
                                    usuarioViewModel.agregarUsuario(usuario)
                                } else {
                                    usuarioViewModel.actualizarUsuario(usuario)
                                }
                                pantalla = "Usuarios"
                            },
                            onCancelar = {
                                pantalla = "Usuarios"
                            }
                        )

                        else -> LoginScreen(
                            usuarioViewModel = usuarioViewModel,
                            onLoginExitoso = { rol ->
                                pantalla = if (rol == "Administrador") {
                                    "Admin"
                                } else {
                                    "Personal"
                                }
                            },
                            onRegistrarse = {
                                pantalla = "Registro"
                            }
                        )
                    }
                }
            }
        }
    }
}