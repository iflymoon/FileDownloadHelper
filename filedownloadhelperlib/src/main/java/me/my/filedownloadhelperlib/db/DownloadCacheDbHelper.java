package me.my.filedownloadhelperlib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * description:文件下载记录数据库
 * Created by mingyue on 2018/5/25
 */
public class DownloadCacheDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "file_download_cache.db";
    private static final int DATABASE_VERSION = 1;
    private static DownloadCacheDbHelper instance;

    public static final String TABLE_NAME = "download_cache";
    public static final String FILE_LOCALID = "id";
    public static final String FILE_URL = "download_url";
    public static final String FILE_PATH = "file_path";
    public static final String FILE_TOTAL_LENGTH = "file_total_length";
    public static final String FILE_READ_LENGTH = "file_read_length";
    public static final String FILE_INSERT_TIME = "file_insert_time";

    private final String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
            + FILE_LOCALID + " integer primary key autoincrement,"
            + FILE_INSERT_TIME + " timestamp not null default (datetime('now','localtime')),"
            + FILE_URL + " varchar(200),"
            + FILE_PATH + " varchar(100),"
            + FILE_TOTAL_LENGTH + " varchar(50),"
            + FILE_READ_LENGTH + " varchar(50),"
            + "constraint unique_download_cache unique (" + FILE_URL + "))";

    public DownloadCacheDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DownloadCacheDbHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadCacheDbHelper.class) {
                if (instance == null)
                    instance = new DownloadCacheDbHelper(context);
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            String sql;
            switch (newVersion) {
                case 2:
                    sql = "";
                    db.execSQL(sql);
            }
        }

    }

}
