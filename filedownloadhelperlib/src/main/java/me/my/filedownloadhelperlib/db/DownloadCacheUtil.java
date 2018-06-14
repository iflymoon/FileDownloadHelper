package me.my.filedownloadhelperlib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.my.filedownloadhelperlib.DownloadInfo;

/**
 * description:文件下载数据库操作工具类
 * Created by mingyue on 2018/5/25
 */
public class DownloadCacheUtil {

    private static DownloadCacheDbHelper dataBaseHelper;
    private static SQLiteDatabase sqLiteDatabase;

    public synchronized static boolean insertFile(Context mContext, DownloadInfo downloadInfo) {
        long rowsId = -1;
        dataBaseHelper = DownloadCacheDbHelper.getInstance(mContext);
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DownloadCacheDbHelper.FILE_URL, downloadInfo.downloadUrl);
            cv.put(DownloadCacheDbHelper.FILE_PATH, downloadInfo.filePath);
            cv.put(DownloadCacheDbHelper.FILE_TOTAL_LENGTH, downloadInfo.totalLength);
            cv.put(DownloadCacheDbHelper.FILE_READ_LENGTH, downloadInfo.readLength);
            rowsId = sqLiteDatabase.insert(DownloadCacheDbHelper.TABLE_NAME, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            if (dataBaseHelper != null)
                dataBaseHelper.close();
        }
        return rowsId != -1;
    }

    public synchronized static boolean updateFile(Context mContext, DownloadInfo downloadInfo) {
        long updateResult = 0;
        dataBaseHelper = DownloadCacheDbHelper.getInstance(mContext);
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DownloadCacheDbHelper.FILE_PATH, downloadInfo.filePath);
            cv.put(DownloadCacheDbHelper.FILE_TOTAL_LENGTH, downloadInfo.totalLength);
            cv.put(DownloadCacheDbHelper.FILE_READ_LENGTH, downloadInfo.readLength);
            updateResult = sqLiteDatabase.update(DownloadCacheDbHelper.TABLE_NAME
                    , cv
                    , DownloadCacheDbHelper.FILE_URL + "=?"
                    , new String[]{downloadInfo.downloadUrl});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            if (dataBaseHelper != null)
                dataBaseHelper.close();
        }
        return updateResult > 0;
    }

    public synchronized static DownloadInfo queryFile(Context mContext, String downloadUrl) {
        dataBaseHelper = DownloadCacheDbHelper.getInstance(mContext);
        sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        List<DownloadInfo> dataList;
        try {
            String querySql = "select * from " + DownloadCacheDbHelper.TABLE_NAME + " where "
                    + DownloadCacheDbHelper.FILE_URL + "=?";
            Cursor cursor = sqLiteDatabase.rawQuery(querySql, new String[]{downloadUrl});
            dataList = queryCursor(cursor);
            if (dataList != null && dataList.size() > 0)
                return dataList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            if (dataBaseHelper != null)
                dataBaseHelper.close();
        }
        return null;
    }

    public synchronized static boolean deleteFile(Context mContext, DownloadInfo downloadInfo) {
        dataBaseHelper = DownloadCacheDbHelper.getInstance(mContext);
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        try {
            String deleteSql = "delete from " + DownloadCacheDbHelper.TABLE_NAME +
                    " where " + DownloadCacheDbHelper.FILE_URL + "=" + downloadInfo.downloadUrl;
            sqLiteDatabase.execSQL(deleteSql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            if (dataBaseHelper != null)
                dataBaseHelper.close();
        }
        return false;
    }

    private static List<DownloadInfo> queryCursor(Cursor cursor) {
        List<DownloadInfo> downloadInfoList = new ArrayList<>();
        while (cursor.moveToNext()) {
            DownloadInfo bean = new DownloadInfo();
            bean.downloadUrl = cursor.getString(cursor.getColumnIndex(DownloadCacheDbHelper.FILE_URL));
            bean.filePath = cursor.getString(cursor.getColumnIndex(DownloadCacheDbHelper.FILE_PATH));
            bean.readLength = cursor.getLong(cursor.getColumnIndex(DownloadCacheDbHelper.FILE_READ_LENGTH));
            bean.totalLength = cursor.getLong(cursor.getColumnIndex(DownloadCacheDbHelper.FILE_TOTAL_LENGTH));
            downloadInfoList.add(bean);
        }
        return downloadInfoList;
    }

}
