package xmq.track.base;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author xmqyeah
 * @CreateDate 2021/12/13 22:20
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockLog.d("onCreate(MockLog)");
        Log.i(getClass().getSimpleName(), "onCreate(Log)");
    }

    @Override
    protected void onStart() {
        MockLog.d("onStart()");
        super.onStart();
    }

}
