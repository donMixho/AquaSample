package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.aldemar.aquasample.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado inmutable de la sesión del usuario.
 */
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentRole: UserRole = UserRole.OPERADOR,
    val currentUserName: String = UserRole.OPERADOR.defaultUser,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel encargado de la sesión y perfil activo con autenticación de Firebase.
 */
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Verificar si existe una sesión activa de Firebase al iniciar
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    currentRole = UserRole.OPERADOR,
                    currentUserName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: UserRole.OPERADOR.defaultUser,
                    userEmail = currentUser.email
                )
            }
        } catch (e: Exception) {
            // Firebase podría no estar completamente configurado en entornos de prueba
            e.printStackTrace()
        }
    }

    /**
     * Inicia sesión con Firebase Authentication (Email/Password).
     */
    fun loginWithFirebase(
        email: String,
        pass: String,
        role: UserRole,
        customName: String?,
        onSuccess: (UserRole, String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor ingrese correo electrónico y contraseña."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val displayName = firebaseUser?.displayName
                    val finalName = when {
                        !customName.isNullOrBlank() -> customName
                        !displayName.isNullOrBlank() -> displayName
                        else -> email.substringBefore("@")
                    }

                    _uiState.value = AuthUiState(
                        isLoggedIn = true,
                        currentRole = role,
                        currentUserName = finalName,
                        userEmail = firebaseUser?.email,
                        isLoading = false
                    )
                    onSuccess(role, finalName)
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Error al autenticar en Firebase."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
    }

    /**
     * Registra un nuevo usuario en Firebase Authentication.
     */
    fun registerWithFirebase(
        email: String,
        pass: String,
        role: UserRole,
        customName: String?,
        onSuccess: (UserRole, String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor ingrese correo electrónico y contraseña."
            )
            return
        }

        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "La contraseña debe tener al menos 6 caracteres."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val finalName = if (!customName.isNullOrBlank()) customName else email.substringBefore("@")

                    _uiState.value = AuthUiState(
                        isLoggedIn = true,
                        currentRole = role,
                        currentUserName = finalName,
                        userEmail = firebaseUser?.email,
                        isLoading = false
                    )
                    onSuccess(role, finalName)
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Error al registrar el usuario en Firebase."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
    }

    /**
     * Permite ingresar en modo local / demostración offline.
     */
    fun loginDemo(role: UserRole, customName: String?, onSuccess: (UserRole, String) -> Unit) {
        val userName = if (!customName.isNullOrBlank()) customName else role.defaultUser
        _uiState.value = AuthUiState(
            isLoggedIn = true,
            currentRole = role,
            currentUserName = userName,
            userEmail = "demo@aldemar.cl"
        )
        onSuccess(role, userName)
    }

    /**
     * Limpia el mensaje de error activo.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Cierra la sesión activa en Firebase y en la app.
     */
    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = AuthUiState(isLoggedIn = false)
    }
}
