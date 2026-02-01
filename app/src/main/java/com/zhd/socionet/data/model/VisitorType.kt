package com.zhd.socionet.data.model

enum class VisitorType {
    GUEST,          // Friends/Family
    DELIVERY,       // Zomato/Amazon
    CAB,            // Uber/Ola
    SERVICE_STAFF,  // Daily help
    EMERGENCY,      // Ambulance/Fire
    MATERIAL_TRUCK  // Move-in/Move-out (Requires NOC)
}