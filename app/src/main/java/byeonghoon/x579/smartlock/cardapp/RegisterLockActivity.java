package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RegisterLockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_lock);
    }

    public void buttonStartRegPressed() {

    }

    public void buttonCancelRegPressed() {
        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
    }

}
