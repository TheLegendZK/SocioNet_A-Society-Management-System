package com.zhd.socionet.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zhd.socionet.core.utils.OtpGenerator
import com.zhd.socionet.core.utils.OtpGeneratorImpl
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.GateEntryDao
import com.zhd.socionet.data.local.NoticeDao
import com.zhd.socionet.data.remote.FirestoreHelper
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
import com.zhd.socionet.data.repository.SosRepository
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
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideOtpGenerator(): OtpGenerator = OtpGeneratorImpl()

    @Provides
    @Singleton
    fun provideFirestoreHelper(db: FirebaseFirestore): FirestoreHelper {
        return FirestoreHelper(db)
    }

    // --- Repository Bindings ---

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth, 
        firestoreHelper: FirestoreHelper, 
        crashlytics: FirebaseCrashlytics
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestoreHelper, crashlytics)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(firestoreHelper: FirestoreHelper): AdminRepository {
        return AdminRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideAdminUserRepository(firestoreHelper: FirestoreHelper): AdminUserRepository {
        return AdminUserRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideGatePassRepository(
        firestoreHelper: FirestoreHelper,
        dao: GateEntryDao,
        crashlytics: FirebaseCrashlytics
    ): GatePassRepository {
        return GatePassRepositoryImpl(firestoreHelper, dao, crashlytics)
    }

    @Provides
    @Singleton
    fun provideSecurityRepository(firestoreHelper: FirestoreHelper): SecurityRepository {
        return SecurityRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideNoticeRepository(
        firestoreHelper: FirestoreHelper,
        dao: NoticeDao,
        crashlytics: FirebaseCrashlytics
    ): NoticeRepository {
        return NoticeRepositoryImpl(firestoreHelper, dao, crashlytics)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(
        firestoreHelper: FirestoreHelper,
        dao: BillDao,
        crashlytics: FirebaseCrashlytics
    ): BillingRepository {
        return BillingRepositoryImpl(firestoreHelper, dao, crashlytics)
    }

    @Provides
    @Singleton
    fun provideStaffRepository(firestoreHelper: FirestoreHelper): StaffRepository {
        return StaffRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideComplaintRepository(
        firestoreHelper: FirestoreHelper,
        dao: ComplaintDao,
        crashlytics: FirebaseCrashlytics
    ): ComplaintRepository {
        return ComplaintRepositoryImpl(firestoreHelper, dao, crashlytics)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        auth: FirebaseAuth, 
        firestoreHelper: FirestoreHelper,
        storage: FirebaseStorage,
        @ApplicationContext context: Context,
        crashlytics: FirebaseCrashlytics
    ): ProfileRepository {
        return ProfileRepositoryImpl(auth, firestoreHelper, storage, context, crashlytics)
    }

    @Provides
    @Singleton
    fun provideSosRepository(firestoreHelper: FirestoreHelper): SosRepository {
        return SosRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideSocietyRepository(firestoreHelper: FirestoreHelper): SocietyRepository {
        return SocietyRepositoryImpl(firestoreHelper)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(firestoreHelper: FirestoreHelper): SettingsRepository {
        return SettingsRepositoryImpl(firestoreHelper)
    }
}
