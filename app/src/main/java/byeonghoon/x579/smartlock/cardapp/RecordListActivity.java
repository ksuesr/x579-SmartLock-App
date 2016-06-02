package byeonghoon.x579.smartlock.cardapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class RecordListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.record_list_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerLayout);

       // mRecyclerAdapter = new LockViewAdapter();
        //mRecyclerView.setAdapter(mRecyclerAdapter);
    }
}
