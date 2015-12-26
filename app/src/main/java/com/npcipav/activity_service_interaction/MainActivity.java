package com.npcipav.activity_service_interaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String LOGTAG = "MainActivity";

    private TextView mTextView;

    private Messenger mServiceMessenger = null;
    private ServiceConnection mConnection = this;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private boolean mIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "Activity started");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // View initialization.
        mTextView = (TextView)findViewById(R.id.textView);

        autobind();
    }

    /**
     * Check if the service is running. If the service is running when the activity starts, we want
     * to automatically bind to it.
     */
    private void autobind() {
        if (!BackgroundService.isRunning()) {
            Intent intent = new Intent(this, BackgroundService.class);
            startService(intent);
        }
        doBindService();
    }

    /**
     * Bind Service to this Activity.
     */
    private void doBindService() {
        Intent intent = new Intent(this, BackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Unbind Service to this Activity.
     */
    private void doUnbindService() {
        if (mIsBound && mServiceMessenger != null) {
            try {
                // Request the service to unregister the activity.
                Message msg = Message.obtain(null, BackgroundService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                // That means that the service get crashed. We just do nothing.
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Send request to update news and launch view updating from Service.
     */
    private void requestNews() {
        Log.d(LOGTAG, "Requesting news.");
        if (mIsBound && mServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, BackgroundService.MSG_GET_NEWS);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                // As usual we do nothing.
            }
        }
    }

    /**
     * Deserialize news from the service.
     * @param bundle The serializable data which we need to turn into news list.
     * @return News list from the server.
     */
    private List<News> unpackNews(Bundle bundle) {
        // List<News> is LinkedList! If it matters, remember this!
        Log.d(LOGTAG, "Unpacking news.");
        List<News> news = (LinkedList<News>)bundle.getSerializable("value");
        Log.d(LOGTAG, "News unpacked.");
        return news;
    }

    /**
     * Update news list in the view.
     * @param news News we need to present in view.
     */
    private void updateNewsUI(List<News> news) {
        // Update UI from serialized data.
        for (News currentNews : news) {
            String str = "Date: " + currentNews.getDate().toString() + "\nSource: " +
                    currentNews.getSource() + "\nReason: " + currentNews.getReason() +
                    "\nBounds: " + currentNews.getBounds() + "\n\n";
            mTextView.append(str);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, BackgroundService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // They said that service should got crashed before we could do anything with it.
            // So let it be blank.
        }
        requestNews(); // TODO!
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been UNEXPECTEDLY disconnected -
        // process crashed.
        mServiceMessenger = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e(LOGTAG, "Failed to unbind from the service ", t);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles incoming messages from Service.
     */
    class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "Handling message.");
            switch (msg.what) {
                case BackgroundService.MSG_POST_NEW_DATA:
                    // Get serializable news from service.
                    Bundle bundle = msg.getData();
                    // Manage serializable object.
                    updateNewsUI(unpackNews(bundle));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
