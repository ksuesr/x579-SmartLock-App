package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class RegisterLockActivity extends AppCompatActivity {

    EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_lock);
    }

    public void buttonStartRegPressed(View v) {
        edit = (EditText) findViewById(R.id.lock_title);
        TextView text_view = (TextView) findViewById(R.id.register_direction);
        edit.setEnabled(false);
        text_view.setText("Tag lock to finish register");

        SessionStorage.set(getApplicationContext(), "register.action", "1");
        SessionStorage.set(getApplicationContext(), "register.action.title", edit.getText().toString());
        final long start = System.currentTimeMillis();

        new AsyncTask<Void, Void, Void>() {
            boolean isSucceed = false;

            @Override protected Void doInBackground(Void... unused) {
                while(System.currentTimeMillis() <= start + 180000) {
                    if(SessionStorage.exists(getApplicationContext(), "register.action.complete")) {
                        if("0".equals(SessionStorage.get(getApplicationContext(), "register.action.complete", "1"))) {
                            isSucceed = true;
                        }
                        break;
                    }
                }
                return null;
            }
            @Override protected void onPostExecute(Void _void) {
                if(isSucceed)
                    afterSuccess();
                else
                    afterFailed();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void buttonCancelRegPressed(View v) {
        afterFailed();
    }

    private void afterSuccess() {
        expireRegister();
        Intent okIntent = new Intent();
        okIntent.putExtra("title", edit.getText().toString());
        setResult(RESULT_OK, okIntent);
        finish();
    }

    private void afterFailed() {
        expireRegister();
        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
    }

    private void expireRegister() {
        SessionStorage.expire(getApplicationContext(), "register.action");
        SessionStorage.expire(getApplicationContext(), "register.action.title");
        SessionStorage.expire(getApplicationContext(), "register.action.complete");
        SessionStorage.expire(getApplicationContext(), "waiting.response");

    }

}
