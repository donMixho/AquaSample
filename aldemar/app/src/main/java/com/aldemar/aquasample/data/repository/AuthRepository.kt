package com.aldemar.aquasample.data.repository

import com.aldemar.aquasample.data.model.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AuthAccount(val uid: String, val name: String, val email: String?, val role: UserRole)

/** Only a trusted Firebase token can grant the supervisor role. */
object RolePolicy {
    fun fromClaims(claims: Map<String, Any>): UserRole =
        if (claims["role"] == "SUPERVISOR") UserRole.SUPERVISOR else UserRole.OPERADOR
}

interface AuthRepository {
    suspend fun restore(): AuthAccount?
    suspend fun login(email: String, password: String): AuthAccount
    suspend fun register(email: String, password: String, name: String): AuthAccount
    fun logout()
}

class FirebaseAuthRepository : AuthRepository {
    private val auth get() = FirebaseAuth.getInstance()

    private suspend fun account(forceRefresh: Boolean): AuthAccount {
        val user = auth.currentUser ?: error("Sesión no disponible")
        val token = user.getIdToken(forceRefresh).awaitResult()
        return AuthAccount(user.uid, user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@") ?: "Usuario", user.email, RolePolicy.fromClaims(token.claims))
    }

    override suspend fun restore(): AuthAccount? =
        if (auth.currentUser == null) null else account(false)

    override suspend fun login(email: String, password: String): AuthAccount {
        auth.signInWithEmailAndPassword(email, password).awaitResult()
        return account(true)
    }

    override suspend fun register(email: String, password: String, name: String): AuthAccount {
        auth.createUserWithEmailAndPassword(email, password).awaitResult()
        auth.currentUser!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).awaitResult()
        return account(true)
    }

    override fun logout() = auth.signOut()
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Solicitud cancelada"))
        }
    }
}
