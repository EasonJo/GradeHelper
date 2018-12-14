package com.eason.grade.db;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.eason.grade.BaseApplication;
import com.eason.grade.utils.FileUtils;
import com.eason.grade.utils.PermissionsChecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * 数据库备份
 *
 * @author Eason
 */
public class DBBackup extends AsyncTask<String, Void, Integer> {
    private static final String TAG = "DBBackup";
    public static final String COMMAND_BACKUP = "backupDatabase";
    public static final String COMMAND_RESTORE = "restroeDatabase";

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private CallBack callBack;

    public DBBackup(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isdbBackFileExist() {
        File exportDir = new File(FileUtils.getAppStorageDir(),
            "db");
        if (exportDir.exists()) {
            File[] files = exportDir.listFiles();
            for (File file : files) {
                if (file.getName().equals(BaseApplication.DB_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected Integer doInBackground(String... params) {

        boolean b = PermissionsChecker.hasEachPermissions(WRITE_EXTERNAL_STORAGE);
        if (!b) {
            if (callBack != null) callBack.onResult(false);
            return Log.w(TAG, "doInBackground: 备份或者恢复失败,没有写存储卡权限");
        }

        // 获得正在使用的数据库路径，我的是 sdcard 目录下的 /dlion/db_dlion.db
        // 默认路径是 /data/data/(包名)/databases/*.db
        File dbFile = mContext.getDatabasePath(BaseApplication.DB_NAME);


        File exportDir = new File(FileUtils.getAppStorageDir(),
            "db");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File backup = new File(exportDir, dbFile.getName());
        String command = params[0];
        if (command.equals(COMMAND_BACKUP)) {
            try {
                backup.createNewFile();
                fileCopy(dbFile, backup);
                if (callBack != null) callBack.onResult(true);
                return Log.d("backup", "ok");
            } catch (Exception e) {

                e.printStackTrace();
                if (callBack != null) callBack.onResult(false);
                return Log.d("backup", "fail");
            }
        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                fileCopy(backup, dbFile);
                if (callBack != null) callBack.onResult(true);
                return Log.d("restore", "success");
            } catch (Exception e) {
                e.printStackTrace();
                if (callBack != null) callBack.onResult(false);
                return Log.d("restore", "fail");
            }
        } else {
            if (callBack != null) callBack.onResult(false);
            return null;
        }
    }

    private void fileCopy(File dbFile, File backup) throws IOException {
        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public interface CallBack {
        void onResult(Boolean b);
    }
}