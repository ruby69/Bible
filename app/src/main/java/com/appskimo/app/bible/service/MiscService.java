package com.appskimo.app.bible.service;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.appskimo.app.bible.BuildConfig;
import com.appskimo.app.bible.On;
import com.appskimo.app.bible.R;
import com.appskimo.app.bible.domain.Callback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.net.HttpURLConnection;
import java.net.URL;

@EBean(scope = EBean.Scope.Singleton)
public class MiscService {
    @RootContext Context context;

    @Pref PrefsService_ prefs;
    @SystemService ConnectivityManager connectivityManager;
    @SystemService WindowManager windowManager;

    @StringRes(R.string.admob_app_id) String admobAppId;
    @StringRes(R.string.admob_banner_unit_id) String bannerAdUnitId;

    private AdRequest adRequest;
    private AdView rectangleAdView;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean initializedMobileAds;
    public void initializeMobileAds() {
        if (!initializedMobileAds) {
            initializedMobileAds = true;
            MobileAds.initialize(context, admobAppId);
            generateRectangleAdView(context);
        }
    }

    @UiThread
    public void loadBannerAdView(AdView adView) {
        if (adView != null && !adView.isLoading()) {
            adView.loadAd(getAdRequest());
        }
    }

    private AdRequest getAdRequest() {
        if(adRequest == null) {
            AdRequest.Builder builder = new AdRequest.Builder();
            if(BuildConfig.DEBUG) {
                for(String device : context.getResources().getStringArray(R.array.t_devices)) {
                    builder.addTestDevice(device);
                }
            }
            adRequest = builder.build();
        }
        return adRequest;
    }

    private AdView generateRectangleAdView(Context context) {
        if (rectangleAdView == null) {
            rectangleAdView = new AdView(context);
            rectangleAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            rectangleAdView.setAdUnitId(bannerAdUnitId);
            loadBannerAdView(rectangleAdView);
        }
        return rectangleAdView;
    }

    @UiThread
    public void showAdDialog(Activity activity, int positiveLabelResId, DialogInterface.OnClickListener positiveListener) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(" ")
                .setPositiveButton(positiveLabelResId, positiveListener)
                .create();

        AdView adView = generateRectangleAdView(activity);
        if (adView != null) {
            ViewParent parent = adView.getParent();
            if(parent != null) {
                ((ViewGroup) parent).removeView(adView);
            }
            dialog.setView(adView);
        }

        if (!activity.isFinishing() && !activity.isDestroyed()) {
            dialog.show();
        }
    }

    @UiThread
    public void showAdDialog(Activity activity, int titleResId, int positiveLabelResId, DialogInterface.OnClickListener positiveListener, int negativeLabelResId, DialogInterface.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(titleResId)
                .setPositiveButton(positiveLabelResId, positiveListener)
                .setNegativeButton(negativeLabelResId, negativeListener)
                .create();

        AdView adView = generateRectangleAdView(activity);
        if (adView != null) {
            ViewParent parent = adView.getParent();
            if(parent != null) {
                ((ViewGroup) parent).removeView(adView);
            }
            dialog.setView(adView);
        }

        if (!activity.isFinishing()) {
            dialog.show();
        }
    }


    @UiThread
    public void showAdDialog(Activity activity, On<Void> on) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(" ")
                .setPositiveButton(R.string.label_continue, (d,i) -> on.success(null))
                .setOnDismissListener(d -> on.success(null))
                .create();

        AdView adView = generateRectangleAdView(activity);
        if (adView != null) {
            ViewParent parent = adView.getParent();
            if(parent != null) {
                ((ViewGroup) parent).removeView(adView);
            }
            dialog.setView(adView);
        }

        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void applyFontScale(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.fontScale = prefs.fontScale().getOr(1.15F);

        DisplayMetrics metrics = resources.getDisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;

        context.createConfigurationContext(configuration);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isConnected() {
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
        } else {
            return false;
        }
    }

    @Background
    void checkHttpConnect(Callback<Void> callback) {
        callback.before();

        if (!isConnected()) {
            callback.onError();
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(context.getString(R.string.url_check_http)).openConnection();
            conn.setRequestProperty("User-Agent","Android");
            conn.setConnectTimeout(3000);
            conn.connect();
            if (conn.getResponseCode() == 204) {
                callback.onSuccess(null);
            } else {
                callback.onError();
            }
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
            callback.onError();
        } finally {
            if(conn != null){
                conn.disconnect();
            }
            callback.onFinish();
        }
    }

}
