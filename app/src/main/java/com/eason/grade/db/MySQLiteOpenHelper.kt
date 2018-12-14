package com.eason.grade.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.eason.grade.bean.gen.DaoMaster
import java.util.*

/**
 * DatabaseOpenHelper, 实现数据库的升级而不丢失数据
 *
 * @author Eason
 */
class MySQLiteOpenHelper : DaoMaster.OpenHelper {

    constructor(context: Context, name: String) : super(context, name)

    constructor(context: Context, name: String, factory: SQLiteDatabase.CursorFactory) : super(context, name, factory)

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i("greenDAO", "Upgrading schema from version $oldVersion to $newVersion by dropping all tables")
        //        MigrationHelper.migrate(db,
        //            FriendDao.class,
        //            PointDao.class,
        //            PointCaDao.class,
        //            RecordDao.class,
        //            RoadTrackDao.class);

        val migrations = ALL_MIGRATIONS.subMap(oldVersion, false, newVersion, true)
        executeMigrations(db, migrations.keys)

    }

    private fun executeMigrations(paramSQLiteDatabase: SQLiteDatabase, migrationVersions: Set<Int>) {
        for (version in migrationVersions) {
            ALL_MIGRATIONS[version]?.migrate(paramSQLiteDatabase)
        }
    }

    companion object {
        private val ALL_MIGRATIONS = TreeMap<Int, Migration>()

        init {
            //ALL_MIGRATIONS[1] = V1Migration()
            //数据库第二个版本,更新的成绩
            ALL_MIGRATIONS[2] = V2Migration()
        }
    }
}

