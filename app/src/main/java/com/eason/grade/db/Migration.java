package com.eason.grade.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * 描述：数据迁移接口
 */
public interface Migration {
    /**
     * 数据库更改回调
     *
     * @param db
     */
    void migrate(SQLiteDatabase db);
}
