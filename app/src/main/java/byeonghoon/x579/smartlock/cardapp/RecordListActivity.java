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
import com.kt.gigaiot_sdk.data.Log;
import com.kt.gigaiot_sdk.data.TagStrmApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import byeonghoon.x579.smartlock.cardapp.private_setting.PrivateSettings;

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
        String stringifiedApdu = "";
        Card target = null;
        List<Card> card_list = Card.getCardList();
        for(Card c : card_list) {
            if(card_id == c.getCardId()) {
                target = c;
                stringifiedApdu = CardService.ByteArrayToHexString(c.getApdu());
                break;
            }
        }

        //Crawl from IoTMakers server
        GiGaIotOAuthResponse iotAuthResponse = new GigaIotOAuth(PrivateSettings.iotAuth_clientID, PrivateSettings.iotAuth_clientSecret).loginWithPassword(PrivateSettings.loginID, PrivateSettings.loginPassword);
        TagStrmApi api = new TagStrmApi(iotAuthResponse.getAccessToken());
        TagStrmApiResponse apiResponse = api.getTagStrmLog(PrivateSettings.iotAuth_clientID, PrivateSettings.iotDevice_deviceID, "" + System.currentTimeMillis(), "");
        ArrayList<Log> logs = apiResponse.getLogs();

        //narrow down logs
        for(Log l : logs) {
            Map<String, Object> attr = l.getAttributes();
            if(attr.get("CardKey").equals(stringifiedApdu)) {
                ListItem item = new ListItem();
                item.title = target.getTitle();
                item.latitude =  Double.parseDouble(attr.get("latitude").toString());
                item.longitude = Double.parseDouble(attr.get("longitude").toString());
                item.timestamp = l.getOccDt();
                item.desc = processTypeMsg((Double) attr.get("inputType"));
                item.isTemporary = (Double) attr.get("inputType") == 7;
            }
        }
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
            TextView desc = (TextView) view.findViewById(R.id.record_list_item_desc);

            if(item.isTemporary) {
                title = "[Temporary] " + item.title;
                title_view.setTextColor(view.getResources().getColor(R.color.colorAccent));
            }
            title_view.setText(title);
            //SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm");
            //time_view.setText("Time: " + format.format(new Date(item.timestamp)));
            time_view.setText("Time: " + item.timestamp);
            desc.setText("Description: " + item.desc);

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

    private String processTypeMsg(double d) {
        String str = "Unknown message :(";
        if (d == 0) {
            str = "Registered";
        } else if(d == 1) {
            str = "Normal open";
        } else if (d == 5) {
            str = "allow temporary permission";
        } else if (d == 6) {
            str = "disallow temporary permission";
        } else if (d == 7) {
            str = "Using temporary permission";
        }
        return str;
    }

    static class ListItem {
        String title;
        String timestamp;
        String desc;
        double latitude;
        double longitude;
        boolean isTemporary = false;
    }
}
