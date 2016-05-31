package byeonghoon.x579.smartlock.cardapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;

public class MainEntryActivity extends AppCompatActivity {

    public static final String TAG = "MainEntryActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: search for preference storage for list of cards
        // if list doesn't exists, promote to add activity

        setContentView(R.layout.activity_main_entry);

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
        switch(item.getItemId()) {
            case R.id.main_menu_add:

                return true;
            case R.id.main_menu_about:
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
