package com.appskimo.app.bible.ui.view;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.SelectEdition;
import com.appskimo.app.bible.service.PrefsService_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_edition_item)
public class EditionItemView extends RelativeLayout {
    @ViewById(R.id.title) TextView title;
    @Pref PrefsService_ prefs;

    private Edition edition;
    private boolean withMode;

    public EditionItemView(Context context) {
        super(context);
    }

    public void setEdition(Edition edition, boolean withMode) {
        this.edition = edition;
        this.withMode = withMode;
        title.setText(edition.getTitle());
    }

    @Click(R.id.titleLayer)
    void onClick() {
        if (withMode) {
            prefs.withEdition().put(edition.name());
        } else {
            prefs.edition().put(edition.name());
        }
        EventBus.getDefault().post(new SelectEdition(withMode));
    }
}