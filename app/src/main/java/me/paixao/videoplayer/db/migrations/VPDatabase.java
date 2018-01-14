package me.paixao.videoplayer.db.migrations;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = VPDatabase.NAME, version = VPDatabase.VERSION)
public class VPDatabase {
    public static final String NAME = "VPDatabase";
    public static final int VERSION = 1;
}
