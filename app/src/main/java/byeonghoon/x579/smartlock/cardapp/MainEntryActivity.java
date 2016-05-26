package byeonghoon.x579.smartlock.cardapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainEntryActivity extends AppCompatActivity {

    public static final String TAG = "MainEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_entry);

        Log.i(TAG, "I'm ready :)");
    }

    protected void onPause() {
        super.onPause();

    }


}
