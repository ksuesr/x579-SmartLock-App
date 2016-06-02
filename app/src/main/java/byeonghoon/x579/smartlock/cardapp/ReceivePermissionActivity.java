package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class ReceivePermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_permission);
    }

    public void buttonAcceptPermPressed() {
        TextView text_view = (TextView) findViewById(R.id.receive_perm_direction);
        EditText text_1 = (EditText) findViewById(R.id.permission_01);
        EditText text_2 = (EditText) findViewById(R.id.permission_02);
        EditText text_3 = (EditText) findViewById(R.id.permission_03);
        EditText text_4 = (EditText) findViewById(R.id.permission_04);
        text_1.setEnabled(false);
        text_2.setEnabled(false);
        text_3.setEnabled(false);
        text_4.setEnabled(false);

        text_view.setText("Tag lock within 3 minutes");
        SessionStorage.set(getApplicationContext(), "permission.frag.1", text_1.getText().toString());
        SessionStorage.set(getApplicationContext(), "permission.frag.2", text_2.getText().toString());
        SessionStorage.set(getApplicationContext(), "permission.frag.3", text_3.getText().toString());
        SessionStorage.set(getApplicationContext(), "permission.frag.4", text_4.getText().toString());
        SessionStorage.set(getApplicationContext(), "permission.time", String.valueOf(System.currentTimeMillis()));

        //add notification for 3 min.
    }

    public void buttonCancelPermPressed() {
        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
    }
}
