const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
setGlobalOptions({ region: "asia-south1", maxInstances: 10 });

// Core logic for sending the notification
async function processNotification(event, docId) {
    const snapshot = event.data;
    if (!snapshot) return;

    const entry = snapshot.data();

    if (entry.status !== "PENDING_APPROVAL") {
        return;
    }

    const hostUserId = entry.hostUserId;
    const hostFlatId = entry.hostFlatId;
    const visitorName = entry.visitorName || "A visitor";

    console.log(`[TRIGGERED] Visitor: ${visitorName}, Flat: ${hostFlatId}, User: ${hostUserId}`);

    try {
        const tokens = new Set(); // Using a Set prevents duplicate notifications

        // Strategy 1: Find token via specific User ID (From our Android fix)
        if (hostUserId) {
            const userDoc = await admin.firestore().collection("users").doc(hostUserId).get();
            if (userDoc.exists && userDoc.data().fcmToken) {
                tokens.add(userDoc.data().fcmToken);
                console.log("Token found via hostUserId");
            }
        }

        // Strategy 2: Fallback! Search for ANY user living in that flat
        if (hostFlatId) {
            // Check if the database uses 'flatId'
            const flatUsers = await admin.firestore().collection("users").where("flatId", "==", hostFlatId).get();
            flatUsers.forEach(doc => { if (doc.data().fcmToken) tokens.add(doc.data().fcmToken); });

            // Check if the database uses 'assignedFlatId'
            const assignedUsers = await admin.firestore().collection("users").where("assignedFlatId", "==", hostFlatId).get();
            assignedUsers.forEach(doc => { if (doc.data().fcmToken) tokens.add(doc.data().fcmToken); });
        }

        const tokensArray = Array.from(tokens);

        if (tokensArray.length === 0) {
            console.log("CRITICAL ERROR: No FCM tokens found in the database. The Resident MUST log into the app at least once to generate a token.");
            return;
        }

        // Create the Push Notification Payload
        const message = {
            notification: {
                title: "Visitor Approval Request",
                body: `${visitorName} is at the gate. Tap to approve/deny.`
            },
            android: {
                notification: {
                    channelId: "socionet_alerts",
                    priority: "high"
                }
            },
            data: {
                entryId: docId
            },
            tokens: tokensArray
        };

        // Send the Notification
        const response = await admin.messaging().sendEachForMulticast(message);
        console.log(`Notification sent! Success: ${response.successCount}, Failed: ${response.failureCount}`);

    } catch (error) {
        console.error("Error sending notification:", error);
    }
}

// ----------------------------------------------------------------------------------
// FAILSAFE: We deploy 3 triggers to ensure we catch the entry no matter what
// your AppConstants.COLL_GATE_PASSES is named in Android.
// ----------------------------------------------------------------------------------
exports.notifyOnGateEntries = onDocumentCreated("gate_entries/{entryId}", (event) => processNotification(event, event.params.entryId));
exports.notifyOnGatePasses = onDocumentCreated("gate_passes/{entryId}", (event) => processNotification(event, event.params.entryId));
exports.notifyOnGatePass = onDocumentCreated("GatePass/{entryId}", (event) => processNotification(event, event.params.entryId));