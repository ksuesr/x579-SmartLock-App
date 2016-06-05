package byeonghoon.x579.smartlock.cardapp;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Byeonghoon on 31-May-16.
 */
public class LockViewAdapter extends RecyclerView.Adapter<LockViewAdapter.ViewHolderImpl> {

    public static class ViewHolderImpl extends RecyclerView.ViewHolder {

        public View myView;
        public ViewHolderImpl(View itemview) {
            super(itemview);
            myView = itemview;
        }
    }

    @Override
    public LockViewAdapter.ViewHolderImpl onCreateViewHolder(ViewGroup group, int viewType) {
        View v = LayoutInflater.from(group.getContext()).inflate(R.layout.lock_list_item, group, false);
        return new ViewHolderImpl(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderImpl holder, int position) {
        Card target = null;
        List<Card> list = Card.getCardList();
        int temp = position;
        for(Card c : list) {
            if(temp == 0) {
                target = c;
                break;
            }
            temp--;
        }
        View v = holder.myView;

        //modify view via v.findViewById + something :)
        TextView text = (TextView) v.findViewById(R.id.item_title);
        text.setText(target.getTitle());
        TextView textbutton_log = (TextView) v.findViewById(R.id.textbutton_lock_log);
        TextView textbutton_permission = (TextView) v.findViewById(R.id.textbutton_lock_send_perm);
        final int id = target.getCardId();


        textbutton_log.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent sendIntent = new Intent(v.getContext(), RecordListActivity.class);
                sendIntent.putExtra("Card_ID", id);
                v.getContext().startActivity(sendIntent);
            }
        });
        textbutton_permission.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //send permission request to server.
                //also, generate 14-digit random code
                Intent sendIntent = new Intent(v.getContext(), SendPermissionActivity.class);
                sendIntent.putExtra("Card_ID", id);
                v.getContext().startActivity(sendIntent);
            }
        });
    }

    @Override public int getItemCount() { return Card.listCount(); }
}
