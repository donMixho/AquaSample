package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.aldemar.aquasample.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado inmutable de la sesión del usuario.
 */
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentRole: UserRole = UserRole.OPERADOR,
    val currentUserName: String = UserRole.OPERADOR.defaultUser
)

/**
 * ViewModel encargado de la sesión y perfil activo (Operador vs. Supervisor).
 *
 * Propósito:
 * - Permite alternar fácilmente de perfil para demostrar ambos roles durante la defensa académica.
 * - Mantiene el nombre del operador que firmará cada muestra tomada en terreno.
 */
class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Inicia sesión con el rol y nombre de usuario seleccionado.
     */
    fun login(role: UserRole, customName: String? = null) {
        val userName = if (!customName.isNullOrBlank()) customName else role.defaultUser
        _uiState.value = AuthUiState(
            isLoggedIn = true,
            currentRole = role,
            currentUserName = userName
        )
    }

    /**
     * Cierra la sesión activa y retorna al estado inicial.
     */
    fun logout() {
        _uiState.value = AuthUiState(isLoggedIn = false)
    }
}
