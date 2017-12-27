package cs.shawn.binderdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by chenshao on 2017/12/26.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Watch for button clicks.
        Button button = findViewById(R.id.start);
        button.setOnClickListener(mStartListener);
        button = findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);
    }

    private View.OnClickListener mStartListener = new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,Controller.class));
        }
    };

    private View.OnClickListener mStopListener = new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,Binding.class));

        }
    };
}
