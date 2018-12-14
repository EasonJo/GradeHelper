package com.eason.grade.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.eason.grade.utils.FileUtils;
import com.eason.grade.utils.PermissionsChecker;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class GreenDaoContext extends ContextWrapper {
    private static final String TAG = "GreenDaoContext";
    private Context mContext;

    public GreenDaoContext(Context base) {
        super(base);
        mContext = base;
    }

    /**
     * 获得数据库路径，如果不存在，则创建对象,需要检查权限
     *
     * @param dbName
     */
    @Override
    public File getDatabasePath(String dbName) {
        //检查是否有读取和写 sd 权限
        boolean b = PermissionsChecker.hasEachPermissions(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
        File dbFile = null;
        if (b) {
            String dbDir = FileUtils.getAppStorageDir() + "/db";

            File baseFile = new File(dbDir);
            // 目录不存在则自动创建目录
            if (!baseFile.exists()) {
                boolean mkdirs = baseFile.mkdirs();
            }

            dbFile = new File(baseFile, dbName);

            // 数据库文件是否创建成功
            boolean isFileCreateSuccess = false;
            // 判断文件是否存在，不存在则创建该文件
            if (!dbFile.exists()) {
                try {
                    isFileCreateSuccess = dbFile.createNewFile();// 创建文件
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!isFileCreateSuccess) {
                    dbFile = super.getDatabasePath(dbName);
                }
            }
        } else {
            //没有权限就使用默认的路径
            dbFile = super.getDatabasePath(dbName);
        }
        Log.d(TAG, "getDatabasePath: 数据库文件路径: " + dbFile.getAbsolutePath());
        return dbFile;
    }

    /**
     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
     *
     * @param name
     * @param mode
     * @param factory
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
        return result;
    }

    /**
     * Android 4.0会调用此方法获取数据库。
     *
     * @param name
     * @param mode
     * @param factory
     * @param errorHandler
     * @see ContextWrapper#openOrCreateDatabase(String, int,
     * SQLiteDatabase.CursorFactory,
     * DatabaseErrorHandler)
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);

        return result;
    }

}
