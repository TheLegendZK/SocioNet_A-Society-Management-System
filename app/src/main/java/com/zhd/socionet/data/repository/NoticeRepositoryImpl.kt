package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.local.NoticeEntity
import com.zhd.socionet.data.model.Notice
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface NoticeRepository {
    suspend fun createNotice(notice: Notice): Result<Unit>
    suspend fun updateNotice(notice: Notice): Result<Unit>
    suspend fun deleteNotice(noticeId: String): Result<Unit>
    suspend fun getNoticesForSociety(societyId: String): Result<List<Notice>>
    suspend fun getNoticesForUser(societyId: String, userWing: String, isOwner: Boolean): List<Notice>
}

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dao: NoticeDao,
    private val crashlytics: FirebaseCrashlytics
) : NoticeRepository {
    override suspend fun createNotice(notice: Notice): Result<Unit> {
        return try {
            val docRef = firestore.collection(AppConstants.COLL_NOTICES).document()
            val finalNotice = notice.copy(id = docRef.id, timestamp = Timestamp.now())
            firestore.collection(AppConstants.COLL_NOTICES).document(docRef.id).set(finalNotice).await()
            dao.insertNotices(listOf(finalNotice.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateNotice(notice: Notice): Result<Unit> {
        return try {
            val finalNotice = notice.copy(timestamp = Timestamp.now())
            firestore.collection(AppConstants.COLL_NOTICES).document(notice.id).set(finalNotice).await()
            dao.insertNotices(listOf(finalNotice.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLL_NOTICES).document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getNoticesForSociety(societyId: String): Result<List<Notice>> {
        return try {
            // Index Error Fix: Fetch all for society and sort locally to avoid composite index requirement
            val snapshot = firestore.collection(AppConstants.COLL_NOTICES)
                .whereEqualTo("societyId", societyId)
                .get()
                .await()
            
            val notices = snapshot.toObjects(Notice::class.java)
                .sortedByDescending { it.timestamp }
            
            Result.success(notices)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getNoticesForUser(societyId: String, userWing: String, isOwner: Boolean): List<Notice> {
        try {
            // Index Error Fix: Fetch and sort locally
            val snapshot = firestore.collection(AppConstants.COLL_NOTICES)
                .whereEqualTo("societyId", societyId)
                .get().await()
                
            val networkNotices = snapshot.toObjects(Notice::class.java)
                .sortedByDescending { it.timestamp }

            dao.clearNotices()
            dao.insertNotices(networkNotices.map { it.toEntity() })
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }

        val localNotices = dao.getAllNotices().map { it.toDomain() }

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
    id = id,
    title = title,
    content = content,
    priority = priority,
    targetAudience = targetAudience,
    postedByAdminId = postedByAdminId,
    authorName = authorName,
    timestamp = timestamp.toDate().time
)

fun NoticeEntity.toDomain() = Notice(
    id = id,
    title = title,
    content = content,
    attachmentUrl = null,
    priority = priority,
    targetAudience = targetAudience,
    postedByAdminId = postedByAdminId,
    authorName = authorName,
    timestamp = Timestamp(Date(timestamp))
)
