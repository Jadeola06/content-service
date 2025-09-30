package com.flexydemy.content.enums;

import lombok.Getter;

@Getter
public enum NotificationTypes {
    // General System Notifications
    SYSTEM_ALERT,
    SYSTEM_UPDATE,
    MAINTENANCE_NOTICE,

    // User Actions
    USER_REGISTERED,
    USER_VERIFIED,
    USER_PROFILE_UPDATED,
    USER_BANNED,
    USER_REPORTED,

    // 💬 Messaging
    MESSAGE_RECEIVED,

    //Achievement
    ACHIEVEMENT,

    //Session
    SESSION_REMINDER,

    COURSE_PROGRESS,

    // 📢 Admin / Moderator
    ADMIN_ANNOUNCEMENT,
    ADMIN_ACTION_TAKEN,
    MODERATOR_COMMENT,

    // 📄 Content / Post / Media
    CONTENT_CREATED,
    CONTENT_UPDATED,
    CONTENT_DELETED,
    COMMENT_ADDED,
    COMMENT_REPLIED,
    CONTENT_REPORTED,

    // 📦 Subscription & Billing
    SUBSCRIPTION_STARTED,
    SUBSCRIPTION_RENEWED,
    SUBSCRIPTION_CANCELLED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // 📈 Analytics / Stats
    NEW_FOLLOWER,
    ACCOUNT_TRENDING,
    MILESTONE_REACHED,

    // 🔐 Security
    PASSWORD_CHANGED,
    LOGIN_ALERT,
    SUSPICIOUS_ACTIVITY,
    TWO_FACTOR_ENABLED,

    // 📬 Other
    INVITE_RECEIVED,
    TASK_ASSIGNED,
    REMINDER,

    // 🧪 Custom / Fallback
    CUSTOM
}
