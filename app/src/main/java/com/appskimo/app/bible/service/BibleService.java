package com.appskimo.app.bible.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.appskimo.app.bible.BuildConfig;
import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Bible;
import com.appskimo.app.bible.domain.Callback;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.domain.More;
import com.appskimo.app.bible.support.SQLiteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@EBean(scope = EBean.Scope.Singleton)
public class BibleService {
    @RootContext protected Context context;

    @OrmLiteDao(helper = SQLiteOpenHelper.class) protected SQLiteOpenHelper.BibleDao bibleDao;
    @OrmLiteDao(helper = SQLiteOpenHelper.class) protected SQLiteOpenHelper.ContentDao contentDao;

    @Pref protected PrefsService_ prefs;

    @Setter @Getter private Bible selectedBible;
    @Setter private int selectedChapter;

    @AfterInject
    protected void afterInject() {
    }

// ------------------------------------------------------------------------------------------------------------------------------------------

    @Background
    public void loadBibles(Callback<List<Bible>> callback) {
        callback.before();
        try {
            callback.onSuccess(getBibles());
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }

    public Bible selectBible(int position) {
        selectedBible = getBibles().get(position);
        prefs.biblePosition().put(position);
        prefs.bibleUid().put(selectedBible.getBibleUid());
        return selectedBible;
    }

    private List<Bible> getBibles() {
        return bibleDao.queryForAll();
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    @Background
    public void findContent(int contentUid, Callback<Content> callback) {
        callback.before();
        try {
            if (contentUid > 0) {
                callback.onSuccess(contentDao.queryForId(contentUid));
            } else {
                callback.onSuccess(getContentForFirst(true));
            }
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }

    @Background
    public void findPrevOrNext(Content current, boolean ascending, Callback<Content> callback) {
        callback.before();

        try {
            Content content = findPrevOrNext(current.getContentUid(), ascending);
            if (content == null) {
                content = getContentForFirst(ascending);
            }
            callback.onSuccess(content);
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }

    private Content getContentForFirst(boolean ascending) {
        try {
            QueryBuilder<Content, Integer> qb = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, ascending);
            Where<Content, Integer> where = qb.where();
            Where<Content, Integer> left = where.eq(Bible.FIELD_bibleUid, selectedBible.getBibleUid());
            Where<Content, Integer> right = where.eq(Content.FIELD_chapter, selectedChapter);
            where.and(left, right);
            return qb.queryForFirst();
        } catch (SQLException e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            return null;
        }
    }

    private Content findPrevOrNext(int contentUid, boolean ascending) {
        try {
            QueryBuilder<Content, Integer> qb = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, ascending);
            Where<Content, Integer> where = qb.where();
            Where<Content, Integer> left = where.eq(Bible.FIELD_bibleUid, selectedBible.getBibleUid());
            Where<Content, Integer> right = where.eq(Content.FIELD_chapter, selectedChapter);
            Where<Content, Integer> other = ascending ? where.gt(Content.FIELD_contentUid, contentUid) : where.lt(Content.FIELD_contentUid, contentUid);
            where.and(left, right, other);
            return qb.queryForFirst();
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            return null;
        }
    }

    @Background
    public void toggleLikeContent(Content content, boolean liked) {
        try {
            content.setLiked(liked);
            contentDao.update(content);
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
    }

    @Background
    public void findLikedPrevOrNext(Content current, boolean ascending, Callback<Content> callback) {
        callback.before();

        try {
            Content content = findLikedPrevOrNext(current.getContentUid(), ascending);
            if (content == null) {
                content = getLikedContentForFirst(ascending);
            }
            callback.onSuccess(content);
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }

    private Content getLikedContentForFirst(boolean ascending) {
        try {
            QueryBuilder<Content, Integer> qb = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, ascending);
            qb.where().eq(Content.FIELD_liked, true);
            return qb.queryForFirst();
        } catch (SQLException e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            return null;
        }
    }

    private Content findLikedPrevOrNext(int contentUid, boolean ascending) {
        try {
            QueryBuilder<Content, Integer> qb = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, ascending);
            Where<Content, Integer> where = qb.where();
            Where<Content, Integer> left = where.eq(Content.FIELD_liked, true);
            Where<Content, Integer> right = ascending ? where.gt(Content.FIELD_contentUid, contentUid) : where.lt(Content.FIELD_contentUid, contentUid);
            where.and(left, right);
            return qb.queryForFirst();
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    @Background
    public void retrieve(More more, Callback<More> callback) {
        callback.before();
        try {
            if (selectedBible != null && selectedChapter > 0) {
                QueryBuilder<Content, Integer> qb1 = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, true).limit(more.getScale());
                Where<Content, Integer> where = qb1.where();
                Where<Content, Integer> left = where.eq(Bible.FIELD_bibleUid, selectedBible.getBibleUid());
                Where<Content, Integer> right = where.eq(Content.FIELD_chapter, selectedChapter);

                if (more.getLastId() != null) {
                    Where<Content, Integer> others = where.gt(Content.FIELD_contentUid, more.getLastId());
                    where.and(left, right, others);
                } else {
                    where.and(left, right);
                }

                List<Content> list = qb1.query();
                more.setContent(list);

                if (list != null && !list.isEmpty()) {
                    QueryBuilder<Content, Integer> qb3 = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, false);
                    Content last = qb3.queryForFirst();
                    Content lastContent = list.get(list.size() - 1);
                    more.setHasMore(last.getContentUid().intValue() > lastContent.getContentUid().intValue());
                } else {
                    more.setHasMore(false);
                }

                callback.onSuccess(more);
            }

        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }


    @Background
    public void retrieveFavor(More more, Callback<More> callback) {
        callback.before();
        try {
            QueryBuilder<Content, Integer> qb1 = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, true).limit(more.getScale());

            if (more.getLastId() != null) {
                Where<Content, Integer> where = qb1.where();
                Where<Content, Integer> left = where.eq(Content.FIELD_liked, true);
                Where<Content, Integer> right = where.gt(Content.FIELD_contentUid, more.getLastId());
                where.and(left, right);
            } else {
                qb1.where().eq(Content.FIELD_liked, true);
            }

            List<Content> list = qb1.query();
            more.setContent(list);

            if (list != null && !list.isEmpty()) {
                QueryBuilder<Content, Integer> qb3 = contentDao.queryBuilder().orderBy(Content.FIELD_contentUid, false);
                qb3.where().eq(Content.FIELD_liked, true);

                Content last = qb3.queryForFirst();
                Content lastContent = list.get(list.size() - 1);
                more.setHasMore(last.getContentUid().intValue() > lastContent.getContentUid().intValue());
            } else {
                more.setHasMore(false);
            }

            callback.onSuccess(more);

        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            callback.onFinish();
        }
    }
}
