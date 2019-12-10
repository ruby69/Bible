package com.appskimo.app.bible;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.appskimo.app.bible.domain.Bible;
import com.appskimo.app.bible.domain.Callback;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.OnFinishLockscreenActivityAll;
import com.appskimo.app.bible.event.OnLockActivity;
import com.appskimo.app.bible.event.OnMainActivity;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.service.MiscService;
import com.appskimo.app.bible.service.PrefsService_;
import com.appskimo.app.bible.support.EventBusObserver;
import com.appskimo.app.bible.ui.view.VerseContentView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;

@EActivity(R.layout.activity_lock)
public class LockActivity extends AppCompatActivity {
    @ViewById(R.id.layer) View layer;
    @ViewById(R.id.verseContentView) VerseContentView verseContentView;
    @ViewById(R.id.bibleSpinner) Spinner bibleSpinner;
    @ViewById(R.id.chapterSpinner) Spinner chapterSpinner;
    @ViewById(R.id.menu) View menu;
    @ViewById(R.id.lockOff) View lockOff;
    @ViewById(R.id.adBanner) AdView adBanner;

    @Pref PrefsService_ prefs;
    @Bean BibleService bibleService;
    @Bean MiscService miscService;
    @ColorRes(R.color.white) int white;

    @SystemService TelephonyManager telephonyManager;

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                finish();
            }
        }
    };

    private FirebaseAnalytics firebaseAnalytics;
    private int spinnerChecker1;
    private int spinnerChecker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 26) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        if (!BuildConfig.DEBUG) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Fabric.with(this, new Crashlytics());
        }
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        verseContentView.setActivity(this);
        verseContentView.setUseAnimate(true);
        verseContentView.setContentsTextColor(white);

        bibleService.loadBibles(new Callback<List<Bible>>() {
            @Override
            public void onSuccess(final List<Bible> bibles) {
                super.onSuccess(bibles);
                bibleService.setSelectedBible(bibles.get(prefs.biblePosition().getOr(0)));
                bibleService.setSelectedChapter(prefs.chapterPosition().getOr(0) + 1);
                makeSpinner(bibles);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                bibleService.findContent(prefs.contentUid().getOr(0), new Callback<Content>() {
                    @Override
                    public void onSuccess(Content content) {
                        super.onSuccess(content);
                        verseContentView.setContent(content, false);
                    }
                });
            }
        });

        EventBus.getDefault().post(new OnLockActivity());
    }

    @AfterInject
    void afterInject() {
        miscService.applyFontScale(this);
    }

    @AfterViews
    void afterViews() {
        menu.setVisibility(View.VISIBLE);
        lockOff.setVisibility(View.VISIBLE);
        miscService.initializeMobileAds();
        miscService.loadBannerAdView(adBanner);
    }

    @UiThread
    void makeSpinner(final List<Bible> bibles) {
        spinnerChecker1 = 0;
        spinnerChecker2 = 0;

        Edition edition = Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
        String [] bibleTitles = new String[bibles.size()];
        for(int i = 0; i<bibles.size(); i++) {
            bibleTitles[i] = bibles.get(i).getTitle(edition);
        }

        ArrayAdapter<CharSequence> bibleAdapter = new ArrayAdapter(LockActivity.this, R.layout.view_spinner1, bibleTitles);
        bibleAdapter.setDropDownViewResource(R.layout.view_spinner_item);

        int biblePosition = prefs.biblePosition().getOr(0);
        bibleSpinner.setAdapter(bibleAdapter);
        bibleSpinner.setSelection(biblePosition);

        Bible bible = bibles.get(biblePosition);
        makeChapterSpinner(bible);
        chapterSpinner.setSelection(prefs.chapterPosition().getOr(0));

        bibleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                Object selectedItem = bibleSpinner.getSelectedItem();
                if (selectedItem != null && "-----".equals(selectedItem.toString())) {
                    ++spinnerChecker1;
                    ++spinnerChecker2;
                    bibleSpinner.setSelection(0);
                } else {
                    if (++spinnerChecker1 > 1) {
                        makeChapterSpinner(bibleService.selectBible(position));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                if (spinnerChecker2 > 2) {
                    miscService.showAdDialog(LockActivity.this, R.string.label_confirm, (dialog, i) -> {});
                }

                if (++spinnerChecker2 > 1) {
                    bibleService.setSelectedChapter(position + 1);
                    prefs.chapterPosition().put(position);

                    prefs.contentUid().put(0);
                    bibleService.findContent(0, new Callback<Content>() {
                        @Override
                        public void onSuccess(Content content) {
                            super.onSuccess(content);
                            verseContentView.setContent(content, false);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    private void makeChapterSpinner(Bible bible) {
        Edition edition = Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
        int chapter = bible.getChapter(edition);
        String [] titles = new String[chapter];
        for(int i = 0; i<chapter; i++) {
            titles[i] = edition.isKo() ? (i+1) + "장" : "Ch-" + (i+1);
        }

        ArrayAdapter<CharSequence> chapterAdapter = new ArrayAdapter(LockActivity.this, R.layout.view_spinner2, titles);
        chapterAdapter.setDropDownViewResource(R.layout.view_spinner_item);
        chapterSpinner.setAdapter(chapterAdapter);
    }

    @Click(R.id.lockOff)
    void onClickLockOff() {
        miscService.showAdDialog(this, R.string.label_lock_off, (dialog, i) -> {
            prefs.lockOffTime().put(new Date().getTime());
            finish();
        });
    }

    @Click(R.id.menu)
    void onClickMenu() {
        LauncherActivity_.intent(this).start();
        finish();
    }

    @Subscribe
    @UiThread
    public void onEvent(OnMainActivity event) {
        finish();
    }

    @Subscribe
    public void onEvent(OnFinishLockscreenActivityAll event) {
        finish();
    }

}
