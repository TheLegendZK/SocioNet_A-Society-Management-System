package com.zhd.socionet.di

import android.content.Context
import androidx.room.Room
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.GateEntryDao
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.local.SocioNetDatabase
import com.zhd.socionet.data.local.StaffDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SocioNetDatabase {
        return Room.databaseBuilder(
            context,
            SocioNetDatabase::class.java,
            "socionet_local_db"
        )
            // Removed destructive migration for production safety. 
            // In the future, add proper Migration objects here using .addMigrations()
            .build()
    }

    @Provides
    fun provideNoticeDao(db: SocioNetDatabase): NoticeDao = db.noticeDao()

    @Provides
    fun provideComplaintDao(db: SocioNetDatabase): ComplaintDao = db.complaintDao()

    @Provides
    fun provideGateEntryDao(db: SocioNetDatabase): GateEntryDao = db.gateEntryDao()

    @Provides
    fun provideStaffDao(db: SocioNetDatabase): StaffDao = db.staffDao()

    @Provides
    fun provideBillDao(db: SocioNetDatabase): BillDao = db.billDao()
}