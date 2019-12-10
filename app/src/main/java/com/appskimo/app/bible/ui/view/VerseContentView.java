package com.appskimo.app.bible.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import androidx.core.app.ShareCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.bible.MainActivity;
import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Callback;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.Like;
import com.appskimo.app.bible.event.SelectEdition;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.service.MiscService;
import com.appskimo.app.bible.service.PrefsService_;
import com.appskimo.app.bible.ui.dialog.EditionDialog;
import com.appskimo.app.bible.ui.dialog.EditionDialog_;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

@EViewGroup(R.layout.view_verse_content)
public class VerseContentView extends RelativeLayout {
    private static final String PACKAGE_KAKAO = "com.kakao.talk";

    @ViewById(R.id.layer) View layer;
    @ViewById(R.id.content1Layer) View content1Layer;
    @ViewById(R.id.content2Layer) View content2Layer;
    @ViewById(R.id.info) TextView infoView;
    @ViewById(R.id.content1) TextView content1View;
    @ViewById(R.id.content2) TextView content2View;
    @ViewById(R.id.kakao) View kakao;
    @ViewById(R.id.share1) View share1;
    @ViewById(R.id.share2) View share2;
    @ViewById(R.id.share2Label) View share2Label;
    @ViewById(R.id.like) LikeButton like;
    @ViewById(R.id.menus) View menus;

    @Bean BibleService bibleService;
    @Bean MiscService miscService;
    @Pref PrefsService_ prefs;

    private Context context;
    private AppCompatActivity activity;
    private Content content;

    private boolean openMenu = false;
    private boolean useAnimate;
    private boolean toRight = true;
    private EditionDialog editionDialog = EditionDialog_.builder().arg("withMode", true).build();

