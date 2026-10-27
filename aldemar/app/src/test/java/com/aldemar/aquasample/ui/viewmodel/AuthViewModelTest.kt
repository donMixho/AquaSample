package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private class FakeAuth : AuthRepository {
        var account = AuthAccount("uid-test", "Persona de prueba", "test@example.invalid", UserRole.OPERADOR)
        var restored: AuthAccount? = null
        var failure: Exception? = null
        var pending: CompletableDeferred<AuthAccount>? = null
        var loginCalls = 0
        var registerCalls = 0
        var signOutCalls = 0
        var capturedEmail = ""
        override suspend fun restore() = restored
        override suspend fun login(email: String, password: String): AuthAccount {
            loginCalls++
            capturedEmail = email
            failure?.let { throw it }
            return pending?.await() ?: account
        }
        override suspend fun register(email: String, password: String, name: String): AuthAccount {
            registerCalls++
            return account.copy(name = name)
        }
        override fun logout() { signOutCalls++ }
    }
    private fun vm(repo: FakeAuth, demo: Boolean = true, handle: SavedStateHandle = SavedStateHandle()): AuthViewModel =
        AuthViewModel(handle, repo, demo).also { store.put("vm", it) }

    @Test fun invalidCredentialsNeverReachProvider() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = vm(repo); runCurrent()
        vm.loginWithFirebase("not-an-email", "secret")
        vm.registerWithFirebase("test@example.invalid", "123", "Nombre")
        assertEquals(0, repo.loginCalls); assertEquals(0, repo.registerCalls)
        assertFalse(vm.uiState.value.isLoggedIn); assertNotNull(vm.uiState.value.errorMessage)
    }
    @Test fun loginUsesVerifiedIdentityAndTrimsEmail() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = vm(repo); runCurrent()
        vm.loginWithFirebase(" test@example.invalid ", "test-pass"); runCurrent()
        assertEquals("test@example.invalid", repo.capturedEmail)
        assertEquals("uid-test", vm.uiState.value.userId)
        assertEquals(UserRole.OPERADOR, vm.uiState.value.currentRole)
        assertFalse(vm.uiState.value.isDemo)
    }
    @Test fun supervisorRoleComesFromProviderAndRestores() = runTest(dispatcher) {
        val repo = FakeAuth(); repo.restored = repo.account.copy(role = UserRole.SUPERVISOR)
        val vm = vm(repo); runCurrent()
        assertTrue(vm.uiState.value.isLoggedIn)
        assertEquals(UserRole.SUPERVISOR, vm.uiState.value.currentRole)
    }
    @Test fun doubleClickSendsOnlyOneLoginAndBlocksDemoWhilePending() = runTest(dispatcher) {
        val repo = FakeAuth(); repo.pending = CompletableDeferred()
        val vm = vm(repo); runCurrent()
        vm.loginWithFirebase("test@example.invalid", "test-pass")
        vm.loginWithFirebase("test@example.invalid", "test-pass")
        vm.loginDemo(UserRole.SUPERVISOR, "Demo")
        runCurrent()
        assertEquals(1, repo.loginCalls); assertTrue(vm.uiState.value.isLoading)
        repo.pending!!.complete(repo.account); runCurrent()
        assertEquals(UserRole.OPERADOR, vm.uiState.value.currentRole)
        assertFalse(vm.uiState.value.isDemo)
    }
    @Test fun failedLoginClearsSessionAndCanRetry() = runTest(dispatcher) {
        val repo = FakeAuth(); repo.failure = IllegalStateException("provider detail must stay private")
        val vm = vm(repo); runCurrent()
        vm.loginWithFirebase("test@example.invalid", "test-pass"); runCurrent()
        assertFalse(vm.uiState.value.isLoggedIn); assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.errorMessage!!.contains("provider detail"))
        assertEquals(1, repo.signOutCalls)
        repo.failure = null
        vm.loginWithFirebase("test@example.invalid", "test-pass"); runCurrent()
        assertTrue(vm.uiState.value.isLoggedIn)
    }
    @Test fun releaseCannotEnterDemoOrRestoreSavedDemo() = runTest(dispatcher) {
        val vm = vm(FakeAuth(), demo = false, handle = SavedStateHandle(mapOf("demo" to true, "demoRole" to "SUPERVISOR")))
        runCurrent(); vm.loginDemo(UserRole.SUPERVISOR, "Demo")
        assertFalse(vm.uiState.value.isLoggedIn); assertFalse(vm.uiState.value.isDemo)
    }
    @Test fun demoStateRestoresAndLogoutRemovesIt() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val repo = FakeAuth(); val first = vm(repo, handle = handle); runCurrent()
        first.loginDemo(UserRole.SUPERVISOR, "Demo")
        val restored = vm(repo, handle = handle)
        assertTrue(restored.uiState.value.isDemo)
        assertEquals(UserRole.SUPERVISOR, restored.uiState.value.currentRole)
        restored.logout()
        assertFalse(restored.uiState.value.isLoggedIn)
        assertEquals(false, handle.get<Boolean>("demo"))
        assertNull(restored.uiState.value.userId)
    }
    @Test fun registrationRequiresNameAndUsesProviderRole() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = vm(repo); runCurrent()
        vm.registerWithFirebase("test@example.invalid", "test-pass", " ")
        assertEquals(0, repo.registerCalls)
        vm.registerWithFirebase("test@example.invalid", "test-pass", " Persona "); runCurrent()
        assertEquals(1, repo.registerCalls)
        assertEquals("Persona", vm.uiState.value.currentUserName)
        assertEquals(UserRole.OPERADOR, vm.uiState.value.currentRole)
    }
    @Test fun onlyExactTrustedSupervisorClaimGrantsPermission() {
        assertEquals(UserRole.SUPERVISOR, RolePolicy.fromClaims(mapOf("role" to "SUPERVISOR")))
        for (claims in listOf(emptyMap(), mapOf("role" to "supervisor"), mapOf("role" to true), mapOf("admin" to true))) {
            assertEquals(UserRole.OPERADOR, RolePolicy.fromClaims(claims))
        }
    }
}
