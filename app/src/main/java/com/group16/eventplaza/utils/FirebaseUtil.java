package com.group16.eventplaza.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.group16.eventplaza.MyApplication;
import com.group16.eventplaza.model.Event;
import com.group16.eventplaza.model.EventCard;
import com.group16.eventplaza.model.NotificationInfo;
import com.group16.eventplaza.model.Participant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    private static Event event1 = null;
    /**
     * get the event detail
     * @param eventId eventId
     * @param handler handler
     */
    public static void getEvent(String eventId, Handler handler) {
        Log.d(TAG, "getEvent begin");
        FirebaseFirestore.getInstance().collection("events")
                .document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getEvent isSuccessful" + task.getResult().getData().toString());
//                        Event event1 = null;
                        try {
                            task.getResult();
                            event1 = (Event) JsonUtil.mapToObject(task.getResult().getData(), Event.class);
                        } catch (Exception e) {
                            Log.d(TAG, "null event1");
                            e.printStackTrace();
                        }
                        //get current timestamp

                        Date date = new Date(System.currentTimeMillis());

                        // check is event ended
                        if(event1.getEndDate().toDate().before(date)){
                            Log.d(TAG, "event is ended");
                            event1.setStatus("ended");
                            // set event status as ended
                            FirebaseFirestore.getInstance().collection("events")
                                    .document(eventId).set(event1);
                        }else{
                            Log.d(TAG, "event is running");
                            long hours = ((event1.getStartDate().getSeconds()-(System.currentTimeMillis()/1000))/3600);
                            Log.d(TAG, "left hours "+hours);
                            if(hours<=48 && hours>=0 && (event1.getStatus().equals("running"))){
                                Log.d(TAG, "event start time left in 48 hours");
                                FirebaseFirestore.getInstance().collection("registrations")
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                            Log.d(TAG, "getEventParticipants isSuccessfulll" + task1.getResult().getDocuments());
                                            for (DocumentSnapshot userSnapshot: task1.getResult().getDocuments()) {
                                                String eventId1 = (String) userSnapshot.getData().get("eventId");
                                                if(eventId.equals(eventId1)){
                                                    String userId = (String) userSnapshot.getData().get("userId");
                                                    insertNotification(eventId, event1.getUserId(), String.valueOf(System.currentTimeMillis() / 1000)
                                                            , userId, "Event "+event1.getTitle()+" will start tomorrow!");
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "getEventParticipants complete failed");
                                        }

                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "getEventParticipants Failure");
                                });
                            }
                        }
                        Message message = Message.obtain();
                        message.obj = event1;
                        message.what = 1;
                        if(handler!=null){
                            handler.sendMessage(message);
                        }
                    } else {
                        Log.e(TAG, "getEvent complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getEvent Failure");
                });

    }

    public static void getEventsByUserId(Context context, Handler handler) {
        ArrayList<EventCard> eventLists = new ArrayList<>();
        String userId = FirebaseUtil.getUserId(context);
        FirebaseFirestore.getInstance().collection("events").whereEqualTo("status","running")
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Log.d(TAG, "getevents isSuccessfulll");
                        for(DocumentSnapshot eventSnapshot : task1.getResult().getDocuments()){
                            String ownerId = (String) eventSnapshot.getData().get("userId");
                            if(userId.equals(ownerId)){
                                double lat = eventSnapshot.getDouble("lat");
                                double lng = eventSnapshot.getDouble("lng");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                EventCard eventCard= new EventCard();
                                // TODO add more details
                                eventCard.setEventDescription((String)eventSnapshot.getData().get("description"));
                                eventCard.setEventId(eventSnapshot.getId());
                                eventCard.setGeoLocation(docLocation);
                                eventCard.setTitle(eventSnapshot.getData().get("title").toString());
                                eventCard.setLat((Double) eventSnapshot.getData().get("lat"));
                                eventCard.setLng((Double) eventSnapshot.getData().get("lng"));

                                if (eventSnapshot.getData().get("image")!=null){
                                    eventCard.setImage(eventSnapshot.getData().get("image").toString());
                                }
                                eventCard.setOwnerId(eventSnapshot.getData().get("userId").toString());
                                Log.d(TAG, "getevents userid"+eventSnapshot.getData().get("userId").toString());
                                // get owner
                                FirebaseFirestore.getInstance().collection("users").document(eventSnapshot.getData().get("userId").toString())
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    eventCard.setOwner((String)task.getResult().getData().get("displayName"));
                                                    eventCard.setOwnerAvatar((String)task.getResult().getData().get("urlString"));
                                                }
                                            }
                                            Log.d(TAG, "getevents displayName"+task.getResult().getData().get("displayName"));
                                            Log.d(TAG, "getevents urlString"+task.getResult().getData().get("urlString"));
                                        });
                                // Set Time format
                                Timestamp timestamp = (Timestamp) eventSnapshot.getData().get("startDate");
                                Date date = timestamp.toDate();

                                SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm");
                                eventCard.setDateTime(simpleDateFormat.format(date));

                                eventLists.add(eventCard);

                            }
                        }
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "getevents euqal result: "+eventLists);
                        Message message = Message.obtain();
                        message.obj = eventLists;
                        message.what = 1;
                        if(handler!=null){
                            handler.sendMessage(message);
                        }
                    } else {
                        Log.e(TAG, "getevents complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getevents Failure");
                });
    }
    public static void list3(Context context, Handler handler) {
//        Message message = Message.obtain();
//        ArrayList<EventCard> eventCardArrayList = new ArrayList<>();
//        message.obj = eventCardArrayList;
//        message.what = 3;
//        if(handler!=null){
//            handler.sendMessage(message);
//        }

        // get all eventId current user join but pending
        ArrayList<String> eventIdList = new ArrayList<>();
        ArrayList<EventCard> eventCardArrayList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("registrations").whereEqualTo("status","pending").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "get registration eventId isSuccessful" + task.getResult().getDocuments());
                        for(DocumentSnapshot eventSnapshot : task.getResult().getDocuments()){
                            if(eventSnapshot.get("userId").equals(FirebaseUtil.getUserId(context))&&eventSnapshot.get("status").equals("pending")){
                                String eventId = eventSnapshot.get("eventId").toString();
                                eventIdList.add(eventId);
                            }
                        }

                        FirebaseFirestore.getInstance().collection("events").get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d(TAG, "get registration events isSuccessful");
                                        Log.d(TAG, "get eventIds result list size " + task1.getResult().getDocuments().size());
                                        Log.d(TAG, "get eventIds " + task1.getResult().getDocuments());
                                        Log.d(TAG, "get eventIds lists " + eventIdList);

                                        for(DocumentSnapshot documentSnapshot : task1.getResult().getDocuments()){
                                            if(eventIdList.contains(documentSnapshot.getId())){
                                                Log.d(TAG, "get eventIds contains true");
                                                double lat = documentSnapshot.getDouble("lat");
                                                double lng = documentSnapshot.getDouble("lng");
                                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                                EventCard eventCard= new EventCard();
                                                eventCard.setEventDescription((String)documentSnapshot.getData().get("description"));
                                                eventCard.setEventId(documentSnapshot.getId());
                                                eventCard.setGeoLocation(docLocation);
                                                eventCard.setTitle(documentSnapshot.getData().get("title").toString());
                                                eventCard.setLat((Double) documentSnapshot.getData().get("lat"));
                                                eventCard.setLng((Double) documentSnapshot.getData().get("lng"));
                                                eventCard.setImage(documentSnapshot.getData().get("image").toString());
                                                // Set Time format
                                                Timestamp timestamp = (Timestamp) documentSnapshot.getData().get("startDate");
                                                Date date = timestamp.toDate();

                                                SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm");
                                                eventCard.setDateTime(simpleDateFormat.format(date));

                                                Log.d(TAG, "get eventIds set event data success");
                                                FirebaseFirestore.getInstance().collection("users").document(documentSnapshot.getData().get("userId").toString())
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            Log.d(TAG, "get eventIds user object "+task2.getResult().getData().get("displayName"));
                                                            if (task2.isSuccessful()) {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                    eventCard.setOwner((String)task2.getResult().getData().get("displayName"));
                                                                    eventCard.setOwnerAvatar((String)task2.getResult().getData().get("urlString"));
                                                                }
                                                            }else{
                                                                Log.d(TAG, "get eventIds fail 1111 ");
                                                            }
                                                            Log.d(TAG, "getevents displayName"+task2.getResult().getData().get("displayName"));
                                                            Log.d(TAG, "getevents urlString"+task2.getResult().getData().get("urlString"));
                                                        }).addOnFailureListener(e -> {
                                                            Log.d(TAG, "get eventIds fail 33333");
                                                        });
                                                eventCardArrayList.add(eventCard);
                                            }
                                        }
                                        try {
                                            Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Log.e(TAG, "get registration result "+eventCardArrayList);
                                        Message message = Message.obtain();
                                        message.obj = eventCardArrayList;
                                        message.what = 3;
                                        if(handler!=null){
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.e(TAG, "get registration events complete failed");
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "get registration events Failure");
                                });

                    } else {
                        Log.e(TAG, "get registration eventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "get registration eventId Failure");
                });
    }
    public static void getRegistrationEventsByUserId(Context context, Handler handler) {
        // get all eventId current user registered
        ArrayList<String> eventIdList = new ArrayList<>();
        ArrayList<EventCard> eventCardArrayList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("registrations").whereEqualTo("status","approved").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "get registration eventId isSuccessful" + task.getResult().getDocuments());
                        for(DocumentSnapshot eventSnapshot : task.getResult().getDocuments()){
                            if(eventSnapshot.get("userId").equals(FirebaseUtil.getUserId(context))){
                                String eventId = eventSnapshot.get("eventId").toString();
                                eventIdList.add(eventId);
                            }
                        }

                        FirebaseFirestore.getInstance().collection("events").get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d(TAG, "get registration events isSuccessful");
                                        Log.d(TAG, "get eventIds result list size " + task1.getResult().getDocuments().size());
                                        Log.d(TAG, "get eventIds " + task1.getResult().getDocuments());
                                        Log.d(TAG, "get eventIds lists " + eventIdList);

                                        for(DocumentSnapshot documentSnapshot : task1.getResult().getDocuments()){
                                            if(eventIdList.contains(documentSnapshot.getId())&&documentSnapshot.get("status").toString().equals("running")){
                                                Log.d(TAG, "get eventIds contains true&&status=running");
                                                Log.d(TAG, "22222id" + documentSnapshot.getId());

                                                double lat = documentSnapshot.getDouble("lat");
                                                double lng = documentSnapshot.getDouble("lng");

                                                // We have to filter out a few false positives due to GeoHash
                                                // accuracy, but most will match
                                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                                EventCard eventCard= new EventCard();
                                                // TODO add more details
                                                eventCard.setEventDescription((String)documentSnapshot.getData().get("description"));
                                                eventCard.setEventId(documentSnapshot.getId());
                                                eventCard.setGeoLocation(docLocation);
                                                eventCard.setTitle(documentSnapshot.getData().get("title").toString());
                                                eventCard.setLat((Double) documentSnapshot.getData().get("lat"));
                                                eventCard.setLng((Double) documentSnapshot.getData().get("lng"));
                                                eventCard.setImage(documentSnapshot.getData().get("image").toString());
                                                // Set Time format
                                                Timestamp timestamp = (Timestamp) documentSnapshot.getData().get("startDate");
                                                Date date = timestamp.toDate();

                                                SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm");
                                                eventCard.setDateTime(simpleDateFormat.format(date));

                                                Log.d(TAG, "get eventIds set event data success");
                                                FirebaseFirestore.getInstance().collection("users").document(documentSnapshot.getData().get("userId").toString())
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            Log.d(TAG, "get eventIds user object "+task2.getResult().getData().get("displayName"));
                                                            if (task2.isSuccessful()) {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                    eventCard.setOwner((String)task2.getResult().getData().get("displayName"));
                                                                    eventCard.setOwnerAvatar((String)task2.getResult().getData().get("urlString"));
                                                                }
                                                            }else{
                                                                Log.d(TAG, "get eventIds fail 1111 ");
                                                            }
                                                            Log.d(TAG, "getevents displayName"+task2.getResult().getData().get("displayName"));
                                                            Log.d(TAG, "getevents urlString"+task2.getResult().getData().get("urlString"));
                                                        }).addOnFailureListener(e -> {
                                                            Log.d(TAG, "get eventIds fail 22222 ");
                                                        });
                                                eventCardArrayList.add(eventCard);
                                            }
                                        }
                                        try {
                                            Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Log.e(TAG, "get registration result "+eventCardArrayList);
                                        Message message = Message.obtain();
                                        message.obj = eventCardArrayList;
                                        message.what = 2;
                                        if(handler!=null){
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.e(TAG, "get registration events complete failed");
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "get registration events Failure");
                                });

                    } else {
                        Log.e(TAG, "get registration eventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "get registration eventId Failure");
                });
    }
    public static void setEventCard(String eventId) {

    }
    /**
     * cancel the Event
     *
     * @param eventId eventId
     */
    public static void cancelEvent(String eventId) {
        Log.d(TAG, "cancel Event begin");
        FirebaseFirestore.getInstance().collection("events")
                .document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "cancel Event isSuccessful" + task.getResult().getData().toString());
                        Event event1 = null;
                        try {
                            event1 = (Event) JsonUtil.mapToObject(task.getResult().getData(), Event.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        event1.setStatus("canceled");
                        // set event status as canceled
                        FirebaseFirestore.getInstance().collection("events")
                                .document(eventId).set(event1);
                    } else {
                        Log.e(TAG, "getEvent complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getEvent Failure");
                });
    }

    /**
     * get the event detail
     *
     * @param handler handler
     */
    public static void getEventParticipants(Handler handler) {
        Log.d(TAG, "getEventParticipants begin");
        List<Participant> eventParticipants = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("registrations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getEventParticipants isSuccessful" + task.getResult().getDocuments());
                        for (DocumentSnapshot userSnapshot: task.getResult().getDocuments()) {
//                                    if (!userSnapshot.getData().get("status").equals("approved")) {
//                                        continue;
//                                    }
                            Participant participant = new Participant();
                            String userId = (String) userSnapshot.getData().get("userId");
                            String eventId = (String) userSnapshot.getData().get("eventId");
                            String status = (String) userSnapshot.getData().get("status");
                            participant.setUserId(userId);
                            participant.setEventId(eventId);
                            participant.setStatus(status);
                            eventParticipants.add(participant);
                        }
                        Message message = Message.obtain();
                        message.obj = eventParticipants;
                        message.what = 2;
                        if(handler!=null){
                            handler.sendMessage(message);
                        }
                    } else {
                        Log.e(TAG, "getEventParticipants complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getEventParticipants Failure");
                });
    }

    /**
     * getCurrentUidRequestStatus
     *
     * @param context context
     * @param handler handler
     */
    public static void getCurrentUidRequestStatus(Context context, Handler handler, String eventId) {
        Log.d(TAG, "getCurrentUidRequestStatus begin");

        FirebaseFirestore.getInstance().collection("registrations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getCurrentUidRequestStatus isSuccessful" + task.getResult().getDocuments());
                        String status = "";
                        for (DocumentSnapshot userSnapshot: task.getResult().getDocuments()) {
                            Log.d(TAG,"userSnapshot: "+userSnapshot.getData().get("eventId"));
                            if(userSnapshot.getData().get("userId").equals(FirebaseUtil.getUserId(context)) && userSnapshot.getData().get("eventId").equals(eventId)){
                                status = (String) userSnapshot.getData().get("status");
                                break;
                            }else{
                                continue;
                            }
                        }
                        Message message = Message.obtain();
                        message.obj = status;
                        message.what = 3;
                        handler.sendMessage(message);
                    } else {
                        Log.e(TAG, "getCurrentUidRequestStatus complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getCurrentUidRequestStatus Failure");
                });
    }

    /**
     * getNotificationInfos
     *
     * @param context context
     * @param handler handler
     */
    public static void getNotificationInfos(Context context, Handler handler) {
        Log.d(TAG, "getNotificationInfos begin");

        List<NotificationInfo> notifications = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("notifications").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot userSnapshot: task.getResult().getDocuments()) {
                            if(userSnapshot.getData().get("eventUid").equals(FirebaseUtil.getUserId(context)) &&
                                    userSnapshot.getData().get("message").toString().contains("request")){
                                String eventId = (String) userSnapshot.getData().get("eventId");
                                String eventUid = (String) userSnapshot.getData().get("eventUid");
                                String requestUserId = (String) userSnapshot.getData().get("requestUserId");
                                Timestamp requestTime = (Timestamp) userSnapshot.getData().get("requestTime");
                                String message = (String) userSnapshot.getData().get("message");

                                NotificationInfo notificationInfo = new NotificationInfo();
                                notificationInfo.setEventId(eventId);
                                notificationInfo.setRequestUserId(requestUserId);
                                notificationInfo.setRequestTime(requestTime);
                                notificationInfo.setMessage(message);
                                notificationInfo.setEventUid(eventUid);

                                notifications.add(notificationInfo);
                            }
                            if(userSnapshot.getData().get("requestUserId").equals(FirebaseUtil.getUserId(context)) &&
                                    userSnapshot.getData().get("message").toString().contains("canceled")){
                                String eventId = (String) userSnapshot.getData().get("eventId");
                                String requestUserId = (String) userSnapshot.getData().get("requestUserId");
                                Timestamp requestTime = (Timestamp) userSnapshot.getData().get("requestTime");
                                String message = (String) userSnapshot.getData().get("message");

                                NotificationInfo notificationInfo = new NotificationInfo();
                                notificationInfo.setEventId(eventId);
                                notificationInfo.setRequestUserId(requestUserId);
                                notificationInfo.setRequestTime(requestTime);
                                notificationInfo.setMessage(message);

                                notifications.add(notificationInfo);
                            }
                            if(userSnapshot.getData().get("requestUserId").equals(FirebaseUtil.getUserId(context)) &&
                                    userSnapshot.getData().get("message").toString().contains("tomorrow")){
                                String eventId = (String) userSnapshot.getData().get("eventId");
                                String requestUserId = (String) userSnapshot.getData().get("requestUserId");
                                Timestamp requestTime = (Timestamp) userSnapshot.getData().get("requestTime");
                                String message = (String) userSnapshot.getData().get("message");

                                NotificationInfo notificationInfo = new NotificationInfo();
                                notificationInfo.setEventId(eventId);
                                notificationInfo.setRequestUserId(requestUserId);
                                notificationInfo.setRequestTime(requestTime);
                                notificationInfo.setMessage(message);

                                notifications.add(notificationInfo);
                            }
//                            if (!userSnapshot.getData().get("eventUid").equals(FirebaseUtil.getUserId(context))) {
//                                continue;
//                            }

                        }
                        Log.d(TAG, "getNotificationInfos isSuccessful" + notifications);
                        Message message = Message.obtain();
                        message.obj = notifications;
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {
                        Log.e(TAG, "getNotificationInfos complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getNotificationInfos Failure");
                });
    }

    /**
     * save All UserName By Id
     * @param context
     */
    public static void saveAllUserNameById(Context context) {
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String userName = (java.lang.String) documentSnapshot.getData().get("displayName");
                            String userId = documentSnapshot.getId();
                            saveDataInSp(context, userId, userName);
                        }
                    } else {
                        Log.e(TAG, "getEventParticipants complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getEventParticipants Failure");
                });
    }

    /**
     * getEventStatusByEventId
     * @param eventId
     */
    public static void getEventStatusByEventId(Context context, String eventId) {
        Log.d(TAG, "getEventStatusByEventId start : "+eventId);
        FirebaseFirestore.getInstance().collection("events")
                .document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getEventStatus isSuccessful" + task.getResult().getData().toString());
//                        Event event = null;
                        try {
                            event1 = (Event) JsonUtil.mapToObject(task.getResult().getData(), Event.class);
                        } catch (Exception e) {
                            Log.d(TAG, "null event1");
                            e.printStackTrace();
                        }
                        //get current timestamp
                        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                        Date date = new Date(System.currentTimeMillis());

                        // check is event ended
                        if(event1.getEndDate().toDate().before(date)){
                            Log.d(TAG, "event is ended");
                            event1.setStatus("ended");
                            // set event status as ended
                            FirebaseFirestore.getInstance().collection("events")
                                    .document(eventId).set(event1);
                        }else{
                            Log.d(TAG, "event is running");
                        }
                        ////
                        SharedPreferences sp = context.getSharedPreferences("status", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("status", event1.getStatus());
                        editor.commit();
                    } else {
                        Log.e(TAG, "getEvent status complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getEvent status Failure");
                });
    }

    /**
     * save All UserId By EventId
     * @param context
     */
    public static void saveAllUserIdByEventId(Context context) {
        FirebaseFirestore.getInstance().collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "saveAllUserIdByEventId complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String userId = (java.lang.String) documentSnapshot.getData().get("userId");
                            String eventId = documentSnapshot.getId();
                            saveUserIdByEventIdInSp(context, eventId, userId);
                        }
                    } else {
                        Log.e(TAG, "saveAllUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "saveAllUserIdByEventId Failure");
                });
    }

    /**
     * get current uId
     *
     * @return uid
     */
    public static String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("event", Context.MODE_PRIVATE);
        return sp.getString("userId",null);
    }

    /**
     * get current uId
     *
     * @param context context
     * @param userId userId
     * @return userName
     */
    public static String getUserNameById(Context context, String userId) {
        if (context == null) {
            context = MyApplication.getInstance();
        }
        SharedPreferences sp = context.getSharedPreferences("event", Context.MODE_PRIVATE);
        return sp.getString(userId,null);
    }

    /**
     * save userId in sp
     * @param context context
     * @param userId userId
     * @return save result
     */
    public static boolean saveUserId(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences("event", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userId", userId);
        return editor.commit();
    }

    /**
     * save userId in sp
     * @param context context
     *
     * @return save result
     */
    public static boolean saveDataInSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("event", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * save userId By EventId in sp
     * @param context context
     *
     * @return save result
     */
    public static boolean saveUserIdByEventIdInSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("eventId", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * get current uId
     *
     * @param context context
     * @param eventId eventId
     * @return userName
     */
    public static String getUserIdByEventIdd(Context context, String eventId) {
        if (context == null) {
            context = MyApplication.getInstance();
        }
        SharedPreferences sp = context.getSharedPreferences("eventId", Context.MODE_PRIVATE);
        return sp.getString(eventId,null);
    }
    public static void getUserIdByEventId(Context context, String eventId) {
        FirebaseFirestore.getInstance().collection("events").document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "getUserIdByEventId complete success");
                        try {
                            event1 = (Event) JsonUtil.mapToObject(task.getResult().getData(), Event.class);
                        } catch (Exception e) {
                            Log.d(TAG, "null event1");
                            e.printStackTrace();
                        }
                        SharedPreferences sp = context.getSharedPreferences("getUserIdByEventId", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("getUserIdByEventId", event1.getUserId());
                        editor.commit();
                    } else {
                        Log.e(TAG, "getUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "getUserIdByEventId Failure");
                });
    }
    /**
     *
     * @param eventId
     * @param eventUserId
     * @param requestTime
     * @param requestUserId
     */
    public static void insertNotification(String eventId, String eventUserId, String requestTime
            , String requestUserId, String message) {
        Log.d(TAG, "updateNotification begin");
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("eventId", eventId);
        notificationMap.put("eventUid", eventUserId);
        notificationMap.put("requestUserId", requestUserId);
        notificationMap.put("requestTime", new Timestamp(Long.parseLong(requestTime), 0));
        notificationMap.put("message", message);

        FirebaseFirestore.getInstance().collection("notifications").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "saveAllRequestUserIdByEventId complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String temRequestUserId = (java.lang.String) documentSnapshot.getData().get("requestUserId");
                            String temEventId = (java.lang.String) documentSnapshot.getData().get("eventId");
                            if (temRequestUserId.equals(requestUserId) && temEventId.equals(eventId)) {
                                return;
                            }
                        }
                        //send email
                        //code:
                        //
                        insertNotificationMap(notificationMap);
                    } else {
                        Log.e(TAG, "saveAllUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "saveAllUserIdByEventId Failure");
                });

    }

    /**
     *
     * @param notificationMap
     */
    public static void insertNotificationMap(Map<String, Object> notificationMap) {
        Log.d(TAG, "insertNotificationMap begin");
        FirebaseFirestore.getInstance().collection("notifications")
                .document()
                .set(notificationMap)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "notifications update Complete");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "notifications update isSuccessful");
                    } else {
                        Log.d(TAG, "notifications update fail");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "notifications update success");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "notifications update fail" + e);
                });
    }

    /**
     *
     * @param eventId
     * @param status
     * @param userId
     */
    public static void insertRegistration(String eventId, String status, String userId) {
        Log.d(TAG, "insertRegistration begin");
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("eventId", eventId);
        notificationMap.put("status", status);
        notificationMap.put("userId", userId);

        FirebaseFirestore.getInstance().collection("registrations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "insertRegistration complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String temRequestUserId = (java.lang.String) documentSnapshot.getData().get("userId");
                            String temEventId = (java.lang.String) documentSnapshot.getData().get("eventId");
                            if (temRequestUserId.equals(userId) && temEventId.equals(eventId)) {
                                return;
                            }
                        }
                        insertRegistrationMap(notificationMap);
                    } else {
                        Log.e(TAG, "saveAllUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "saveAllUserIdByEventId Failure");
                });

    }

    /**
     *
     * @param notificationMap
     */
    public static void insertRegistrationMap(Map<String, Object> notificationMap) {
        Log.d(TAG, "insertRegistrationMap begin");
        FirebaseFirestore.getInstance().collection("registrations")
                .document()
                .set(notificationMap)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Registration update Complete");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Registration update isSuccessful");
                    } else {
                        Log.d(TAG, "Registration update fail");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Registration update success");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "Registration update fail" + e);
                });
    }

    /**
     *
     * @param eventId
     * @param requestUserId
     */
    public static void deleteNotification(String eventId, String requestUserId) {
        FirebaseFirestore.getInstance().collection("notifications").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "saveAllRequestUserIdByEventId complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String temRequestUserId = (java.lang.String) documentSnapshot.getData().get("requestUserId");
                            String temEventId = (java.lang.String) documentSnapshot.getData().get("eventId");
                            if (temRequestUserId.equals(requestUserId) && temEventId.equals(eventId)) {
                                deleteNotificationMap(documentSnapshot.getId());
                                return;
                            }
                        }
                    } else {
                        Log.e(TAG, "saveAllUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "saveAllUserIdByEventId Failure");
                });
    }

    /**
     *
     * @param documentId
     */
    public static void deleteNotificationMap(String documentId) {
        Log.d(TAG, "deleteNotificationMap begin");
        FirebaseFirestore.getInstance().collection("notifications")
                .document(documentId).delete()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "deleteNotificationMap Complete");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteNotificationMap isSuccessful");
                    } else {
                        Log.d(TAG, "deleteNotificationMap fail");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "deleteNotificationMap success");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "deleteNotificationMap fail" + e);
                });
    }

    /**
     *
     * @param eventId
     * @param userId
     */
    public static void deleteRegistration(String eventId, String userId, Boolean status) {
        Log.d(TAG, "deleteRegistration begin");
        //update event participantsNumber
        Log.d(TAG, "Registration Status = "+status);
        if(status){
            Log.d(TAG, "delete approved Registration begin");
            Log.d(TAG, "update ParticipantsNumber:"+event1.getParticipantsNumber());
            event1.setParticipantsNumber(event1.getParticipantsNumber()-1);
            FirebaseFirestore.getInstance().collection("events")
                    .document(eventId).set(event1);
            Log.d(TAG, "update ParticipantsNumber:"+event1.getParticipantsNumber());
        }else{
            Log.d(TAG, "delete pending Registration begin");
        }

        FirebaseFirestore.getInstance().collection("registrations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "insertRegistration complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String temRequestUserId = (java.lang.String) documentSnapshot.getData().get("userId");
                            String temEventId = (java.lang.String) documentSnapshot.getData().get("eventId");
                            if (temRequestUserId.equals(userId) && temEventId.equals(eventId)) {
                                deleteRegistrationMap(documentSnapshot.getId());
                                return;
                            }
                        }
                    } else {
                        Log.e(TAG, "saveAllUserIdByEventId complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "saveAllUserIdByEventId Failure");
                });
    }

    /**
     *
     * @param documentId
     */
    public static void deleteRegistrationMap(String documentId) {
        Log.d(TAG, "deleteRegistrationMap begin");
        FirebaseFirestore.getInstance().collection("registrations")
                .document(documentId).delete()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "deleteRegistrationMap Complete");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteRegistrationMap isSuccessful");
                    } else {
                        Log.d(TAG, "deleteRegistrationMap fail");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "deleteRegistrationMap success");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "deleteRegistrationMap fail" + e);
                });
    }

    /**
     *
     * @param eventId
     * @param userId
     */
    public static void modifyRegistration(String eventId, String userId) {
        Log.d(TAG, "modifyRegistration begin");
        Map<String, Object> registrationMap = new HashMap<>();
        registrationMap.put("eventId", eventId);
        registrationMap.put("status", "approved");
        registrationMap.put("userId", userId);
        //update event participantsNumber
        event1.setParticipantsNumber(event1.getParticipantsNumber()+1);
        FirebaseFirestore.getInstance().collection("events")
                .document(eventId).set(event1);
        FirebaseFirestore.getInstance().collection("registrations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "modifyRegistration complete success");
                        for (DocumentSnapshot documentSnapshot: task.getResult().getDocuments()) {
                            String temRequestUserId = (java.lang.String) documentSnapshot.getData().get("userId");
                            String temEventId = (java.lang.String) documentSnapshot.getData().get("eventId");
                            if (temRequestUserId.equals(userId) && temEventId.equals(eventId)) {
                                modifyRegistrationMap(documentSnapshot.getId(), registrationMap);
                                return;
                            }
                        }
                    } else {
                        Log.e(TAG, "modifyRegistration complete failed");
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "modifyRegistration Failure");
                });
    }

    /**
     *
     * @param documentId
     * @param registrationMap
     */
    public static void modifyRegistrationMap(String documentId, Map<String, Object> registrationMap) {
        Log.d(TAG, "modifyRegistrationMap begin");
        FirebaseFirestore.getInstance().collection("registrations")
                .document(documentId).set(registrationMap)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "modifyRegistrationMap Complete");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "modifyRegistrationMap isSuccessful");
                    } else {
                        Log.d(TAG, "modifyRegistrationMap fail");
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "modifyRegistrationMap success");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "modifyRegistrationMap fail" + e);
                });
    }
}