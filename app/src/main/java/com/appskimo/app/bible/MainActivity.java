package com.appskimo.app.bible;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.appskimo.app.bible.domain.Bible;
import com.appskimo.app.bible.domain.Callback;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.ChangeFontScale;
import com.appskimo.app.bible.event.OnLockActivity;
import com.appskimo.app.bible.event.OnMainActivity;
import com.appskimo.app.bible.event.SelectChapter;
import com.appskimo.app.bible.event.SelectContent;
import com.appskimo.app.bible.event.SelectEdition;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.service.MiscService;
import com.appskimo.app.bible.service.PrefsService_;
import com.appskimo.app.bible.support.EventBusObserver;
import com.appskimo.app.bible.support.ScreenOffService_;
import com.appskimo.app.bible.ui.frags.LikedFragment_;
import com.appskimo.app.bible.ui.frags.ListFragment_;
import com.appskimo.app.bible.ui.frags.SettingsFragment_;
import com.appskimo.app.bible.ui.view.FontScaleView_;
import com.appskimo.app.bible.ui.view.VerseContentView;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int POSITION_LIST = 0;
    private static final int POSITION_FAVOR = 1;
    private static final int POSITION_SETTINGS = 2;

    @ViewById(R.id.appBarLayout) AppBarLayout appBarLayout;
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.drawerLayout) DrawerLayout drawerLayout;
    @ViewById(R.id.navigationView) NavigationView navigationView;
    @ViewById(R.id.verseContentView) VerseContentView verseContentView;
    @ViewById(R.id.mainViewPager) ViewPager mainViewPager;

    @Pref PrefsService_ prefs;
    @Bean BibleService bibleService;
    @Bean MiscService miscService;

    private PagerAdapter pagerAdapter;
    private BottomSheetBehavior bottomSheetBehavior;

    private Spinner bibleSpinner;
    private Spinner chapterSpinner;
    private int spinnerChecker1;
    private int spinnerChecker2;
    private boolean liked;

    private AlertDialog fontScaleDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this);
            Fabric.with(this, new Crashlytics());
        }
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        if (prefs.useLockScreen().getOr(false)) {
            ScreenOffService_.start(this);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        prefs.lockOffTime().put(0L);
        EventBus.getDefault().post(new OnMainActivity());
    }

    @AfterInject
    void afterInject() {
        miscService.applyFontScale(this);
    }

    @AfterViews
    void afterViews() {
        initNavigationDrawer();
        mainViewPager.setAdapter(pagerAdapter);
        initBottomSheet();
        miscService.initializeMobileAds();
        fontScaleDialog = new AlertDialog.Builder(this).setTitle(R.string.label_font_scale).setView(FontScaleView_.build(this)).create();
        if(prefs.launchedCount().get() == 1) {
            fontScaleDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        bibleSpinner = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.bibleSpinner));
        chapterSpinner = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.chapterSpinner));

        initAppbarSpinner();
        return super.onCreateOptionsMenu(menu);
    }

    private void initAppbarSpinner() {
        bibleService.loadBibles(new Callback<List<Bible>>() {
            @Override
            public void onSuccess(final List<Bible> bibles) {
                super.onSuccess(bibles);
                bibleService.setSelectedBible(bibles.get(prefs.biblePosition().getOr(0)));
                bibleService.setSelectedChapter(prefs.chapterPosition().getOr(0) + 1);

                makeAppbarSpinner(bibles);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                EventBus.getDefault().post(new SelectChapter());
            }
        });
    }

    @UiThread
    void makeAppbarSpinner(final List<Bible> bibles) {
        spinnerChecker1 = 0;
        spinnerChecker2 = 0;

        Edition edition = Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
        String [] bibleTitles = new String[bibles.size()];
        for(int i = 0; i<bibles.size(); i++) {
            bibleTitles[i] = bibles.get(i).getTitle(edition);
        }

        ArrayAdapter<CharSequence> bibleAdapter = new ArrayAdapter(MainActivity.this, R.layout.view_spinner1, bibleTitles);
        bibleAdapter.setDropDownViewResource(R.layout.view_spinner_item);

        int biblePosition = prefs.biblePosition().getOr(0);
        bibleSpinner.setAdapter(bibleAdapter);
        bibleSpinner.setSelection(biblePosition);

        Bible bible = bibles.get(biblePosition);
        makeAppbarChapterSpinner(bible);
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
                        makeAppbarChapterSpinner(bibleService.selectBible(position));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                if (spinnerChecker2 > 3) {
                    miscService.showAdDialog(MainActivity.this, R.string.label_confirm, (dialog, i) -> {});
                }

                if (++spinnerChecker2 > 1) {
                    bibleService.setSelectedChapter(position + 1);
                    prefs.chapterPosition().put(position);
                    EventBus.getDefault().post(new SelectChapter());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    private void makeAppbarChapterSpinner(Bible bible) {
        Edition edition = Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
        int chapter = bible.getChapter(edition);
        String [] titles = new String[chapter];
        for(int i = 0; i<chapter; i++) {
            titles[i] = edition.isKo() ? (i+1) + "장" : "Ch-" + (i+1);
        }

        ArrayAdapter<CharSequence> chapterAdapter = new ArrayAdapter(this, R.layout.view_spinner2, titles);
        chapterAdapter.setDropDownViewResource(R.layout.view_spinner_item);
        chapterSpinner.setAdapter(chapterAdapter);
    }

    private void initNavigationDrawer() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        initDrawerEditionView();
    }

    private void initDrawerEditionView() {
        View headerLayout = navigationView.getHeaderView(0);
        TextView edition = (TextView) headerLayout.findViewById(R.id.edition);
        edition.setText(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name())).getTitle());
    }

    private void initBottomSheet() {
        verseContentView.setActivity(this);
        verseContentView.setWithViewBackgroundResource(R.color.yellow_trans20);
        bottomSheetBehavior = BottomSheetBehavior.from(verseContentView);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void setVisibleWritingPad(boolean visible) {
        appBarLayout.setExpanded(!visible);
        bottomSheetBehavior.setState(visible ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuBible) {
            setVisibleWritingPad(false);
            mainViewPager.setCurrentItem(POSITION_LIST, Boolean.FALSE);
            drawerLayout.closeDrawer(GravityCompat.START);
            liked = false;

            bibleSpinner.setVisibility(View.VISIBLE);
            chapterSpinner.setVisibility(View.VISIBLE);

        } else if (id == R.id.menuLiked) {
            setVisibleWritingPad(false);
            mainViewPager.setCurrentItem(POSITION_FAVOR, Boolean.FALSE);
            drawerLayout.closeDrawer(GravityCompat.START);
            liked = true;

            bibleSpinner.setVisibility(View.GONE);
            chapterSpinner.setVisibility(View.GONE);

        } else if (id == R.id.menuSettings) {
            setVisibleWritingPad(false);
            mainViewPager.setCurrentItem(POSITION_SETTINGS, Boolean.FALSE);
            drawerLayout.closeDrawer(GravityCompat.START);

            bibleSpinner.setVisibility(View.GONE);
            chapterSpinner.setVisibility(View.GONE);

        } else if (id == R.id.menuLockscreen) {
            boolean useLockscreen = prefs.useLockScreen().get();
            if (useLockscreen) {
                miscService.showAdDialog(this, R.string.label_onlock_start, (dialog, i) -> OnActivity_.intent(this).start());
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.message_confirm_lockscreen)
                        .setPositiveButton(R.string.label_confirm, (dialogInterface, i) -> {
                            prefs.useLockScreen().put(true);
                            ScreenOffService_.start(MainActivity.this);
                            OnActivity_.intent(MainActivity.this).start();
                        }).create();

                if (!this.isFinishing()) {
                    alertDialog.show();
                }
            }
        }

        return true;
    }

    @Subscribe
    public void onEvent(SelectEdition event) {
        if (!event.isWithMode()) {
            initDrawerEditionView();
            initAppbarSpinner();
            Toast.makeText(getApplicationContext(), getString(R.string.label_edition_change) + Edition.valueOf(prefs.edition().getOr(Edition.KRV.name())).getTitle(), Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEvent(SelectContent event) {
        setVisibleWritingPad(true);
        verseContentView.setContent(event.getContent(), liked);
    }

    @Subscribe
    public void onEvent(ChangeFontScale event) {
        if (fontScaleDialog != null && fontScaleDialog.isShowing()) {
            fontScaleDialog.dismiss();
        }
        LauncherActivity_.intent(this).start();
        finish();
    }

    @Subscribe
    @UiThread
    public void onEvent(OnLockActivity event) {
        finish();
    }

    private void linkPlayStore(String packageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> items = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);

            items.add(ListFragment_.builder().build());
            items.add(LikedFragment_.builder().build());
            items.add(SettingsFragment_.builder().build());
        }

        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
            setVisibleWritingPad(false);
            return;
        }

        miscService.showAdDialog(this, R.string.label_finish, (dialog, i) -> finish());
    }
}
