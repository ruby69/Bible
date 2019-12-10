package com.appskimo.app.bible.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.event.MoreItems;
import com.appskimo.app.bible.ui.view.ContentItemView;
import com.appskimo.app.bible.ui.view.ContentItemView_;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

@EBean
public class ContentRecyclerViewAdapter extends CommonRecyclerViewAdapter<Content, ContentRecyclerViewAdapter.ViewHolder> {
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(position == this.getItemCount() - 15) {
            EventBus.getDefault().post(new MoreItems());
        }
        viewHolder.contentItemView.setRecord(position, items.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(ContentItemView_.build(viewGroup.getContext()));
    }

    public void updateLike(int contentUid, boolean liked) {
        Content content = findByContentUid(contentUid);
        if (content != null) {
            content.setLiked(liked);
            notifyDataSetChanged();
        }
    }

    private Content findByContentUid(int contentUid) {
        for (Content item : items) {
            if (contentUid == item.getContentUid().intValue()) {
                return item;
            }
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ContentItemView contentItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentItemView = (ContentItemView) itemView;
        }
    }
}