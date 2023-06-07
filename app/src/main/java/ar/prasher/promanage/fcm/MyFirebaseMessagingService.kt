package ar.prasher.promanage.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import ar.prasher.promanage.R
import ar.prasher.promanage.activities.IntroActivity
import ar.prasher.promanage.activities.MainActivity
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    //two types of messages data and notification message

    //notification messages only get inside here when app is in
    //the foreground
    //data message handled here even when app is in the bg
    //when we get message this function will be called
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "FROM : ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG,"Message Data Payload : ${remoteMessage.data}")
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotification(title, message)
        }

        remoteMessage.notification?.let {
            Log.d(TAG,"Message Notification Body : ${it.body}")
        }

    }

    private fun sendRegistrationToServer(token: String?) {

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG,"Refreshed Token : $token")
        sendRegistrationToServer(token)
    }


    //showing actual notification with an intent
    private fun sendNotification(title:String, message : String){
        val intent =if (FirestoreClass().getCurrentUserID().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,IntroActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP)//to keep activity on top


        //if user is in other application
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val channelID = resources.getString(R.string.default_notification_channel_id)

        val defaultSoundUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
        )
        val notificationBuilder = NotificationCompat.Builder(
            this,
            channelID
        ).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Title")
            .setContentText("Message")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelID,
                "Channel ProManage Title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }
}