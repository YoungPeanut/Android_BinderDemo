package cs.shawn.binderdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cs.shawn.binderdemo.aidl.IRemoteService;
import cs.shawn.binderdemo.aidl.IRemoteServiceCallback;
import cs.shawn.binderdemo.aidl.ISecondary;
import cs.shawn.binderdemo.aidl.RemoteService;

/**
 * Created by chenshao on 2017/12/26.
 *
 * Activity 通过aidl与 另一个进程的Service通信
 *
 *
 *
 */

public class Binding extends Activity{
    /** The primary interface we will be calling on the service. */
    IRemoteService mService = null;
    /** Another interface we use on the service. */
    ISecondary mSecondaryService = null;

    Button mKillButton;
    TextView mCallbackText;

    private boolean mIsBound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_service_binding);

        Button button = findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        mKillButton = findViewById(R.id.kill);
        mKillButton.setOnClickListener(mKillListener);
        mKillButton.setEnabled(false);

        mCallbackText = findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");
    }

    private View.OnClickListener mBindListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Establish a couple connections with the service, binding
            // by interface names.  This allows other applications to be
            // installed that replace the remote service by implementing
            // the same interface.
            Intent intent = new Intent(Binding.this, RemoteService.class);
            intent.setAction(IRemoteService.class.getName());
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            intent.setAction(ISecondary.class.getName());
            bindService(intent, mSecondaryConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding.");
        }
    };
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            communicating with our service through an AIDL interface
            mService = IRemoteService.Stub.asInterface(iBinder);

            mKillButton.setEnabled(true);
            mCallbackText.setText("Attached.");
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();

                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            Toast.makeText(Binding.this, "remote_service_connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mKillButton.setEnabled(false);
            mCallbackText.setText("Disconnected.");
            Toast.makeText(Binding.this, "remote_service_disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Class for interacting with the secondary interface of the service.
     */
    private ServiceConnection mSecondaryConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mSecondaryService = ISecondary.Stub.asInterface(iBinder);
            mKillButton.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            mSecondaryService = null;
            mKillButton.setEnabled(false);
        }

        @Override
        public void onBindingDied(ComponentName name) {

            mSecondaryService = null;
            mKillButton.setEnabled(false);
        }
    };

    private View.OnClickListener mUnbindListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mIsBound) {
                //  time to unregister.
                if (mService != null) {
                    try {
                        mService.unregisterCallback(mCallback);
                    } catch (RemoteException e) {
                        // There is nothing special we need to do if the service has crashed.
                    }
                }

                // Detach our existing connection.
                unbindService(mConnection);
                unbindService(mSecondaryConnection);
                mKillButton.setEnabled(false);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }
    };

    private View.OnClickListener mKillListener = new View.OnClickListener() {
        public void onClick(View v) {
            // To kill the process hosting our service, we need to know its
            // PID.  Conveniently our service has a call that will return
            // to us that information.
            if (mSecondaryService != null) {
                try {
                    int pid = mSecondaryService.getPid();
                    // Note that, though this API allows us to request to
                    // kill any process based on its PID, the kernel will
                    // still impose standard restrictions on which PIDs you
                    // are actually able to kill.  Typically this means only
                    // the process running your application and any additional
                    // processes created by that app as shown here; packages
                    // sharing a common UID will also be able to kill each
                    // other's processes.
                    Process.killProcess(pid);
                    mCallbackText.setText("Killed service process.");
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    // Recover gracefully from the process hosting the
                    // server dying.
                    // Just for purposes of the sample, put up a notification.
                    Toast.makeText(Binding.this,
                            "remote_call_failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        /**
         * Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread
         */
        @Override
        public void valueChanged(int value) throws RemoteException {

            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };

    private static final int BUMP_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };
}
