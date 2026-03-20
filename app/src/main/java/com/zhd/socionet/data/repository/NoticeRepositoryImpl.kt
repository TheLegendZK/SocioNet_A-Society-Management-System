package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.local.NoticeEntity
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.remote.FirestoreHelper
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
    private val db: FirestoreHelper,
    private val dao: NoticeDao,
    private val crashlytics: FirebaseCrashlytics
) : NoticeRepository {
    override suspend fun createNotice(notice: Notice): Result<Unit> {
        return try {
            val docRef = db.getNoticesRef().document()
            val finalNotice = notice.copy(id = docRef.id, timestamp = Timestamp.now())
            db.getNoticesRef().document(docRef.id).set(finalNotice).await()
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
            db.getNoticesRef().document(notice.id).set(finalNotice).await()
            dao.insertNotices(listOf(finalNotice.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            db.getNoticesRef().document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getNoticesForSociety(societyId: String): Result<List<Notice>> {
        return try {
            val snapshot = db.getNoticesRef()
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
            val snapshot = db.getNoticesRef()
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
    attachmentUrl = attachmentUrl,
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
    attachmentUrl = attachmentUrl,
    priority = priority,
    targetAudience = targetAudience,
    postedByAdminId = postedByAdminId,
    authorName = authorName,
    timestamp = Timestamp(Date(timestamp))
)