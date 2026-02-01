package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zhd.socionet.data.model.Notice
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface NoticeRepository {
    suspend fun createNotice(notice: Notice): Result<Unit>
    suspend fun getNoticesForUser(userWing: String, isOwner: Boolean): List<Notice>
}

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NoticeRepository {

    override suspend fun createNotice(notice: Notice): Result<Unit> {
        return try {
            val docRef = firestore.collection("notices").document()
            val finalNotice = notice.copy(id = docRef.id)
            docRef.set(finalNotice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNoticesForUser(userWing: String, isOwner: Boolean): List<Notice> {
        return try {
            // In a real production app, we might use Algolia for complex filtering.
            // Here, we fetch the recent 50 notices and filter in memory for perfect accuracy
            // without complex composite indexes.
            val snapshot = firestore.collection("notices")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()

            val allNotices = snapshot.toObjects(Notice::class.java)

            // The "Real World" Filter Logic
            allNotices.filter { notice ->
                when (notice.targetAudience) {
                    "ALL" -> true
                    "OWNERS_ONLY" -> isOwner
                    else -> notice.targetAudience == "WING_$userWing" // e.g., matches "WING_A"
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}