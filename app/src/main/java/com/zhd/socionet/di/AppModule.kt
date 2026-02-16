package com.zhd.socionet.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.utils.OtpGenerator
import com.zhd.socionet.core.utils.OtpGeneratorImpl
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideOtpGenerator(): OtpGenerator = OtpGeneratorImpl()

    // --- Repository Bindings ---

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, db: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(auth, db)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(db: FirebaseFirestore): AdminRepository {
        return AdminRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideAdminUserRepository(db: FirebaseFirestore): AdminUserRepository {
        return AdminUserRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideGatePassRepository(db: FirebaseFirestore): GatePassRepository {
        return GatePassRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideSecurityRepository(db: FirebaseFirestore): SecurityRepository {
        return SecurityRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideNoticeRepository(db: FirebaseFirestore, dao: NoticeDao): NoticeRepository {
        return NoticeRepositoryImpl(db, dao)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(db: FirebaseFirestore, dao: BillDao): BillingRepository {
        return BillingRepositoryImpl(db, dao)
    }

    @Provides
    @Singleton
    fun provideStaffRepository(db: FirebaseFirestore): StaffRepository {
        return StaffRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideComplaintRepository(db: FirebaseFirestore, dao: ComplaintDao): ComplaintRepository {
        return ComplaintRepositoryImpl(db, dao)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(auth: FirebaseAuth, db: FirebaseFirestore): ProfileRepository {
        return ProfileRepositoryImpl(auth, db)
    }

    @Provides
    @Singleton
    fun provideSosRepository(db: FirebaseFirestore): SosRepositoryImpl {
        return SosRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideSocietyRepository(db: FirebaseFirestore): SocietyRepository {
        return SocietyRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(db: FirebaseFirestore): SettingsRepository {
        return SettingsRepositoryImpl(db)
    }
}