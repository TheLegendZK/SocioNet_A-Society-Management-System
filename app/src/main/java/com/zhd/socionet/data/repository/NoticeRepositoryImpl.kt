package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.local.NoticeEntity
import com.zhd.socionet.data.model.Notice
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface NoticeRepository {
    suspend fun createNotice(notice: Notice): Result<Unit>
    suspend fun getNoticesForUser(userWing: String, isOwner: Boolean): List<Notice>
}

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dao: NoticeDao,
) : NoticeRepository {
    override suspend fun createNotice(notice: Notice): Result<Unit> {
        return try {
            val docRef = firestore.collection("notices").document()
            val finalNotice = notice.copy(id = docRef.id)
            firestore.collection("notices").document(docRef.id).set(finalNotice).await()
            // Save to local immediately for instant UI update
            dao.insertNotices(listOf(finalNotice.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNoticesForUser(userWing: String, isOwner: Boolean): List<Notice> {
        // 1. Try Fetching from Network
        try {
            val snapshot = firestore.collection("notices")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            val networkNotices = snapshot.toObjects(Notice::class.java)

            // 2. Save to Room (Single Source of Truth)
            dao.clearNotices()
            dao.insertNotices(networkNotices.map { it.toEntity() })
        } catch (e: Exception) {
            // Network failed? Ignore, we will use Cache.
            e.printStackTrace()
        }

        // 3. Always return from Room (Offline First)
        val localNotices = dao.getAllNotices().map { it.toDomain() }

        // 4. Apply Logic Filtering
        return localNotices.filter { notice ->
            when (notice.targetAudience) {
                "ALL" -> true
                "OWNERS_ONLY" -> isOwner
                else -> notice.targetAudience == "WING_$userWing"
            }
        }
    }
}

fun Notice.toEntity() = NoticeEntity(
    id,
    title,
    content,
    priority,
    targetAudience,
    postedByAdminId,
    timestamp.toDate().time
)

fun NoticeEntity.toDomain() = Notice(
    id, title, content, null, priority, targetAudience, postedByAdminId,
    Timestamp(Date(timestamp))
)