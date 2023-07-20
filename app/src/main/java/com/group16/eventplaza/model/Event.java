package com.group16.eventplaza.model;

import com.google.firebase.Timestamp;

public class Event {
    private String description;
    private Timestamp endDate;
    private String geohash;
    private String image;
    private double lat;
    private double lng;
    private String location;
    private Long maxParticipantsNumber;
    private Long participantsNumber;
    private Timestamp startDate;
    private String status;
    private String title;
    private String userId;

    private String participantList;





    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getParticipantsNumber() {
        return participantsNumber;
    }

    public void setParticipantsNumber(Long participantsNumber) {
        this.participantsNumber = participantsNumber;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getMaxParticipantsNumber() {
        return maxParticipantsNumber;
    }

    public void setMaxParticipantsNumber(Long maxParticipantsNumber) {
        this.maxParticipantsNumber = maxParticipantsNumber;
    }

//    public String getParticipantList() {
//        return participantList;
//    }
//
//    public void setParticipantList(String participantList) {
//        this.participantList = participantList;
//    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {

        this.lng = lng;


    }
}
