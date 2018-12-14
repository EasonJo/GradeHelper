package com.eason.grade.db;

import android.content.Context;
import com.eason.grade.BuildConfig;
import com.eason.grade.bean.gen.DaoMaster;
import com.eason.grade.bean.gen.DaoSession;

/**
 * DB Master
 */
public class GreenDaoManager {
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static GreenDaoManager mInstance; //单例

    private GreenDaoManager(Context mContext) {
        if (mInstance == null) {
            DaoMaster.OpenHelper mHelper;

            if (BuildConfig.DEBUG) {
                mHelper = new DaoMaster.DevOpenHelper(new GreenDaoContext(mContext.getApplicationContext()), "info-db", null);
            } else {
                mHelper = new MySQLiteOpenHelper(new GreenDaoContext(mContext.getApplicationContext()), "info-db");
            }

            mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
            mDaoSession = mDaoMaster.newSession();
        }
    }

    public static GreenDaoManager getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (GreenDaoManager.class) {//保证异步处理安全操作
                if (mInstance == null) {
                    mInstance = new GreenDaoManager(mContext);
                }
            }
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }
}
