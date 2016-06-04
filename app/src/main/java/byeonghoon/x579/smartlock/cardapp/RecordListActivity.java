package byeonghoon.x579.smartlock.cardapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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

        mRecyclerAdapter = new ListAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    static class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<ListItem> item_lists = new ArrayList<>();

        static class ViewHolderImpl extends RecyclerView.ViewHolder {
            public View myView;
            public ViewHolderImpl(View itemview) {
                super(itemview);
                myView = itemview;
            }
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
            View view = LayoutInflater.from(group.getContext()).inflate(R.layout.record_list_item, group, false);
            return new ViewHolderImpl(view);
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ListItem item = item_lists.get(position);
            View view = holder.itemView;
        }

        @Override public int getItemCount() { return item_lists.size(); }
    }

    static class ListItem {
        String title;
        Long timestamp;
        String desc;
        double latitude;
        double longnitude;
    }
}
