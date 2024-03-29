package com.asergeev.imhere.activity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.asergeev.imhere.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 8/7/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mMessagesReference;
    private FirebaseAuth mFirebaseAuth;
    private static final String TAG = "GeofenceTransitions";
    private String a;

    public GeofenceTransitionsIntentService() {

        // Use the TAG to name the worker thread.

        super(TAG);

    }



    /**

     * Handles incoming intents.

     * @param intent sent by Location Services. This Intent is provided to Location

     *               Services (inside a PendingIntent) when addGeofences() is called.

     */

    @Override

    protected void onHandleIntent(Intent intent) {
        SharedPreferences pref1 = getSharedPreferences("Pref", MODE_PRIVATE);
        a= pref1.getString("Code", "");
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {


            return;

        }



        // Get the transition type.

        int geofenceTransition = geofencingEvent.getGeofenceTransition();



        // Test that the reported transition was of interest.

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
                ) {



            // Get the geofences that were triggered. A single event can trigger multiple geofences.

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();



            // Get the transition details as a String.

            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,

                    triggeringGeofences);



            // Send notification and log the transition details.

            sendNotification(geofenceTransitionDetails);
            mMessagesReference = mDatabase.getReference().child("messages");
            Message message = new Message(geofenceTransitionDetails, "Я тут " , a);
            mMessagesReference.push().setValue(message);
            Log.i(TAG, geofenceTransitionDetails);

        } else {

            // Log the error.

            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));

        }


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){

            // Get the geofences that were triggered. A single event can trigger multiple geofences.

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();



            // Get the transition details as a String.

            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,

                    triggeringGeofences);



            // Send notification and log the transition details.

            sendNotification(geofenceTransitionDetails);
            mMessagesReference = mDatabase.getReference().child("messages");
            Message message = new Message(geofenceTransitionDetails, "Я вышел" , a);
            mMessagesReference.push().setValue(message);
            Log.i(TAG, geofenceTransitionDetails);
        } else {


            // Log the error.

            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));


        }

    }



    /**

     * Gets transition details and returns them as a formatted string.

     *

     * @param geofenceTransition    The ID of the geofence transition.

     * @param triggeringGeofences   The geofence(s) triggered.

     * @return                      The transition details formatted as String.

     */

    private String getGeofenceTransitionDetails(

            int geofenceTransition,

            List<Geofence> triggeringGeofences) {



        String geofenceTransitionString = getTransitionString(geofenceTransition);



        // Get the Ids of each geofence that was triggered.

        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();

        for (Geofence geofence : triggeringGeofences) {

            triggeringGeofencesIdsList.add(geofence.getRequestId());

        }

        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);



        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;

    }



    /**

     * Posts a notification in the notification bar when a transition is detected.

     * If the user clicks the notification, control goes to the MainActivity.

     */

    private void sendNotification(String notificationDetails) {

        // Create an explicit content Intent that starts the main Activity.

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);



        // Construct a task stack.

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);



        // Add the main Activity to the task stack as the parent.

        stackBuilder.addParentStack(MainActivity.class);



        // Push the content Intent onto the stack.

        stackBuilder.addNextIntent(notificationIntent);



        // Get a PendingIntent containing the entire back stack.

        PendingIntent notificationPendingIntent =

                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



        // Get a notification builder that's compatible with platform versions >= 4

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);



        // Define the notification settings.

        builder.setSmallIcon(R.mipmap.gps)

                // In a real app, you may want to use a library like Volley

                // to decode the Bitmap.

                .setLargeIcon(BitmapFactory.decodeResource(getResources(),

                        R.mipmap.gps))

                .setColor(Color.RED)

                .setContentTitle(notificationDetails)



                .setContentIntent(notificationPendingIntent);



        // Dismiss notification once the user touches it.

        builder.setAutoCancel(true);



        // Get an instance of the Notification manager

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        // Issue the notification

        mNotificationManager.notify(0, builder.build());

    }



    /**

     * Maps geofence transition types to their human-readable equivalents.

     *

     * @param transitionType    A transition type constant defined in Geofence

     * @return                  A String indicating the type of transition

     */

    private String getTransitionString(int transitionType) {

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:

                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:

                return getString(R.string.geofence_transition_exited);

            default:

                return getString(R.string.unknown_geofence_transition);

        }

    }
}
