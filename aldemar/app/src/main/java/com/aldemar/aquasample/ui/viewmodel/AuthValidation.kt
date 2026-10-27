package com.aldemar.aquasample.ui.viewmodel

object AuthValidation {
    fun validate(email: String, password: String, name: String?, register: Boolean): String? = when {
        email.isBlank() || password.isBlank() -> "Ingresa correo electrónico y contraseña."
        !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()) -> "Ingresa un correo electrónico válido."
        register && password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        register && name.isNullOrBlank() -> "Ingresa tu nombre."
        register && name!!.trim().length > 100 -> "El nombre admite hasta 100 caracteres."
        else -> null
    }
}
