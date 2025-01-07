package uk.ac.tees.mad.servicescout.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun registerUser(email: String, password: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))
            val user = User(id = userId, email = email, name = name)
            firestore.collection("users").document(userId).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID not found"))
            val userRef = firestore.collection("users").document(userId).get().await()
            if (userRef.exists()) {
                val user = userRef.toObject(User::class.java)
                    ?: return Result.failure(Exception("User not found"))
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): User {
        val doc = firestore.collection("users").document(auth.currentUser?.uid ?: "").get().await()
        return doc.toObject(User::class.java) ?: throw Exception("User not found")
    }

    suspend fun updateUserProfile(user: User, profileImageUri: Uri?) {
        var url = ""
        if (profileImageUri != null) {
            val ref = storage.reference.child("profile_images/${user.id}.jpg")
            ref.putFile(profileImageUri).await()
            url = ref.downloadUrl.await().toString()
        }
        firestore.collection("users").document(user.id).set(user.copy(profileImageUrl = url))
            .await()
    }

    fun logout() {
        auth.signOut()
    }
}

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val contact: String = ""
)