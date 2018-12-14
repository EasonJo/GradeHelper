package com.eason.grade.base

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.eason.grade.db.DBBackup
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Activity基类，自动进行ButterKnife绑定
 */
abstract class BaseActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()
    //    lateinit var settingConfigBean: SettingConfigBean
//    lateinit var settingConfig: SettingConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        checkPermission()
//        settingConfigBean = ConfigUtils.getConfigure(BaseApplication.application)
//        settingConfig = SettingConfig(BaseApplication.application)
    }

    /**
     * Add Rxjava mission to [CompositeDisposable], prevent memory leak
     *
     * @param d [Disposable]
     */
    fun add(d: Disposable) {
        disposables.add(d)
    }

    override fun onDestroy() {
        super.onDestroy()
        //release all rxjava resource
        disposables.dispose()
        disposables.clear()
    }

    @SuppressLint("CheckResult")
    private fun checkPermission() {
        RxPermissions(this)
            .requestEach(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                if (!permission.granted) {
                    Toast.makeText(applicationContext, "未取得" + permission.name + "权限,可能运行异常", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * 在第一次读取数据库数据的时候,尝试还原备份的数据库文件
     */
    fun tryRestoreAllDBWhenFirstUseDBData() {

        val dbBackup = DBBackup(this)
        if (dbBackup.isdbBackFileExist()) {
            MaterialDialog.Builder(this)
                .content("检测到数据库有备份文件,是否恢复")
                .positiveText("确认恢复")
                .negativeText("取消")
                .onPositive { _, _ ->
                    dbBackup.setCallBack { b ->
                        runOnUiThread {
                            if (b!!) {
                                Toast.makeText(this@BaseActivity, "数据库恢复成功,请重新进入当前页面", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@BaseActivity, "数据库恢复失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    dbBackup.execute(DBBackup.COMMAND_RESTORE)
                    //settingConfig.isFirstBoot = false
                }
                .onNegative { _, _ ->
                    //settingConfig.isFirstBoot = false
                }
                .show()
        }
    }
}
