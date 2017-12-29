package cs.shawn.binderdemo.aidl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cs.shawn.binderdemo.R;

/**
 * Created by chenshao on 2017/12/26.
 *
 * Example of explicitly starting and stopping the remove service.
 * This demonstrates the implementation of a service that runs in a different process
 * 只是启动远程服务，没有交互
 */

public class Controller  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_service_controller);

        // Watch for button clicks.
        Button button = findViewById(R.id.start);
        button.setOnClickListener(mStartListener);
        button = findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);
    }

    private View.OnClickListener mStartListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Make sure the service is started.  It will continue running
            // until someone calls stopService().
            // We use an action code here, instead of explictly supplying
            // the component name, so that other packages can replace
            // the service.
            startService(new Intent(Controller.this, AidlService.class));
        }
    };

    private View.OnClickListener mStopListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Cancel a previous call to startService().  Note that the
            // service will not actually stop at this point if there are
            // still bound clients.
            stopService(new Intent(Controller.this, AidlService.class));
        }
    };
}
