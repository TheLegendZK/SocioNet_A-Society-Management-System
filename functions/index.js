const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
setGlobalOptions({ region: "asia-south1", maxInstances: 10 });

// --- NOTIFICATION LOGIC ---

// Notification for Visitor Approvals
async function processVisitorNotification(event, docId) {
    const snapshot = event.data;
    if (!snapshot) return;

    const entry = snapshot.data();

    if (entry.status !== "PENDING_APPROVAL") {
        return;
    }

    const hostUserId = entry.hostUserId;
    const hostFlatId = entry.hostFlatId;
    const visitorName = entry.visitorName || "A visitor";

    console.log(`[VISITOR] ${visitorName} for Flat: ${hostFlatId}`);

    try {
        const tokensMap = new Map();

        if (hostUserId) {
            const userDoc = await admin.firestore().collection("users").doc(hostUserId).get();
            if (userDoc.exists && userDoc.data().fcmToken) {
                tokensMap.set(userDoc.data().fcmToken, userDoc.id);
            }
        }

        if (hostFlatId) {
            const assignedUsers = await admin.firestore().collection("users").where("flatId", "==", hostFlatId).get();
            assignedUsers.forEach(doc => {
                if (doc.data().fcmToken) {
                    tokensMap.set(doc.data().fcmToken, doc.id);
                }
            });
        }

        const tokensArray = Array.from(tokensMap.keys());
        if (tokensArray.length === 0) return;

        const message = {
            notification: {
                title: "Visitor Approval Request",
                body: `${visitorName} is at the gate. Tap to approve.`
            },
            android: {
                notification: {
                    channelId: "socionet_alerts",
                    priority: "high"
                    // Removed custom clickAction to allow default launcher activity handling
                }
            },
            data: {
                entryId: docId,
                hostFlatId: hostFlatId || "",
                targetUserId: hostUserId || ""
            },
            tokens: tokensArray
        };

        await admin.messaging().sendEachForMulticast(message);
    } catch (error) {
        console.error("Error sending visitor notification:", error);
    }
}

// Notification for Emergency SOS
async function processSosNotification(event, docId) {
    const snapshot = event.data;
    if (!snapshot) return;

    const alert = snapshot.data();
    const userName = alert.raisedByUserName || "A resident";
    const flatNumber = alert.flatNumber || "Unknown Flat";

    console.log(`[SOS] ${userName} from Flat ${flatNumber}`);

    try {
        const societyId = alert.flatId.split("_")[0];
        const staffDocs = await admin.firestore().collection("users")
            .where("societyId", "==", societyId)
            .get();

        const tokensSet = new Set();
        staffDocs.forEach(doc => {
            const data = doc.data();
            // Using a Set to ensure users with multiple roles only get one notification
            if (data.fcmToken && (data.roles.includes("SECURITY_GUARD") || data.roles.includes("SUPER_ADMIN"))) {
                tokensSet.add(data.fcmToken);
            }
        });

        const tokensArray = Array.from(tokensSet);
        if (tokensArray.length === 0) return;

        const message = {
            notification: {
                title: "⚠️ EMERGENCY ALERT",
                body: `${userName} from Flat ${flatNumber} has triggered an SOS!`
            },
            android: {
                notification: { channelId: "socionet_alerts", priority: "high", color: "#FF0000" }
            },
            data: { alertId: docId, type: "SOS" },
            tokens: tokensArray
        };

        await admin.messaging().sendEachForMulticast(message);
    } catch (error) {
        console.error("Error sending SOS notification:", error);
    }
}

// --- FAN-OUT LOGIC (DATA CONSISTENCY) ---

exports.onUserProfileUpdate = onDocumentUpdated("users/{userId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();
    if (newData.fullName !== oldData.fullName) {
        const userId = event.params.userId;
        const db = admin.firestore();
        const batch = db.batch();
        const complaints = await db.collection("complaints").where("raisedByUserId", "==", userId).get();
        complaints.forEach(doc => batch.update(doc.ref, { raisedByUserName: newData.fullName }));
        const notices = await db.collection("notices").where("postedByAdminId", "==", userId).get();
        notices.forEach(doc => batch.update(doc.ref, { authorName: newData.fullName }));
        const flats = await db.collection("flats").where("primaryOwnerId", "==", userId).get();
        flats.forEach(doc => batch.update(doc.ref, { primaryOwnerName: newData.fullName }));
        const alerts = await db.collection("emergency_alerts").where("raisedByUserId", "==", userId).get();
        alerts.forEach(doc => batch.update(doc.ref, { raisedByUserName: newData.fullName }));
        return batch.commit();
    }
    return null;
});

exports.onFlatUpdate = onDocumentUpdated("flats/{flatId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();
    if (newData.flatNumber !== oldData.flatNumber) {
        const flatId = event.params.flatId;
        const db = admin.firestore();
        const batch = db.batch();
        const users = await db.collection("users").where("flatId", "==", flatId).get();
        users.forEach(doc => batch.update(doc.ref, { flatNumber: newData.flatNumber }));
        const complaints = await db.collection("complaints").where("flatId", "==", flatId).get();
        complaints.forEach(doc => batch.update(doc.ref, { flatNumber: newData.flatNumber }));
        const bills = await db.collection("bills").where("flatId", "==", flatId).get();
        bills.forEach(doc => batch.update(doc.ref, { flatNumber: newData.flatNumber }));
        const alerts = await db.collection("emergency_alerts").where("flatId", "==", flatId).get();
        alerts.forEach(doc => batch.update(doc.ref, { flatNumber: newData.flatNumber }));
        return batch.commit();
    }
    return null;
});

exports.onSocietyUpdate = onDocumentUpdated("societies/{societyId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();
    if (newData.name !== oldData.name) {
        const societyId = event.params.societyId;
        const db = admin.firestore();
        const users = await db.collection("users").where("societyId", "==", societyId).get();
        const batch = db.batch();
        users.forEach(doc => batch.update(doc.ref, { societyName: newData.name }));
        return batch.commit();
    }
    return null;
});

// --- EXPORTS ---
exports.notifyOnGateEntries = onDocumentCreated("gate_entries/{entryId}", (event) => processVisitorNotification(event, event.params.entryId));
exports.notifyOnGatePasses = onDocumentCreated("gate_passes/{entryId}", (event) => processVisitorNotification(event, event.params.entryId));
exports.notifyOnSosAlert = onDocumentCreated("emergency_alerts/{alertId}", (event) => processSosNotification(event, event.params.alertId));
