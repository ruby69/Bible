package com.appskimo.app.bible.ui.dialog;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.ui.adapter.CommonRecyclerViewAdapter;
import com.appskimo.app.bible.ui.view.EditionItemView;
import com.appskimo.app.bible.ui.view.EditionItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.Serializable;
import java.util.Arrays;

import lombok.Setter;

@EFragment(R.layout.dialog_simple_selector)
public class EditionDialog extends CommonDialog {
    public static final String TAG = "EditionDialog";

    @ViewById(R.id.recyclerView) protected RecyclerView recyclerView;
    @Bean protected BibleService bibleService;
    private EditionRecyclerViewAdapter recyclerViewAdapter;

    @AfterInject
    protected void afterInject() {
        recyclerViewAdapter = new EditionRecyclerViewAdapter();
    }

    @AfterViews
    protected void afterViews() {
        Serializable arg = getArguments().getSerializable("withMode");
        if (arg != null) {
            recyclerViewAdapter.setWithMode((boolean) arg);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.reset(Arrays.asList(Edition.values()));
    }

    private static class EditionRecyclerViewAdapter extends CommonRecyclerViewAdapter<Edition, EditionRecyclerViewAdapter.ViewHolder> {
        @Setter private boolean withMode;

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.editionItemView.setEdition(items.get(position), withMode);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ViewHolder(EditionItemView_.build(viewGroup.getContext()));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            EditionItemView editionItemView;
            public ViewHolder(View itemView) {
                super(itemView);
                editionItemView = (EditionItemView) itemView;
            }
        }
    }

}