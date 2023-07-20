package com.group16.eventplaza.model;

import com.google.firebase.Timestamp;

public class NotificationInfo {
    String eventId;
    String requestUserId;
    Timestamp requestTime;
    String message;
    String eventUid;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    public String getRequestUserId() {
        return requestUserId;
    }

    public void setRequestUserId(String requestUserId) {
        this.requestUserId = requestUserId;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
