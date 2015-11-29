package com.npcipav.activity_service_interaction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private static final String LOGTAG = "BackgroundService";

    // Service codes:
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_GET_NEWS = 3;
    // Activity codes:
    static final int MSG_POST_NEW_DATA = 1;

    final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private Timer mTimer;
    private Messenger mClientMessenger = null;
    private NotificationManager mNotificationManager;
    private static boolean mIsRunning = false;
    private NewsCollector mNewsCollector;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Service Started");
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNewsCollector = new NewsCollector();
        // Extract updating time from preferences.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = preferences.getInt("update_time", 0);
        // Set timer for automatic news updating.
        mTimer = new Timer();
        // We're gonna repeat our task updating news, each <mins> minutes.
        mTimer.scheduleAtFixedRate(new NewsUpdateTimerTask(), 0, mins * 60 * 1000);
        mIsRunning = true;
    }

    public static boolean isRunnign() {
        return mIsRunning;
    }

    /**
     * Displays a notification in the notification bar.
     */
    private void showNotification(News news) {
        String msg = "Showing news notification. Date: " + news.getDate().toString() + "; Source :"
                + news.getSource() + "; Reason: " + news.getReason() + "; Bounds: "
                + news.getBounds();
        Log.d(LOGTAG, msg);
        // TODO! Using NotificationManager show notification.
    }

    /**
     * Serializes news list.
     * @param news The list of news we want to serialize.
     * @return The serializable news object.
     */
    private Serializable serializeNews(List<News> news) {
        return new Serializable() {
            // TODO! Idk what i should put here.
        };
    }

    /**
     * Sends serialized news to the activity.
     */
    private void sendNewsToUI(Serializable data) {
        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", data);
            Message msg = Message.obtain(null, MSG_POST_NEW_DATA);
            msg.setData(bundle);
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            // That means that client is dead so we just delete it then.
            mClientMessenger = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) mTimer.cancel();
        mNotificationManager.cancelAll();
        Log.i(LOGTAG, "Service Stopped");
        mIsRunning = false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles incoming messages from the activity.
     */
    class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "handleMessage" + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClientMessenger = msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null;
                    break;
                case MSG_GET_NEWS:
                    mNewsCollector.refreshNews();
                    List<News> news = mNewsCollector.getNewsList();
                    sendNewsToUI(serializeNews(news));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Updates news and displays notifications.
     */
    class NewsUpdateTimerTask extends TimerTask {
        @Override
        synchronized public void run() {
            mNewsCollector.refreshNews();
            // TODO! Check related news and show them in notifications.
            // For now it shows all of them.
            for (News news : mNewsCollector.getNewsList()) {
                showNotification(news);
            }
        }
    }
}
