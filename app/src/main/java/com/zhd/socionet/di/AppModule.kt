package com.zhd.socionet.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import com.zhd.socionet.data.repository.SosRepositoryImpl
import com.zhd.socionet.data.repository.StaffRepository
import com.zhd.socionet.data.repository.StaffRepositoryImpl
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

    // --- Repository Bindings ---
    // In a larger app, we'd use @Binds in an abstract module, but this is clearer for now.

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
    fun provideNoticeRepository(db: FirebaseFirestore): NoticeRepository {
        return NoticeRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(db: FirebaseFirestore): BillingRepository {
        return BillingRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideStaffRepository(db: FirebaseFirestore): StaffRepository {
        return StaffRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideComplaintRepository(db: FirebaseFirestore): ComplaintRepository {
        return ComplaintRepositoryImpl(db)
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
}