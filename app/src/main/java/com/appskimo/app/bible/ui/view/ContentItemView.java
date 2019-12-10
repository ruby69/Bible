package com.appskimo.app.bible.ui.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.domain.Edition;
import com.appskimo.app.bible.event.Like;
import com.appskimo.app.bible.event.OnCheck;
import com.appskimo.app.bible.event.SelectContent;
import com.appskimo.app.bible.service.BibleService;
import com.appskimo.app.bible.service.PrefsService_;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_content_item)
public class ContentItemView extends RelativeLayout {
    @ViewById(R.id.verse) TextView verseView;
    @ViewById(R.id.content) TextView contentView;
    @ViewById(R.id.like) LikeButton like;
    @ViewById(R.id.check) LikeButton check;

    @ColorRes(R.color.white) int white;
    @ColorRes(R.color.pink_trans50) int highlight;
    @Pref PrefsService_ prefs;
    @Bean BibleService bibleService;

    private BackgroundColorSpan backgroundColorSpan;
    private ForegroundColorSpan foregroundColorSpan;
    private Content content;

    private OnLikeListener likeListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            EventBus.getDefault().post(new Like(Like.From.LIST, content.getContentUid(), true));
            bibleService.toggleLikeContent(content, true);
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            EventBus.getDefault().post(new Like(Like.From.LIST, content.getContentUid(), false));
            bibleService.toggleLikeContent(content, false);
        }
    };

    private OnLikeListener checkListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            content.setChecked(true);
            Spannable spannable = new SpannableStringBuilder(content.getContents(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()))));
            spannable.setSpan(backgroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(foregroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentView.setText(spannable);
            EventBus.getDefault().post(new OnCheck(content, OnCheck.From.LIST));
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            content.setChecked(false);
            contentView.setText(content.getContents(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()))));
            EventBus.getDefault().post(new OnCheck(content, OnCheck.From.LIST));
        }
    };

    public ContentItemView(Context context) {
        super(context);
    }

    public ContentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    void afterViews() {
        like.setOnLikeListener(likeListener);
        check.setOnLikeListener(checkListener);

        backgroundColorSpan = new BackgroundColorSpan(highlight);
        foregroundColorSpan = new ForegroundColorSpan(white);
    }

    public void setRecord(int position, Content content) {
        this.content = content;

        verseView.setText(String.format("%s ", content.getVerse()));
        like.setLiked(content.isLiked());
        check.setLiked(content.isChecked());

        if (content.isChecked()) {
            Spannable spannable = new SpannableStringBuilder(content.getContents(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()))));
            spannable.setSpan(backgroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(foregroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentView.setText(spannable);
        }else {
            contentView.setText(content.getContents(Edition.valueOf(prefs.edition().getOr(Edition.KRV.name()))));
        }
    }

    @Click(R.id.layer)
    void onClickLayer() {
        EventBus.getDefault().post(new SelectContent(content));
    }

}