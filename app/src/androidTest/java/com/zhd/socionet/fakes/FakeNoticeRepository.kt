package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.repository.NoticeRepository

class FakeNoticeRepository : NoticeRepository {
    override suspend fun createNotice(notice: Notice): Result<Unit> = Result.success(Unit)
    override suspend fun getNoticesForUser(userWing: String, isOwner: Boolean): List<Notice> = listOf(
        Notice(id = "n1", title = "Meeting", content = "General meeting at 8 PM.")
    )
}
