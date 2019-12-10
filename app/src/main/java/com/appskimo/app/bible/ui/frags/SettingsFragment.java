package com.appskimo.app.bible.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.ChangeFontScale;
import com.appskimo.app.bible.event.SelectEdition;
import com.appskimo.app.bible.service.PrefsService_;
import com.appskimo.app.bible.support.EventBusObserver;
import com.appskimo.app.bible.support.ScreenOffService_;
import com.appskimo.app.bible.ui.dialog.EditionDialog;
import com.appskimo.app.bible.ui.dialog.EditionDialog_;
import com.appskimo.app.bible.ui.view.FontScaleView_;

import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.Subscribe;

@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment {
    @ViewById(R.id.lockscreenUse) Switch lockscreenUse;
    @ViewById(R.id.selectEdition) Button selectEdition;
    @ViewById(R.id.selectFont) Button selectFont;

    @Pref PrefsService_ prefs;

    private EditionDialog editionDialog = EditionDialog_.builder().build();
    private AlertDialog fontScaleDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontScaleDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.label_font_scale).setView(FontScaleView_.build(getActivity())).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        lockscreenUse.setChecked(prefs.useLockScreen().get());
        selectEdition.setText(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name())).getTitle());
        float scale = prefs.fontScale().getOr(1.15F);
        if (scale == 0.70F) {
            selectFont.setText(R.string.label_font_tiny1);
        } else if (scale == 0.85F) {
            selectFont.setText(R.string.label_font_small1);
        } else if (scale == 1.00F) {
            selectFont.setText(R.string.label_font_normal1);
        } else {
            selectFont.setText(R.string.label_font_large1);
        }
    }

    @CheckedChange(R.id.lockscreenUse)
    void onCheckedSwitch(CompoundButton button, boolean isChecked) {
        if (prefs.useLockScreen().get() != isChecked) {
            prefs.useLockScreen().put(isChecked);
            if (isChecked) {
                ScreenOffService_.start(getActivity());
            } else {
                ScreenOffService_.intent(getActivity()).stop();
            }
        }
    }

    @Click(R.id.selectEdition)
    void onClickSelectEdition() {
        editionDialog.show(getChildFragmentManager(), EditionDialog.TAG);
    }

    @Subscribe
    public void onEvent(SelectEdition event) {
        selectEdition.setText(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name())).getTitle());
        editionDialog.dismiss();
    }

    @Click(R.id.selectFont)
    void onClickSelectFont() {
        fontScaleDialog.show();
    }

    @Subscribe
    public void onEvent(ChangeFontScale event) {
        if (fontScaleDialog != null && fontScaleDialog.isShowing()) {
            fontScaleDialog.dismiss();
        }
    }
}
