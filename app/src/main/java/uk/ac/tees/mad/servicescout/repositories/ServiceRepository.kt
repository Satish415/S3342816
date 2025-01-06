package uk.ac.tees.mad.servicescout.repositories

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.servicescout.ui.theme.screens.Service

class ServiceRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun fetchServices(): Result<List<Service>> {
        return try {
            val querySnapshot = firestore.collection("services").get().await()
            val services = querySnapshot.documents.mapNotNull { document ->
                Service(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    category = document.getString("category") ?: "",
                    imageUrl = document.getString("imageUrl") ?: "",
                    location = document.getString("location") ?: ""
                )
            }
            Result.success(services)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun uploadService(service: Service): Result<Unit> {
        return try {
            firestore.collection("services").add(service).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getServiceById(serviceId: String): Result<Service> {
        return try {
            val document = firestore.collection("services").document(serviceId).get().await()
            Result.success(document.toObject(Service::class.java)?.copy(id = serviceId) ?: Service())
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun uploadImage(uri: Uri?): Result<String> {
        return try {
            uri?.let {
                val storageRef = storage.reference
                val imageRef = storageRef.child("images/${uri.lastPathSegment}")
                val uploadTask = imageRef.putFile(uri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } ?: Result.success("")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

}