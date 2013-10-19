package me.code4fun.roboq.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import me.code4fun.roboq.Request;
import me.code4fun.roboq.Response;
import me.code4fun.roboq.SimpleExecutor;

import static me.code4fun.roboq.Request.*;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void foo() {

        new Request(GET, "http://www.baidu.com").execute(
                SimpleExecutor.instance,
                new Callback() {
            @Override
            public void onResponse(Request req, Response resp, Exception error) {
                Log.d("Roboq", resp.asText());
            }
        });
    }
}
