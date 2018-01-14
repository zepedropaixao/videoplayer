package me.paixao.videoplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.greenrobot.eventbus.EventBus;


public class App extends Application {
    private static App instance;

    // Logging level
    public static boolean activeLogs = true;

    SharedPreferences sharedPreferences;

    public Handler handler;

    protected final App _this = this;

    String version, versionName = "";

    public static Toast mToast;

    public EventBus bus;

    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("infraspeak.activity",
                Context.MODE_PRIVATE);

        instance = this;

        handler = new Handler(getContext().getMainLooper());

        FlowManager.init(new FlowConfig.Builder(this)
                .openDatabasesOnInit(true)
                .build());

        bus = EventBus.getDefault();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Integer currentVersion = pInfo.versionCode;
            version = currentVersion.toString();
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void putPref(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getPref(String key) {
        return sharedPreferences.getString(key, null);
    }

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }

    public void toast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(_this, toast, Toast.LENGTH_LONG);
                    mToast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    le("Failed to perform toast: '" + toast + "', continue anyway");
                }
            }
        });
    }

    public void toast(final int toast_id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(_this, getString(toast_id), Toast.LENGTH_LONG);
                    mToast.show();
                } catch (Exception e) {
                    le("Failed to perform toast: '" + getString(toast_id) + "', continue anyway");
                }
            }
        });
    }

    public void toast(final int toast_id, final Object... formatArgs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(_this, getString(toast_id, formatArgs), Toast.LENGTH_LONG);
                    mToast.show();
                } catch (Exception e) {
                    le("Failed to perform toast: '" + getString(toast_id, formatArgs) + "', continue anyway");
                }
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        try {
            handler.post(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void l(String message) {
        l("DEBUG", message);
    }

    public void l(String klazz, String message) {
        if (activeLogs)
            Log.i(klazz, message);
    }

    public void le(String message) {
        le("ERROR", message);
    }

    public void le(String klazz, String message) {
        if (activeLogs)
            Log.e(klazz, message);
    }

    // Shared Preferences configurations
    public String getValue() {
        return getPref("value");
    }

    public void setValue(String value) {
        putPref("value", value);
    }


}
