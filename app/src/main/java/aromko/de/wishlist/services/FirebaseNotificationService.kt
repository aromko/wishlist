package aromko.de.wishlist.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import aromko.de.wishlist.R
import aromko.de.wishlist.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FirebaseNotificationService : FirebaseMessagingService(), LifecycleObserver {
    private var fFirebaseAuth: FirebaseAuth? = null
    private var fFirebaseUser: FirebaseUser? = null
    private var isAppInForeground = false

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notificationTitleVal: String?
        val notificationBodyVal: String?
        val userId: String?
        val allowedUsers: String?
        val wishListId: String?
        val wishListName: String?
        val wishName: String?
        val text: String?
        fFirebaseAuth = FirebaseAuth.getInstance()
        fFirebaseUser = fFirebaseAuth!!.currentUser

        if (isAppInForeground) {
            //Do Nothing in foreground
        } else {
            userId = remoteMessage.data["userId"]
            allowedUsers = remoteMessage.data["allowedUsers"]
            wishListId = remoteMessage.data["wishListId"]
            wishListName = remoteMessage.data["wishListName"]
            wishName = remoteMessage.data["wishName"]
            text = wishListName ?: wishName

            if (!fFirebaseUser!!.uid.equals(userId) && allowedUsers!!.contains(fFirebaseUser!!.uid)) {
                notificationTitleVal = remoteMessage.data["title"]
                notificationBodyVal = remoteMessage.data["body"]
                sendLocalNotification(
                    getTextFromResource(notificationTitleVal, wishListName),
                    getTextFromResource(notificationBodyVal, text),
                    wishListId
                )
            } else {
                Log.d(TAG, "Keine Nachricht an den Sender!!")
            }
        }

    }

    private fun getTextFromResource(notificationTitleVal: String?, wishListName: String?): String? {
        var notificationId = resources.getIdentifier(notificationTitleVal, "string", packageName)
        return getString(notificationId).replace("XXX", wishListName.toString())
    }

    private fun sendLocalNotification(notificationTitle: String?, notificationBody: String?, wishListId: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("WISHLIST_ID", wishListId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = "Wishlist"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.appicon_background)
                .setContentTitle(notificationTitle)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Wishlist Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(getNotificationId(), notificationBuilder.build())
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForegroundStart() {
        isAppInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onForegroundStop() {
        isAppInForeground = false
    }

    private fun getNotificationId(): Int {
        val rnd = Random()
        return 100 + rnd.nextInt(9000)
    }

    companion object {
        private const val TAG = "FMS"
    }
}