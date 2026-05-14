package com.example.uthsob3o;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.uthsob3o.activities.AddCropActivity;
import com.example.uthsob3o.activities.AddPostActivity;
import com.example.uthsob3o.activities.HomeActivity;
import com.example.uthsob3o.activities.NotificationActivity;
import com.example.uthsob3o.activities.ProfileActivity;
import com.example.uthsob3o.activities.SearchActivity;

public class NavigationHelper {

    public static void setupBottomNav(
            Activity activity,
            LinearLayout navHome,
            LinearLayout navSearch,
            LinearLayout navAdd,
            LinearLayout navAlerts,
            LinearLayout navProfile,
            String currentRole,
            String currentUid,
            String activeTab) {

        navHome.setOnClickListener(v -> {
            if ("home".equals(activeTab)) return;
            Intent intent = new Intent(activity,
                    HomeActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });

        navSearch.setOnClickListener(v -> {
            if ("search".equals(activeTab)) return;
            Intent intent = new Intent(activity,
                    SearchActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });

        navAdd.setOnClickListener(v -> {
            if ("farmer".equals(currentRole)) {
                activity.startActivity(new Intent(
                        activity, AddCropActivity.class));
            } else {
                activity.startActivity(new Intent(
                        activity, AddPostActivity.class));
            }
        });

        navAlerts.setOnClickListener(v -> {
            if ("alerts".equals(activeTab)) return;
            Intent intent = new Intent(activity,
                    NotificationActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            if ("profile".equals(activeTab)) return;
            Intent intent = new Intent(activity,
                    ProfileActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });
    }

    public static void highlightTab(
            LinearLayout navHome,
            LinearLayout navSearch,
            LinearLayout navAlerts,
            LinearLayout navProfile,
            String activeTab,
            int activeColor,
            int inactiveColor) {

        setColor(navHome, inactiveColor);
        setColor(navSearch, inactiveColor);
        setColor(navAlerts, inactiveColor);
        setColor(navProfile, inactiveColor);

        switch (activeTab) {
            case "home":
                setColor(navHome, activeColor); break;
            case "search":
                setColor(navSearch, activeColor); break;
            case "alerts":
                setColor(navAlerts, activeColor); break;
            case "profile":
                setColor(navProfile, activeColor); break;
        }
    }

    private static void setColor(
            LinearLayout layout, int color) {
        if (layout == null) return;
        if (layout.getChildCount() >= 2) {
            View child = layout.getChildAt(1);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }
    }
}