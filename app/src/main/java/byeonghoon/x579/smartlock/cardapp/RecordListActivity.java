package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kt.gigaiot_sdk.GigaIotOAuth;
import com.kt.gigaiot_sdk.TagStrmApi;
import com.kt.gigaiot_sdk.data.GiGaIotOAuthResponse;
import com.kt.gigaiot_sdk.data.TagStrmApiResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayout;

    private int card_id;

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

        card_id = getIntent().getIntExtra("Card_ID", 0);

        //Crawl from IoTMakers server
        GiGaIotOAuthResponse iotAuthResponse = new GigaIotOAuth("clientid", "clientsecret").login();
        TagStrmApi api = new TagStrmApi(iotAuthResponse.getAccessToken());
        TagStrmApiResponse apiResponse = api.getTagStrmLog("svcTgtSeq", "spotDevSeq");
        

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
            final ListItem item = item_lists.get(position);
            View view = holder.itemView;
            String title = item.title;

            TextView title_view = (TextView) view.findViewById(R.id.record_list_title);
            TextView time_view = (TextView) view.findViewById(R.id.record_list_item_timestamp);
            TextView location_button = (TextView) view.findViewById(R.id.record_list_item_location);

            if(item.desc.equalsIgnoreCase("temporary")) {
                title = "[Temporary] " + item.title;
                title_view.setTextColor(view.getResources().getColor(R.color.colorAccent));
            }
            title_view.setText(title);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm");
            time_view.setText("Time: " + format.format(new Date(item.timestamp)));

            location_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://maps.google.com/maps?q=loc:" + item.latitude + "," + item.longitude + " (" + item.title + ")";
                    Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    v.getContext().startActivity(mapsIntent);
                }
            });
        }

        @Override public int getItemCount() { return item_lists.size(); }
    }

    static class ListItem {
        String title;
        Long timestamp;
        String desc;
        double latitude;
        double longitude;
    }
}
