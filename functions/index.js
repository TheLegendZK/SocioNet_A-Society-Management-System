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
        const tokensMap = new Map(); // Using a Map to track which token belongs to which user

        // Strategy 1: Find token via specific User ID
        if (hostUserId) {
            const userDoc = await admin.firestore().collection("users").doc(hostUserId).get();
            if (userDoc.exists && userDoc.data().fcmToken) {
                tokensMap.set(userDoc.data().fcmToken, hostUserId);
                console.log("Token found via hostUserId");
            }
        }

        // Strategy 2: Fallback! Search for ANY user living in that flat
        if (hostFlatId) {
            const assignedUsers = await admin.firestore().collection("users").where("assignedFlatId", "==", hostFlatId).get();
            assignedUsers.forEach(doc => {
                if (doc.data().fcmToken) {
                    tokensMap.set(doc.data().fcmToken, doc.id);
                }
            });
        }

        const tokensArray = Array.from(tokensMap.keys());

        if (tokensArray.length === 0) {
            console.log("CRITICAL ERROR: No FCM tokens found in the database.");
            return;
        }

        // Create the Push Notification Payload
        // We use data-only messages for more control on the client side if needed,
        // but keeping notification for now as it's simpler for basic use.
        const message = {
            notification: {
                title: "Visitor Approval Request",
                body: `${visitorName} is at the gate. Tap to approve/deny.`
            },
            android: {
                notification: {
                    channelId: "socionet_alerts",
                    priority: "high",
                    clickAction: "OPEN_ACTIVITY_1"
                }
            },
            data: {
                entryId: docId,
                hostFlatId: hostFlatId || "",
                targetUserId: hostUserId || "" // We'll pass the intended recipient ID
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

exports.notifyOnGateEntries = onDocumentCreated("gate_entries/{entryId}", (event) => processNotification(event, event.params.entryId));
exports.notifyOnGatePasses = onDocumentCreated("gate_passes/{entryId}", (event) => processNotification(event, event.params.entryId));
exports.notifyOnGatePass = onDocumentCreated("GatePass/{entryId}", (event) => processNotification(event, event.params.entryId));
