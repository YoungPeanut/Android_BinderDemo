package cs.shawn.binderdemo.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

/**
 * Created by chenshao on 2017/12/28.
 */

public class MessengerService extends Service{

    private final Messenger messager = new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msg) {

            int what = msg.what;
            Bundle bundle = msg.getData ();

            switch (what) {

                case 10:

                    String content = bundle.getString ("msg_content");
                    bundle.putString ("msg_content", content + "_callback");

                    Message message = Message.obtain (null,20);
                    message.setData (bundle);
                    try {
                        msg.replyTo.send (message);

                    } catch (Exception e) {
                        e.printStackTrace ();
                    }
                    break;

                default:
                    break;
            }

        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messager.getBinder();
    }
}
