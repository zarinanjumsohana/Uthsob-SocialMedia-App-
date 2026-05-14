package com.example.uthsob3o;

import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NotificationHelper {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static String getTime() {
        return new SimpleDateFormat("hh:mm a",
                Locale.getDefault()).format(new Date());
    }

    private static void saveAlert(
            String uid, Map<String, Object> notif) {
        if (uid == null || uid.isEmpty()) return;
        db.collection("notifications")
                .document(uid)
                .collection("alerts")
                .document(UUID.randomUUID().toString())
                .set(notif);
    }

    private static void saveFull(
            String uid, Map<String, Object> notif) {
        if (uid == null || uid.isEmpty()) return;
        db.collection("notifications")
                .document(uid)
                .collection("full")
                .document(UUID.randomUUID().toString())
                .set(notif);
    }

    // 1. New booking request → notify farmer
    public static void sendBookingRequestAlert(
            String farmerUid,
            String businessmanName,
            String cropName,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "booking_request");
        notif.put("title", "নতুন বুকিং অনুরোধ! 📦");
        notif.put("message", businessmanName
                + " আপনার " + cropName
                + " বুক করতে চান।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("businessmanName", businessmanName);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(farmerUid, notif);
    }

    // 2. New bid placed → notify farmer
    public static void sendBidReceivedAlert(
            String farmerUid,
            String businessmanName,
            String cropName,
            double bidAmount,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "bid_received");
        notif.put("title", "নতুন বিড পাওয়া গেছে! 🔨");
        notif.put("message", businessmanName
                + " আপনার " + cropName
                + " এর জন্য ৳" + (int) bidAmount
                + " বিড করেছেন।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("bidAmount", bidAmount);
        notif.put("businessmanName", businessmanName);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(farmerUid, notif);
    }

    // 3. Booking accepted → notify businessman
    public static void sendBookingAcceptedAlert(
            String businessmanUid,
            String farmerName,
            String cropName,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "booking_accepted");
        notif.put("title", "বুকিং গৃহীত হয়েছে! ✅");
        notif.put("message", farmerName
                + " আপনার " + cropName
                + " বুকিং গ্রহণ করেছেন। পণ্য বিক্রি হয়েছে।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(businessmanUid, notif);
    }

    // 4. Auction started → notify followers
    public static void sendAuctionStartedAlert(
            String userId,
            String farmerName,
            String cropName,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "auction_started");
        notif.put("title", "নিলাম শুরু হয়েছে! 🔔");
        notif.put("message", farmerName
                + " এর " + cropName
                + " এর নিলাম শুরু হয়েছে। এখনই বিড করুন!");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(userId, notif);
    }

    // 5a. Auction won → notify winner
    public static void sendAuctionWonAlert(
            String winnerUid,
            String farmerName,
            String cropName,
            double amount,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "auction_won");
        notif.put("title", "আপনি নিলাম জিতেছেন! 🏆");
        notif.put("message", farmerName
                + " আপনার ৳" + (int) amount
                + " বিড গ্রহণ করেছেন। "
                + cropName + " কিনতে যোগাযোগ করুন।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(winnerUid, notif);
    }

    // 5b. Auction lost → notify losers
    public static void sendAuctionLostAlert(
            String loserUid,
            String cropName,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "auction_lost");
        notif.put("title", "নিলামে হেরেছেন 😔");
        notif.put("message", cropName
                + " এর নিলামে অন্য কেউ জিতেছেন।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(loserUid, notif);
    }

    // Bid accepted → notify businessman
    public static void sendBidAcceptedAlert(
            String businessmanUid,
            String farmerName,
            String cropName,
            double amount,
            String cropId) {
        sendAuctionWonAlert(businessmanUid,
                farmerName, cropName, amount, cropId);
    }

    // Follow notification
    public static void sendFollowNotification(
            String farmerUid,
            String followerName) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "new_follower");
        notif.put("title", "নতুন ফলোয়ার! 👤");
        notif.put("message", followerName
                + " আপনাকে ফলো করেছেন।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("timestamp", System.currentTimeMillis());
        saveFull(farmerUid, notif);
    }

    // Auction ending soon → notify farmer
    public static void sendAuctionEndingAlert(
            String farmerUid,
            String cropName,
            String cropId) {

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "auction_ending");
        notif.put("title", "নিলাম শেষ হচ্ছে! ⏰");
        notif.put("message", "আপনার " + cropName
                + " এর নিলাম শীঘ্রই শেষ হবে।");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());
        saveAlert(farmerUid, notif);
    }

    // Like notification (goes to full/activity tab)
    public static void sendLikeNotification(
            String userId,
            String likerName,
            String postContent) {

        if (userId == null || userId.isEmpty()) return;

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "like");
        notif.put("title", "লাইক পেয়েছেন! ❤️");
        notif.put("message", likerName
                + " আপনার পোস্ট পছন্দ করেছেন: \""
                + postContent + "\"");
        notif.put("time", getTime());
        notif.put("read", false);
        notif.put("timestamp", System.currentTimeMillis());
        saveFull(userId, notif);
    }

    // Send FCM push notification to device
    public static void sendPushNotification(
            String toUid,
            String title,
            String body) {

        // Get user's FCM token
        db.collection("users").document(toUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String token =
                                doc.getString("fcmToken");
                        if (token != null
                                && !token.isEmpty()) {
                            // Save notification for FCM
                            // (FCM sending requires server)
                            // For now save to Firestore
                            // and show in-app
                        }
                    }
                });
    }
}