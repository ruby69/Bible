package com.appskimo.app.bible.service;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultFloat;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.DefaultStringSet;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface PrefsService {
    @DefaultInt(0)
    int launchedCount();

    @DefaultString("KRV")
    String edition();

    String withEdition();



    @DefaultInt(0)
    int biblePosition();

    @DefaultInt(1)
    int bibleUid();

    @DefaultInt(0)
    int contentUid();

    @DefaultInt(0)
    int chapterPosition();

    @DefaultFloat(1.15F)
    float fontScale();

    @DefaultInt(0) // 0 - before, 1 - prgoress, 2 - succeed, 3 - failed
    int initializedDbStatus();



    // Lock prefs...........

    @DefaultBoolean(false)
    boolean useLockScreen();

    @DefaultLong(0)
    long lockOffTime();

    @DefaultLong(0)
    long adExpireTime();

}
