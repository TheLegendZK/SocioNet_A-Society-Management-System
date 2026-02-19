package com.zhd.socionet.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zhd.socionet.core.utils.OtpGenerator
import com.zhd.socionet.core.utils.OtpGeneratorImpl
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.repository.AdminRepository
import com.zhd.socionet.data.repository.AdminRepositoryImpl
import com.zhd.socionet.data.repository.AdminUserRepository
import com.zhd.socionet.data.repository.AdminUserRepositoryImpl
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.AuthRepositoryImpl
import com.zhd.socionet.data.repository.BillingRepository
import com.zhd.socionet.data.repository.BillingRepositoryImpl
import com.zhd.socionet.data.repository.ComplaintRepository
import com.zhd.socionet.data.repository.ComplaintRepositoryImpl
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.GatePassRepositoryImpl
import com.zhd.socionet.data.repository.NoticeRepository
import com.zhd.socionet.data.repository.NoticeRepositoryImpl
import com.zhd.socionet.data.repository.ProfileRepository
import com.zhd.socionet.data.repository.ProfileRepositoryImpl
import com.zhd.socionet.data.repository.SecurityRepository
import com.zhd.socionet.data.repository.SecurityRepositoryImpl
import com.zhd.socionet.data.repository.SettingsRepository
import com.zhd.socionet.data.repository.SettingsRepositoryImpl
import com.zhd.socionet.data.repository.SocietyRepository
import com.zhd.socionet.data.repository.SocietyRepositoryImpl
import com.zhd.socionet.data.repository.SosRepositoryImpl
import com.zhd.socionet.data.repository.StaffRepository
import com.zhd.socionet.data.repository.StaffRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

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
    fun provideProfileRepository(
        auth: FirebaseAuth, 
        db: FirebaseFirestore,
        storage: FirebaseStorage,
        @ApplicationContext context: Context
    ): ProfileRepository {
        return ProfileRepositoryImpl(auth, db, storage, context)
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