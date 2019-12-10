package com.appskimo.app.bible.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.event.MoreFavor;
import com.appskimo.app.bible.ui.view.LikedItemView;
import com.appskimo.app.bible.ui.view.LikedItemView_;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

@EBean
public class LikedRecyclerViewAdapter extends CommonRecyclerViewAdapter<Content, LikedRecyclerViewAdapter.ViewHolder> {
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(position == this.getItemCount() - 15) {
            EventBus.getDefault().post(new MoreFavor());
        }
        viewHolder.likedItemView.setRecord(position, items.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LikedItemView_.build(viewGroup.getContext()));
    }

    public void remove(int contentUid) {
        int position = findPosition(contentUid);
        if (position > -1) {
            notifyItemRemoved(position);
            items.remove(position);
        }
    }

    private int findPosition(int contentUid) {
        for (int i = 0; i < items.size(); i++) {
            if (contentUid == items.get(i).getContentUid().intValue()) {
                return i;
            }
        }
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LikedItemView likedItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            likedItemView = (LikedItemView) itemView;
        }
    }
}