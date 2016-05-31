package byeonghoon.x579.smartlock.cardapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;

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
        Iterator<Card> iterator = Card.getCardList().iterator();
        while(iterator.hasNext()) {
            target = iterator.next();
            if(target.getCardId() == position) {
                break;
            }
        }
        View v = holder.myView;

        //modify view via v.findViewById + something :)
    }

    @Override public int getItemCount() { return Card.listCount(); }
}
