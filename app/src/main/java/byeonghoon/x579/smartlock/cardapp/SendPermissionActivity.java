package byeonghoon.x579.smartlock.cardapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SendPermissionActivity extends AppCompatActivity {

    EditText perm_date;
    EditText perm_time;
    EditText perm_duration;

    Calendar myCalendar;
    PickerListener listener;

    int card_id;


    class PickerListener implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
        @Override public void onDateSet(DatePicker picker, int year, int monthofyear, int dayofmonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthofyear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofmonth);
            updateLabel();
        }

        @Override public void onTimeSet(TimePicker picker, int hour, int minute) {
            myCalendar.set(Calendar.HOUR, hour);
            myCalendar.set(Calendar.MINUTE, minute);
            updateLabel();
        }

        private void updateLabel() {
            SimpleDateFormat format_date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            SimpleDateFormat format_time = new SimpleDateFormat("hh:mm", Locale.getDefault());

            perm_date.setText(format_date.format(myCalendar.getTime()));
            perm_time.setText(format_time.format(myCalendar.getTime()));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_permission);
        perm_date = (EditText) findViewById(R.id.permission_date);
        perm_time = (EditText) findViewById(R.id.permission_time);
        perm_duration = (EditText) findViewById(R.id.permission_duration);
        listener = new PickerListener();

        perm_date.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new DatePickerDialog(SendPermissionActivity.this, listener, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        perm_time.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new TimePickerDialog(SendPermissionActivity.this, listener, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true).show();
            }
        });

        card_id = getIntent().getIntExtra("Card_ID", 0);
    }


    public void buttonAcceptPermPressed() {
        TextView text_view = (TextView) findViewById(R.id.receive_perm_direction);
        Button button = (Button) findViewById(R.id.button_accept_permission);
        perm_date.setEnabled(false);
        perm_time.setEnabled(false);
        perm_duration.setEnabled(false);
        button.setEnabled(false);

        int duration;
        try {
            duration = Integer.parseInt(perm_duration.getText().toString());
        } catch (NumberFormatException exception) {
            duration = 1;
        }
        if(duration <= 1) duration = 1;
        if(duration >= 60) duration = 60;

        final long start = System.currentTimeMillis();

        text_view.setText("Tag lock within 3 minutes to enable");
        //Encrypt!
        String tempCode = String.valueOf(myCalendar.getTimeInMillis()) + "#" + String.valueOf(duration * 1000) + "#" + AccountStorage.GetAccount(getApplicationContext(), card_id);
        SessionStorage.set(getApplicationContext(), "permission.time.send.start", String.valueOf(start));
        SessionStorage.set(getApplicationContext(), "permission.time.send.duration", "180000");
        SessionStorage.set(getApplicationContext(), "permission.temporary.send.configure", "1");
        SessionStorage.set(getApplicationContext(), "permission.temporary.send.code", tempCode);


        //add notification for duration
        new AsyncTask<Void, Void, Void>() {
            @Override protected Void doInBackground(Void... unused) {
                while(System.currentTimeMillis() > start + 180000) {
                    if(!SessionStorage.exists(getApplicationContext(), "permission.time.send.start")) {
                        break;
                    }
                }
                return null;
            }
            @Override protected void onPostExecute(Void _void) {
                buttonBackPermPressed();
            }
        }.execute();
    }

    public void buttonBackPermPressed() {
        SessionStorage.expire(getApplicationContext(), "permission.time.send.start");
        SessionStorage.expire(getApplicationContext(), "permission.time.send.duration");
        SessionStorage.expire(getApplicationContext(), "permission.temporary.send.configure");
        SessionStorage.expire(getApplicationContext(), "permission.temporary.send.code");

        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
    }

}
