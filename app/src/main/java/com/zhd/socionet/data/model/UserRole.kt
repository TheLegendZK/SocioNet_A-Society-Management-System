package com.zhd.socionet.data.model

enum class UserRole {
    SUPER_ADMIN,    // The Society Secretary
    OWNER,          // Legal owner of a flat
    TENANT,         // Renter (Lease-bound)
    FAMILY_MEMBER,  // Spouse/Kid (No voting rights)
    SECURITY_GUARD, // Staff user
    SERVICE_STAFF   // External (Maid/Driver) - No app access
}