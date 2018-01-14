package me.paixao.videoplayer.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;

import me.paixao.videoplayer.App;

public class BaseActivity extends AppCompatActivity {

    App app = App.getInstance();
    protected final int REQUEST_PERMISSION = 1986;

    BaseActivity _this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        // Stub
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (grantResults.length > i && (grantResults[i] != PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    requestPermission(permission);
                } else {
                    requestPermissionGoToAppSettings();
                }
            } else {
                refresh();
            }
        }
    }

    public void requestPermission(final String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("VideoPlayer requires several permissions to function, without it, it will not work properly.")
                .setCancelable(false)
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(
                                _this, new String[]{permission}, REQUEST_PERMISSION);
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void requestPermissionGoToAppSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Whithout the required permissions, VideoPlayer will not work, please give the required permissions at the settings of the app. ")
                .setCancelable(false)
                .setPositiveButton("Go To App Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        _this.finish();
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean checkPermissions() {
        int permissionFileWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionFileRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionFileWrite != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (permissionFileRead != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void requestPermissions() {
        int permissionFileWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionFileRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        ArrayList<String> perms = new ArrayList<>();


        if (permissionFileWrite != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionFileRead != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, perms.toArray(new String[perms.size()]), REQUEST_PERMISSION);
        }
    }

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public void toast(String text) {
        app.toast(text);
    }

    public void toast(int text_id) {
        app.toast(text_id);
    }

    public void toast(int text_id, Object... formatArgs) {
        app.toast(text_id, formatArgs);
    }

    public static void l(String tag, String message) {
        App.getInstance().l(tag, message);
    }

    public static void le(String tag, String message) {
        App.getInstance().le(tag, message);
    }

    public void l(String message) {
        app.l(getClassName(), message);
    }

    public void le(String message) {
        app.le(getClassName(), message);
    }

    public ArrayList<String> getAllMedia() {
        HashSet<String> videoItemHashSet = new HashSet<String>();
        String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                videoItemHashSet.add((cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))));
            } while (cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> downloadedList = new ArrayList<>(videoItemHashSet);
        return downloadedList;
    }
}
