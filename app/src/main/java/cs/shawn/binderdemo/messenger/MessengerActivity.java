package cs.shawn.binderdemo.messenger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import cs.shawn.binderdemo.R;
import cs.shawn.binderdemo.aidl.AidlService;

/**
 * Created by chenshao on 2017/12/26.
 * todo  没有做 permission
 */

public class MessengerActivity extends Activity {
    Messenger service;

    private Messenger messenger;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = new Messenger(iBinder);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    EditText messageEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_service_controller);

        // Watch for button clicks.
        Button button = findViewById(R.id.start);
        button.setOnClickListener(mStartListener);
        button = findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);
        button = findViewById(R.id.btn_send);
        button.setOnClickListener(sendListener);

        messenger = new Messenger(new MyHandler(this));
        messageEt = findViewById(R.id.messageEt);
    }

    private View.OnClickListener sendListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String msg = messageEt.getText().toString();

            Message message = Message.obtain(null, 10);
            message.replyTo = messenger;

            Bundle bundle = new Bundle();
            bundle.putString("msg_content", msg);
            message.setData(bundle);
            try {
                service.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private View.OnClickListener mStartListener = new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(MessengerService.class.getName());
            Intent eintent = new Intent(getExplicitIntent(MessengerActivity.this, intent));
            bindService(eintent, connection, BIND_AUTO_CREATE);

            // 或者直接显式启动
        }
    };

    private View.OnClickListener mStopListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Cancel a previous call to startService().  Note that the
            // service will not actually stop at this point if there are
            // still bound clients.
            stopService(new Intent(MessengerActivity.this, AidlService.class));
        }
    };

    private Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,
                0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName,
                className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    static class MyHandler extends Handler {

        private final WeakReference<Activity> mActivity;
        private TextView messageTx;

        public MyHandler(Activity activity) {

            mActivity = new WeakReference(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Activity activity = mActivity.get();
            if (msg.what == 20 && activity != null) {

                String message = msg.getData().getString("msg_content");
                messageTx = activity.findViewById(R.id.tv);
                messageTx.setText(message);
            }

        }
    }

}
