package com.group16.eventplaza.model;

import com.firebase.geofire.GeoLocation;

public class EventCard {

    private String title;

    private String dateTime;

    private String eventDescription;

    private String owner;

    private String ownerAvatar;

    private GeoLocation geoLocation;

    private String location;

    private int currentParticipant;

    private int maxParticipant;
    private String eventId;

    private double lat;

    private double lng;

    private String image;

    private String OwnerId;


    public EventCard() {
    }

    public EventCard(String title, String dateTime, String eventDescription, String owner, String ownerAvatar,
                     String location) {
        this.title = title;
        this.dateTime = dateTime;
        this.eventDescription = eventDescription;
        this.owner = owner;
        this.ownerAvatar = ownerAvatar;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerAvatar() {
        return ownerAvatar;
    }

    public void setOwnerAvatar(String ownerAvatar) {
        this.ownerAvatar = ownerAvatar;
    }

    public int getParticipant() {
        return currentParticipant;
    }

    public void setCurrentParticipant(int currentParticipant) {
        this.currentParticipant = currentParticipant;
    }

    public int getMaxParticipant() {
        return maxParticipant;
    }

    public void setMaxParticipant(int maxParticipant) {
        this.maxParticipant = maxParticipant;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String toString() {
        return "EventCard{" +
                "dateTime='" + dateTime + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", owner='" + owner + '\'' +
                ", ownerAvatar='" + ownerAvatar + '\'' +
                ", geoLocation=" + geoLocation +
                '}';
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public int getCurrentParticipant() {
        return currentParticipant;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



    public String getOwnerId() {
        return OwnerId;
    }

    public void setOwnerId(String ownerId) {
        OwnerId = ownerId;
    }
}
