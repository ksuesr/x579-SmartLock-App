package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

public class MainEntryActivity extends AppCompatActivity {

    public static final String TAG = "MainEntryActivity";

    public static final int REQUEST_BASE = 1 << 10;
    public static final int REQUEST_REGISTER_LOCK = REQUEST_BASE + 1;
    public static final int REQUEST_RECEIVE_PERMISSION = REQUEST_BASE + 2;
    public static final int REQUEST_ABOUT = REQUEST_BASE + 3;
    public static final int REQUEST_FIRST_RUN = REQUEST_BASE + 10;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_entry);

        //TODO: search for preference storage for list of cards
        // if list doesn't exists, promote to add activity
        if(!SessionStorage.exists(this, "user.id")) {
            Toast.makeText(getApplicationContext(), "You need to sign in first", Toast.LENGTH_SHORT).show();
            Intent first_run = new Intent(this, FirstRunActivity.class);
            startActivityForResult(first_run, REQUEST_FIRST_RUN);
        }

        initView();
    }

    protected void initView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerLayout);

        mRecyclerAdapter = new LockViewAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);

        Log.i(TAG, "I'm ready :)");
    }

    protected void onPause() {
        super.onPause();
    }


    // Menu relations
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.main_menu_add:
                intent = new Intent(getApplicationContext(), RegisterLockActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER_LOCK);
                return true;
            case R.id.main_menu_receive_permission:
                intent = new Intent(getApplicationContext(), ReceivePermissionActivity.class);
                startActivityForResult(intent, REQUEST_RECEIVE_PERMISSION);
                return true;
            case R.id.main_menu_about:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivityForResult(intent, REQUEST_ABOUT);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    // Activity Result
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
       super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case REQUEST_REGISTER_LOCK:
                if(resultCode == RESULT_OK) {
                    String name = intent.getExtras().getString("title");
                    Toast.makeText(getBaseContext(), "New lock(" + name + ") registered", Toast.LENGTH_LONG).show();
                    mRecyclerView.requestLayout();
                } else {
                    Toast.makeText(getBaseContext(), "Register failed", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_FIRST_RUN:
                initView();
                mRecyclerView.requestLayout();
                break;
        }
    }
}
