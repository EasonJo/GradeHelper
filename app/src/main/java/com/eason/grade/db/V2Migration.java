package com.eason.grade.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * V2 版本.只改了 Record 数据结构
 */
public class V2Migration implements Migration {
    @Override
    public void migrate(SQLiteDatabase db) {
        //更新  类
//        MigrationHelper.migrate(db, RecordDao.class);
    }
}
