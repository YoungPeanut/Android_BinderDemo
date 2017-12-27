package cs.shawn.binderdemo.aidl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import cs.shawn.binderdemo.Controller;
import cs.shawn.binderdemo.R;

/**
 * Created by chenshao on 2017/12/26.
 *
 * service that runs in a different process
 * use IPC to interact with it.
 * bind 和 start两种方式
 */

public class RemoteService extends Service {
    NotificationManager mNM;

    @Override
    public void onCreate() {
        Log.i("RemoteService", "onCreate");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("RemoteService", "Received start id " + startId + ": " + intent);
        showNotification("remote_service by start");

        return START_NOT_STICKY;
    }

    /********* below for bind *************/

    private static final int REPORT_MSG = 1;
    final RemoteCallbackList<IRemoteServiceCallback> callbackList = new RemoteCallbackList<>();

    MyHandler mHandler = new MyHandler();
    // main thread
    class MyHandler extends Handler{
        int mValue = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case REPORT_MSG: {

                    int value = ++mValue;

                    // Broadcast to all clients the new value.
                    final int N = callbackList.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                            callbackList.getBroadcastItem(i).valueChanged(value);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    callbackList.finishBroadcast();

                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1000);
                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(CharSequence text) {
//        CharSequence text = getText(R.string.remote_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.remote_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.remote_service_started, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        mHandler.removeMessages(REPORT_MSG);
        mHandler.sendEmptyMessage(REPORT_MSG);

        if (IRemoteService.class.getName().equals(intent.getAction())) {
            showNotification("remote_service by bind");
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction())) {
            showNotification("remote_service by bind secondary");
            return mSecondaryBinder;
        }
        return null;
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) callbackList.register(cb);
        }
        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) callbackList.unregister(cb);
        }
    };

    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        public int getPid() {
            return Process.myPid();
        }
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                               float aFloat, double aDouble, String aString) {
        }
    };

    @Override
    public void onDestroy() {
         mNM.cancel(R.string.remote_service_started);

        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();

        // Unregister all callbacks.
        callbackList.kill();

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG);
    }

}
