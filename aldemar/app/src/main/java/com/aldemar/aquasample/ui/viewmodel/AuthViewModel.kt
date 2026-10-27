package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.BuildConfig
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.AuthAccount
import com.aldemar.aquasample.data.repository.AuthRepository
import com.aldemar.aquasample.data.repository.FirebaseAuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentRole: UserRole = UserRole.OPERADOR,
    val currentUserName: String = "",
    val userEmail: String? = null,
    val userId: String? = null,
    val isDemo: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val savedState: SavedStateHandle,
    private val repository: AuthRepository,
    private val demoEnabled: Boolean
) : ViewModel() {
    constructor(savedState: SavedStateHandle) : this(savedState, FirebaseAuthRepository(), BuildConfig.DEBUG)

    private val _uiState = MutableStateFlow(AuthUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        if (demoEnabled && savedState.get<Boolean>("demo") == true) {
            _uiState.value = AuthUiState(isLoggedIn = true, isDemo = true,
                currentUserName = savedState["demoName"] ?: "Demostración",
                currentRole = if (savedState.get<String>("demoRole") == "SUPERVISOR") UserRole.SUPERVISOR else UserRole.OPERADOR)
        } else viewModelScope.launch {
            try {
                _uiState.value = repository.restore()?.toState() ?: AuthUiState()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorMessage = "No se pudo recuperar la sesión. Inicia sesión nuevamente.")
            }
        }
    }

    fun loginWithFirebase(email: String, pass: String) = authenticate(email, pass, null, false)
    fun registerWithFirebase(email: String, pass: String, customName: String?) = authenticate(email, pass, customName, true)

    private fun authenticate(email: String, pass: String, name: String?, register: Boolean) {
        if (_uiState.value.isLoading || _uiState.value.isLoggedIn) return
        val error = AuthValidation.validate(email, pass, name, register)
        if (error != null) { _uiState.value = _uiState.value.copy(errorMessage = error); return }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val account = if (register) repository.register(email.trim(), pass, name!!.trim())
                    else repository.login(email.trim(), pass)
                savedState["demo"] = false
                _uiState.value = account.toState()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                runCatching { repository.logout() }
                val message = when (e) {
                    is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa Internet y reintenta."
                    is FirebaseAuthException -> when (e.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya tiene una cuenta. Intenta iniciar sesión."
                        "ERROR_OPERATION_NOT_ALLOWED" -> "El acceso por correo no está habilitado. Contacta al administrador."
                        "ERROR_WEAK_PASSWORD" -> "La contraseña no cumple los requisitos del servicio."
                        else -> "No se pudo iniciar sesión. Revisa los datos y el estado de tu cuenta."
                    }
                    else -> "No se pudo completar el acceso. Reintenta o contacta al administrador."
                }
                _uiState.value = AuthUiState(errorMessage = message)
            }
        }
    }

    fun loginDemo(role: UserRole, customName: String?) {
        if (!demoEnabled || _uiState.value.isLoading || _uiState.value.isLoggedIn) return
        val name = customName?.trim().orEmpty().ifBlank { "Demostración" }.take(100)
        runCatching { repository.logout() }
        savedState["demo"] = true
        savedState["demoName"] = name
        savedState["demoRole"] = role.name
        _uiState.value = AuthUiState(isLoggedIn = true, isDemo = true, currentRole = role, currentUserName = name)
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun logout() {
        if (_uiState.value.isLoading) return
        runCatching { repository.logout() }
        savedState["demo"] = false
        savedState.remove<String>("demoName")
        savedState.remove<String>("demoRole")
        _uiState.value = AuthUiState()
    }

    private fun AuthAccount.toState() = AuthUiState(isLoggedIn = true, currentRole = role,
        currentUserName = name, userEmail = email, userId = uid)
}
