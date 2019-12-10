package com.appskimo.app.bible.support;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.appskimo.app.bible.BuildConfig;
import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Backup;
import com.appskimo.app.bible.domain.Bible;
import com.appskimo.app.bible.domain.Content;
import com.appskimo.app.bible.event.OnCompleteOrmLite;
import com.appskimo.app.bible.event.ProgressMessage;
import com.appskimo.app.bible.service.PrefsService_;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SQLiteOpenHelper extends OrmLiteSqliteOpenHelper {
    private Context context;
    private PrefsService_ prefs;

    public SQLiteOpenHelper(Context context) {
        super(context, context.getString(R.string.db_name), null, context.getResources().getInteger(R.integer.db_version));
        this.context = context;
    }

    public SQLiteOpenHelper setPrefs(PrefsService_ prefs) {
        this.prefs = prefs;
        return this;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            prefs.initializedDbStatus().put(1);

            dropAndCreateTables(connectionSource);
            initialize(database);

            prefs.initializedDbStatus().put(2);
        } catch (Exception e) {
            prefs.initializedDbStatus().put(3);
            if (BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        } finally {
            EventBus.getDefault().post(new OnCompleteOrmLite());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            prefs.initializedDbStatus().put(1);

            createBackupTables(connectionSource);
            backup(database);

            dropAndCreateTables(connectionSource);
            initialize(database);

            restoreDatas(database);
            dropBackupTables(connectionSource);

            prefs.initializedDbStatus().put(2);
        } catch (Exception e) {
            prefs.initializedDbStatus().put(3);
            if (BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        } finally {
            EventBus.getDefault().post(new OnCompleteOrmLite());
        }
    }

    private void dropAndCreateTables(ConnectionSource connectionSource) throws Exception {
        TableUtils.dropTable(connectionSource, Content.class, true);
        TableUtils.dropTable(connectionSource, Bible.class, true);

        TableUtils.createTable(connectionSource, Bible.class);
        TableUtils.createTable(connectionSource, Content.class);
    }

    private void initialize(SQLiteDatabase database) throws Exception {
        executeQuery(database, bibleBinderJson, R.string.sql_bible_insert, R.raw.bibles);
        executeQuery(database, contentBinderJson, R.string.sql_content_insert, contents);
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private BinderJson bibleBinderJson = (statement, itemArray) -> {
        statement.bindLong(1, itemArray.getLong(0));
        statement.bindLong(2, itemArray.getLong(1));
        statement.bindString(3, itemArray.getString(2));
        statement.bindString(4, itemArray.getString(3));
        statement.bindLong(5, itemArray.getLong(4));
        statement.bindString(6, itemArray.getString(5));
        statement.bindString(7, itemArray.getString(6));
        statement.bindLong(8, itemArray.getLong(7));
        statement.bindString(9, itemArray.getString(8));
        statement.bindString(10, itemArray.getString(9));
        statement.bindLong(11, itemArray.getLong(10));
        statement.bindString(12, itemArray.getString(11));
        statement.bindString(13, itemArray.getString(12));
        statement.bindLong(14, itemArray.getLong(13));
        statement.bindString(15, itemArray.getString(14));
        statement.bindString(16, itemArray.getString(15));
        statement.bindLong(17, itemArray.getLong(16));
        statement.bindString(18, itemArray.getString(17));
        statement.bindString(19, itemArray.getString(18));
        statement.bindLong(20, itemArray.getLong(19));
        statement.bindString(21, itemArray.getString(20));
        statement.bindString(22, itemArray.getString(21));
        statement.bindLong(23, itemArray.getLong(22));
        statement.bindString(24, itemArray.getString(23));
        statement.bindString(25, itemArray.getString(24));
        statement.bindLong(26, itemArray.getLong(25));
    };

    private BinderJson contentBinderJson = (statement, itemArray) -> {
        statement.bindLong(1, itemArray.getLong(0));
        statement.bindLong(2, itemArray.getLong(1));
        statement.bindLong(3, itemArray.getLong(2));
        statement.bindString(4, itemArray.getString(3));
        statement.bindString(5, itemArray.getString(4));
        statement.bindString(6, itemArray.getString(5));
        statement.bindString(7, itemArray.getString(6));
        statement.bindString(8, itemArray.getString(7));
        statement.bindString(9, itemArray.getString(8));
        statement.bindString(10, itemArray.getString(9));
        statement.bindString(11, itemArray.getString(10));
        statement.bindString(12, itemArray.getString(11));
        statement.bindString(13, itemArray.getString(12));
    };

    private BinderCursor restoreWordBinder = (statement, c) -> {
        statement.bindLong(1, c.getInt(c.getColumnIndex("liked")));
        statement.bindLong(2, c.getInt(c.getColumnIndex("contentUid")));
    };

    // ------------------------------------------------------------------------------------------------------------------------------

    public interface BinderJson {
        void bind(SQLiteStatement statement, JSONArray itemArray) throws Exception;
    }

    private interface BinderCursor {
        void bind(SQLiteStatement statement, Cursor c);
    }

    private void executeQuery(SQLiteDatabase database, int queryResId) {
        SQLiteStatement statement = null;
        try {
            database.beginTransaction();
            statement = database.compileStatement(context.getString(queryResId));
            statement.execute();
            statement.close();
            database.setTransactionSuccessful();
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }

    private void executeQuery(SQLiteDatabase database, BinderJson binderJson, int queryResId, int... arr) throws Exception {
        BufferedReader br = null;
        SQLiteStatement statement = null;
        try {
            database.beginTransaction();
            statement = database.compileStatement(context.getString(queryResId));

            for (int j = 0; j < arr.length; j++) {
                EventBus.getDefault().post(new ProgressMessage(context.getResources().getString(R.string.message_progress_update) + " ::: " + (j + 1) + "/" + arr.length));
                br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(arr[j]), "utf-8"));
                String str = br.readLine();
                if (str != null) {
                    JSONArray jsonArr = new JSONArray(str);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        binderJson.bind(statement, (JSONArray) jsonArr.get(i));
                        statement.execute();
                    }
                }
                br.close();
            }

            statement.close();
            database.setTransactionSuccessful();
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (database.inTransaction()) {
                database.endTransaction();
            }

            if (br != null) {
                br.close();
            }
        }
    }

    private void executeQuery(SQLiteDatabase database, BinderCursor binder, int selectResId, int updateResId) {
        SQLiteStatement statement = null;
        Cursor cursor = null;
        try {
            database.beginTransaction();
            statement = database.compileStatement(context.getString(updateResId));

            cursor = database.rawQuery(context.getString(selectResId), null);
            if (cursor != null) {
                while(cursor.moveToNext()) {
                    binder.bind(statement, cursor);
                    statement.execute();
                }
                cursor.close();
            }
            statement.close();
            database.setTransactionSuccessful();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }


    // ------------------------------------------------------------------------------------------------------------------------------

    private void createBackupTables(ConnectionSource connectionSource) throws Exception {
        TableUtils.createTable(connectionSource, Backup.ContentUpdate.class);
    }

    private void backup(SQLiteDatabase database) {
        EventBus.getDefault().post(new ProgressMessage(context.getResources().getString(R.string.message_progress_update)));
        executeQuery(database, R.string.sql_backup_content_update);
    }

    private void restoreDatas(SQLiteDatabase database) {
        EventBus.getDefault().post(new ProgressMessage(context.getResources().getString(R.string.message_progress_update)));
        executeQuery(database, restoreWordBinder, R.string.sql_restore_content_update1, R.string.sql_restore_content_update2);
    }

    private void dropBackupTables(ConnectionSource connectionSource) throws Exception {
        EventBus.getDefault().post(new ProgressMessage(context.getResources().getString(R.string.message_progress_update)));
        TableUtils.dropTable(connectionSource, Backup.ContentUpdate.class, true);
    }


    // ------------------------------------------------------------------------------------------------------------------------------


    private int[] contents = {
            R.raw.contents001,
            R.raw.contents002,
            R.raw.contents003,
            R.raw.contents004,
            R.raw.contents005,
            R.raw.contents006,
            R.raw.contents007,
            R.raw.contents008,
            R.raw.contents009,
            R.raw.contents010,
            R.raw.contents011,
            R.raw.contents012,
            R.raw.contents013,
            R.raw.contents014,
            R.raw.contents015,
            R.raw.contents016,
            R.raw.contents017,
            R.raw.contents018,
            R.raw.contents019,
            R.raw.contents020,
            R.raw.contents021,
            R.raw.contents022,
            R.raw.contents023,
            R.raw.contents024,
            R.raw.contents025,
            R.raw.contents026,
            R.raw.contents027,
            R.raw.contents028,
            R.raw.contents029,
            R.raw.contents030,
            R.raw.contents031,
            R.raw.contents032,
            R.raw.contents033,
            R.raw.contents034,
            R.raw.contents035,
            R.raw.contents036,
            R.raw.contents037,
            R.raw.contents038,
            R.raw.contents039,
            R.raw.contents040,
            R.raw.contents041,
            R.raw.contents042,
            R.raw.contents043,
            R.raw.contents044,
            R.raw.contents045,
            R.raw.contents046,
            R.raw.contents047,
            R.raw.contents048,
            R.raw.contents049,
            R.raw.contents050,
            R.raw.contents051,
            R.raw.contents052,
            R.raw.contents053,
            R.raw.contents054,
            R.raw.contents055,
            R.raw.contents056,
            R.raw.contents057,
            R.raw.contents058,
            R.raw.contents059,
            R.raw.contents060,
            R.raw.contents061,
            R.raw.contents062,
            R.raw.contents063,
            R.raw.contents064,
            R.raw.contents065,
            R.raw.contents066,
            R.raw.contents067,
            R.raw.contents068,
            R.raw.contents069,
            R.raw.contents070,
            R.raw.contents071,
            R.raw.contents072,
            R.raw.contents073,
            R.raw.contents074,
            R.raw.contents075,
            R.raw.contents076,
            R.raw.contents077,
            R.raw.contents078,
            R.raw.contents079,
            R.raw.contents080,
            R.raw.contents081,
            R.raw.contents082,
            R.raw.contents083,
            R.raw.contents084,
            R.raw.contents085,
            R.raw.contents086,
            R.raw.contents087,
            R.raw.contents088,
            R.raw.contents089,
            R.raw.contents090,
            R.raw.contents091,
            R.raw.contents092,
            R.raw.contents093,
            R.raw.contents094,
            R.raw.contents095,
            R.raw.contents096,
            R.raw.contents097,
            R.raw.contents098,
            R.raw.contents099,
            R.raw.contents100
    };
    // ------------------------------------------------------------------------------------------------------------------------------

    public static class BibleDao extends RuntimeExceptionDao<Bible, Integer> {
        public BibleDao(Dao<Bible, Integer> dao) {
            super(dao);
        }
    }

    public static class ContentDao extends RuntimeExceptionDao<Content, Integer> {
        public ContentDao(Dao<Content, Integer> dao) {
            super(dao);
        }
    }
}