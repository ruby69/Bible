package com.appskimo.app.bible.ui.frags;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.ShareCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Callback;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.domain.More;
import com.appskimo.app.bible.event.Like;
import com.appskimo.app.bible.event.MoreItems;
import com.appskimo.app.bible.event.OnCheck;
import com.appskimo.app.bible.event.SelectChapter;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.service.PrefsService_;
import com.appskimo.app.bible.support.EventBusObserver;
import com.appskimo.app.bible.ui.adapter.ContentRecyclerViewAdapter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;
import java.util.TreeMap;

@EFragment(R.layout.fragment_list)
public class ListFragment extends Fragment {
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    @ViewById(R.id.menus) View menus;

    @Bean ContentRecyclerViewAdapter recyclerViewAdapter;
    @Bean BibleService bibleService;
    @Pref PrefsService_ prefs;

    @SystemService ClipboardManager clipboardManager;

    private boolean openMenu = false;
    private More currentMore;
    private Map<Integer, Content> checkedMap = new TreeMap<>();

    private final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (refreshLayout != null) {
                refreshLayout.setRefreshing(true);
            }

            if (bibleService != null) {
                bibleService.retrieve(new More(), new Callback<More>() {
                    @Override
                    public void onSuccess(More more) {
                        currentMore = more;
                        reset(more);
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
    }

    @AfterViews
    void afterViews() {
        YoYo.with(Techniques.FadeOut).duration(0).onEnd(endCallback).playOn(menus);
        recyclerView.setAdapter(recyclerViewAdapter);
        refreshLayout.setOnRefreshListener(refreshListener);
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    @UiThread
    void reset(More more) {
        checkedMap.clear();

        currentMore = more;
        recyclerViewAdapter.reset(more.getContent());

        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }

        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Click(R.id.upward)
    void onClickUpward() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Click(R.id.cancelChecked)
    void onClickCancelChecked() {
        for(Content content : checkedMap.values()) {
            content.setChecked(false);
        }
        checkedMap.clear();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Click(R.id.copyChecked)
    void onClickCopyChecked() {
        if (!checkedMap.isEmpty()) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.label_verse_checked), getCheckedContent()));
            Toast.makeText(getActivity(), R.string.message_copy_checked, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.message_nochecked, Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.shareChecked)
    void onClickShareChecked() {
        if (!checkedMap.isEmpty()) {
            ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(getCheckedContent())
                    .setChooserTitle(R.string.label_share_verse)
                    .startChooser();

        } else {
            Toast.makeText(getActivity(), R.string.message_nochecked, Toast.LENGTH_SHORT).show();
        }
    }

    private String getCheckedContent() {
        Edition edition = getEdition();
        StringBuilder sb = new StringBuilder();
        for(Content content : checkedMap.values()) {
            sb.append(content.getContents(edition)).append(" - ").append(content.getInfo(edition)).append("\n\n");
        }
        return sb.toString();
    }

    private Edition getEdition() {
        return Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
    }

    private YoYo.AnimatorCallback endCallback = animator -> {
        if (menus != null) {
            menus.setVisibility(View.GONE);
        }
    };
    private YoYo.AnimatorCallback startCallback = animator -> {
        if (menus != null) {
            menus.setVisibility(View.VISIBLE);
        }
    };

    @Click(R.id.optionsMenu)
    void onClickOptionsMenu() {
        if (openMenu) {
            YoYo.with(Techniques.FadeOutDown).duration(100).onEnd(endCallback).playOn(menus);
            openMenu = false;
        } else {
            YoYo.with(Techniques.FadeInUp).interpolate(new OvershootInterpolator()).duration(300).onStart(startCallback).playOn(menus);
            openMenu = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Subscribe
    public void onEvent(SelectChapter event) {
        if (bibleService != null) {
            bibleService.retrieve(new More(), new Callback<More>() {
                @Override
                public void onSuccess(More more) {
                    reset(more);
                }
            });
        }
    }


    @Subscribe
    @UiThread
    public void onEvent(MoreItems event) {
        if (bibleService != null && currentMore.isHasMore()) {
            bibleService.retrieve(currentMore, new Callback<More>() {
                @Override
                public void onSuccess(More more) {
                    currentMore = more;
                    recyclerViewAdapter.add(more.getContent());
                }
            });
        }
    }

    @Subscribe
    @UiThread
    public void onEvent(Like event) {
        if (!event.from().isList()) {
            recyclerViewAdapter.updateLike(event.getContentUid(), event.isLiked());
        }
    }

    @Subscribe
    @UiThread
    public void onEvent(OnCheck event) {
        if (event.from().isList()) {
            Content content = event.getContent();
            if (content.isChecked()) {
                if (!openMenu) {
                    onClickOptionsMenu();
                }
                checkedMap.put(content.getContentUid(), content);
            } else {
                checkedMap.remove(content.getContentUid());
            }
        }
    }

}
