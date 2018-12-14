package com.eason.grade

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.car.offroadsports.MyActivityLifecycleCallbacks
import com.eason.grade.bean.gen.DaoMaster
import com.eason.grade.bean.gen.DaoSession
import com.eason.grade.db.MySQLiteOpenHelper
import com.eason.grade.utils.CrashHandler
import org.greenrobot.greendao.query.QueryBuilder

/**
 * 注册GREENDAO Session，启动数据库
 */
class BaseApplication : Application() {
    private lateinit var mHelper: DaoMaster.OpenHelper
    lateinit var db: SQLiteDatabase
        private set
    private lateinit var mDaoMaster: DaoMaster
    lateinit var daoSession: DaoSession
        private set
    private val myActivityLifecycleCallbacks = MyActivityLifecycleCallbacks()

    override fun onCreate() {
        super.onCreate()
        application = this
        setDatabase()
        registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks)
        CrashHandler.getInstance().init(this)
        CrashHandler.getInstance().setMyActivityLifecycleCallbacks(myActivityLifecycleCallbacks)
    }

    /**
     * 设置greenDao
     */
    private fun setDatabase() {

        mHelper = MySQLiteOpenHelper(this, DB_NAME)

        db = mHelper.writableDatabase
        // 注意：该数据库连接属于DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = DaoMaster(db)
        daoSession = mDaoMaster.newSession()
        QueryBuilder.LOG_SQL = true
        QueryBuilder.LOG_VALUES = true
    }

    companion object {

        lateinit var application: BaseApplication
            private set
        const val DB_NAME = "info-db.db"
    }
}
