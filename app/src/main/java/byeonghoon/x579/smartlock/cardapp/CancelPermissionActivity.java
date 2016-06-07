package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CancelPermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_permission);
    }

    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.expire(getApplicationContext(), "permission.cancel");
    }

    public void buttonCancelStartPressed() {
        TextView direction = (TextView) findViewById(R.id.cancel_perm_direction);
        direction.setText("Tag lock to cancel permission");
        SessionStorage.set(getApplicationContext(), "permission.cancel", "1");
    }

    public void buttonCancelPressed() {
        SessionStorage.expire(getApplicationContext(), "permission.cancel");
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
