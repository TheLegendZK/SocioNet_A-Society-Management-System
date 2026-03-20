package com.zhd.socionet.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralizes access to Collections to avoid typos in String names.
 */
@Singleton
class FirestoreHelper @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getUsersRef(): CollectionReference = firestore.collection(AppConstants.COLL_USERS)
    fun getSocietiesRef(): CollectionReference = firestore.collection(AppConstants.COLL_SOCIETIES)
    fun getFlatsRef(): CollectionReference = firestore.collection(AppConstants.COLL_FLATS)
    fun getGateEntriesRef(): CollectionReference = firestore.collection(AppConstants.COLL_GATE_ENTRIES)
    fun getNoticesRef(): CollectionReference = firestore.collection(AppConstants.COLL_NOTICES)
    fun getBillsRef(): CollectionReference = firestore.collection(AppConstants.COLL_BILLS)
    fun getStaffRef(): CollectionReference = firestore.collection(AppConstants.COLL_SERVICE_STAFF)
    fun getComplaintsRef(): CollectionReference = firestore.collection(AppConstants.COLL_COMPLAINTS)
    fun getEmergencyAlertsRef(): CollectionReference = firestore.collection(AppConstants.COLL_EMERGENCY_ALERTS)
    
    fun getFirestore() = firestore
}
