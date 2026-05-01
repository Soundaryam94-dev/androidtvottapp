package com.example.myottapp.data.repository

// ─────────────────────────────────────────────────────────────────────
//  AuthRepository.kt — Supabase stub (add real implementation later)
//  Place at: data/repository/AuthRepository.kt
// ─────────────────────────────────────────────────────────────────────
class AuthRepository {

    // Stub — returns success always until Supabase is added
    suspend fun signUp(email: String, password: String): Result<Unit> =
        Result.success(Unit)

    suspend fun signIn(email: String, password: String): Result<Unit> =
        Result.success(Unit)

    suspend fun signOut(): Result<Unit> =
        Result.success(Unit)

    fun getCurrentUser(): Any? = null

    fun isLoggedIn(): Boolean = false

    fun getCurrentEmail(): String = ""

    fun getCurrentUserId(): String? = null
}