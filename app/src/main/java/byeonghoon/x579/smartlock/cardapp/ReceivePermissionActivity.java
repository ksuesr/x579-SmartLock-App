package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReceivePermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_permission);
    }

    public void buttonAcceptPermPressed(View v) {
        final TextView text_view = (TextView) findViewById(R.id.receive_perm_direction);
        EditText text_1 = (EditText) findViewById(R.id.permission_01);
        Button button = (Button) findViewById(R.id.button_accept_receive_permission);
        final long start = System.currentTimeMillis();
        text_1.setEnabled(false);
        int remainHour = 3;
        int remainMinute = 0;

        text_view.setText(String.format("Tag lock within 3 minutes"));
        SessionStorage.set(getApplicationContext(), "permission.temporary.receive.code", text_1.getText().toString());
        SessionStorage.set(getApplicationContext(), "permission.time.receive.start", String.valueOf(start));
        SessionStorage.set(getApplicationContext(), "permission.time.receive.duration", "180000");

        button.setEnabled(false);

        //add notification for duration
        new AsyncTask<Void, Void, Void>() {
            @Override protected Void doInBackground(Void... unused) {
                while(System.currentTimeMillis() <= start + 180000) {
                    if(!SessionStorage.exists(getApplicationContext(), "permission.time.receive.start")) {
                        break;
                    }
                }
                return null;
            }
            @Override protected void onPostExecute(Void _void) {
                afterFinish();
            }
        }.execute();
    }

    public void buttonBackPermPressed() {
        afterFinish();
    }

    private void afterFinish() {
        SessionStorage.expire(getApplicationContext(), "permission.time.receive.start");
        SessionStorage.expire(getApplicationContext(), "permission.time.receive.duration");
        SessionStorage.expire(getApplicationContext(), "permission.temporary.receive.code");

        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
    }

}