    private OnLikeListener likeListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            EventBus.getDefault().post(new Like(Like.From.VERSE, content.getContentUid(), true));
            bibleService.toggleLikeContent(content, true);
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            EventBus.getDefault().post(new Like(Like.From.VERSE, content.getContentUid(), false));
            bibleService.toggleLikeContent(content, false);
        }
    };

    public VerseContentView(Context context) {
        super(context);
        this.context = context;
    }

    public VerseContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
        if (activity instanceof MainActivity) {
            like.setUnlikeDrawableRes(R.drawable.ic_favorite_border_black_24dp);
            like.setLikeDrawableRes(R.drawable.ic_favorite_black_24dp);
        }
    }

    @AfterInject
    void afterInject() {
        miscService.applyFontScale(getContext());
    }

    @AfterViews
    void afterViews() {
        YoYo.with(Techniques.FadeOut).duration(0).onEnd(endCallback).playOn(menus);
        like.setOnLikeListener(likeListener);
        if (isInstalledKakao()) {
            kakao.setVisibility(View.VISIBLE);
        } else {
            share1.setVisibility(View.VISIBLE);
            share2.setVisibility(View.GONE);
            share2Label.setVisibility(View.GONE);
        }
    }

    public boolean isInstalledKakao() {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_KAKAO, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Edition getEdition() {
        return Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()));
    }

    private Edition getWithEdition() {
        String withName = prefs.withEdition().get();
        return (withName != null && !withName.isEmpty()) ? Edition.valueOf(withName) : null;
    }

    private boolean liked;

    @UiThread
    public void setContent(Content content, boolean liked) {
        this.content = content;
        this.liked = liked;
        prefs.contentUid().put(content.getContentUid());
        Edition edition = getEdition();
        infoView.setText(content.getInfo(edition));
        content1View.setText(content.getContents(edition));
        content1Layer.setScrollY(0);
        like.setLiked(content.isLiked());

        populateWithContent();
    }

    private Callback<Content> manipulateCallback = new Callback<Content>() {
        @Override
        public void onSuccess(Content content) {
            manipulate(content);
        }
    };

    @UiThread
    void manipulate(Content content) {
        if (content != null) {
            setContent(content, liked);
            if (useAnimate) {
                YoYo.with(toRight ? Techniques.FadeInRight : Techniques.FadeInLeft).duration(750).playOn(layer);
            }
        }
    }

    @UiThread
    void populateWithContent() {
        Edition withEdition = getWithEdition();
        if (content != null && withEdition != null) {
            content2Layer.setVisibility(View.VISIBLE);
            content2View.setText(content.getContents(withEdition));
            content2Layer.setScrollY(0);
        } else {
            content2Layer.setVisibility(View.GONE);
        }
    }

    @Click(R.id.prev)
    void onClickPrev() {
        toRight = false;
        if (liked) {
            bibleService.findLikedPrevOrNext(content, false, manipulateCallback);
        } else {
            bibleService.findPrevOrNext(content, false, manipulateCallback);
        }
    }

    @Click(R.id.next)
    void onClickNext() {
        toRight = true;
        if (liked) {
            bibleService.findLikedPrevOrNext(content, true, manipulateCallback);
        } else {
            bibleService.findPrevOrNext(content, true, manipulateCallback);
        }
    }

    @Click(R.id.withBible)
    void onClickWithBible() {
        String withName = prefs.withEdition().get();
        if (withName != null && !withName.isEmpty()) {
            prefs.withEdition().put(null);
            populateWithContent();
        } else {
            editionDialog.show(activity.getSupportFragmentManager(), EditionDialog.TAG);
            if (openMenu) {
                onClickOptionsMenu();
            }
        }
    }

    @Click(R.id.kakao)
    void onClickKakao() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            for (ResolveInfo info : resolveInfos) {
                if (info.activityInfo.packageName.toLowerCase().contains(PACKAGE_KAKAO) || info.activityInfo.name.toLowerCase().contains(PACKAGE_KAKAO)) {
                    intent.putExtra(Intent.EXTRA_TEXT, content.getContentsForShare(getEdition()));
                    intent.setPackage(info.activityInfo.packageName);

                    Intent chooser = Intent.createChooser(intent, getContext().getString(R.string.label_share_verse));
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intent});
                    activity.startActivity(chooser);

                    return;
                }
            }
        }
    }

    @Click(value = {R.id.share1, R.id.share2})
    void onClickShare() {
        ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setText(content.getContentsForShare(getEdition()))
                .setChooserTitle(R.string.label_share_verse)
                .startChooser();
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

    @Subscribe
    public void onEvent(SelectEdition event) {
        if (event.isWithMode()) {
            populateWithContent();
            if (editionDialog != null && editionDialog.isShown()) {
                editionDialog.dismiss();
            }
        }
    }

    public void setWithViewBackgroundResource(int resId) {
        content2Layer.setBackgroundResource(resId);
    }

    public void setUseAnimate(boolean useAnimate) {
        this.useAnimate = useAnimate;
    }

    public void setContentsTextColor(int color) {
        infoView.setTextColor(color);
        content1View.setTextColor(color);
        content2View.setTextColor(color);
    }




//    private ViewTreeObserver.OnScrollChangedListener content1ScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
//        @Override
//        public void onScrollChanged() {
//            content2Layer.setScrollY(content1Layer.getScrollY());
//        }
//    };
//
//    private ViewTreeObserver.OnScrollChangedListener content2ScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
//        @Override
//        public void onScrollChanged() {
//            content1Layer.setScrollY(content2Layer.getScrollY());
//        }
//    };
//
//    private OnTouchListener content1TouchListener = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent event) {
//            int action = event.getAction();
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    content2Layer.getViewTreeObserver().removeOnScrollChangedListener(content2ScrollListener);
//                    break;
//                case MotionEvent.ACTION_UP:
//                    content2Layer.getViewTreeObserver().addOnScrollChangedListener(content2ScrollListener);
//                    break;
//            }
//            return false;
//        }
//    };
//
//    private OnTouchListener content2TouchListener = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent event) {
//            int action = event.getAction();
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    content1Layer.getViewTreeObserver().removeOnScrollChangedListener(content1ScrollListener);
//                    break;
//                case MotionEvent.ACTION_UP:
//                    content1Layer.getViewTreeObserver().addOnScrollChangedListener(content1ScrollListener);
//                    break;
//            }
//            return false;
//        }
//    };


//        content1Layer.getViewTreeObserver().addOnScrollChangedListener(content1ScrollListener);
//        content2Layer.getViewTreeObserver().addOnScrollChangedListener(content2ScrollListener);
//        content1Layer.setOnTouchListener(content1TouchListener);
//        content2Layer.setOnTouchListener(content2TouchListener);
}