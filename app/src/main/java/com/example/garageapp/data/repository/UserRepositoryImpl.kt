package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.User
import com.example.garageapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                trySend(
                    User(
                        userId = firebaseUser.uid,
                        shopId = "", // Load from Firestore if needed
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        role = "", // Load from Firestore if needed
                        createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
                    )
                )
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user
        if (firebaseUser != null) {
            Result.success(
                User(
                    userId = firebaseUser.uid,
                    shopId = "", // Load from Firestore if needed
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    role = "", // Load from Firestore if needed
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: 0L
                )
            )
        } else {
            Result.failure(Exception("Authentication failed"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
