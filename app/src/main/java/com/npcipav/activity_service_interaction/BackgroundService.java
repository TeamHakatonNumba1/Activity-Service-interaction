package com.npcipav.activity_service_interaction;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

public class BackgroundService extends Service {

    private static final String LOGTAG = "BackgroundService";
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_GET_NEWS = 3;
    static final int MSG_POST_NEW_DATA = 1;

    final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private Messenger mClientMessenger = null;
    private NotificationManager mNotificationManager;
    private static boolean mIsRunning = false;
    private NewsCollector mNewsCollector;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Service Started");
        // TODO! Set scheduled news updating.
        mIsRunning = true;
    }

    public static boolean isRunnign() {
        return mIsRunning;
    }

    /**
     * Displays a notification in the notification bar.
     */
    private void showNotification() {}

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
        // TODO! Stop scheduled tasks.
        mNotificationManager.cancel(R.string.service_started);
        Log.i(LOGTAG, "Service Stopped");
        mIsRunning = false;
    }
}
