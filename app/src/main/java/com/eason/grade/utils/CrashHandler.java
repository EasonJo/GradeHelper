package com.eason.grade.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.car.offroadsports.MyActivityLifecycleCallbacks;

import java.io.*;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 崩溃工具类
 * <p>
 * Created by Jocker on 2018/6/23.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 系统默认UncaughtExceptionHandler
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * context
     */
    private Context mContext;

    /**
     * 存储异常和参数信息
     */
    private Map<String, String> paramsMap = new HashMap<>();


    private String TAG = this.getClass().getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static CrashHandler mInstance;

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss SSS", Locale.getDefault());

    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例
     */
    public static synchronized CrashHandler getInstance() {
        if (null == mInstance) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void setMyActivityLifecycleCallbacks(MyActivityLifecycleCallbacks myActivityLifecycleCallbacks) {
        this.myActivityLifecycleCallbacks = myActivityLifecycleCallbacks;
    }

    private MyActivityLifecycleCallbacks myActivityLifecycleCallbacks;


    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * uncaughtException 回调函数
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            // 退出程序
            // android.os.Process.killProcess(android.os.Process.myPid());
            // System.exit(1);

            myActivityLifecycleCallbacks.removeAllActivities();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            System.gc();

        }

    }

    /**
     * 收集错误信息.发送到服务器
     *
     * @return 处理了该异常返回true, 否则false
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常即将退出.", Toast.LENGTH_LONG).show();

                final StringBuffer sb = new StringBuffer();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                ex.printStackTrace(printWriter);
                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }
                printWriter.close();
                String result = writer.toString();
                sb.append(result);
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", sb.toString());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                Toast.makeText(mContext, "错误已复制到粘贴板", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);

        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = time + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(FileUtils.getAppStorageDir(), "app-crash");
                if (!dir.exists())
                    dir.mkdir();
                String filePath = dir.getAbsolutePath() + File.separator + fileName;
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(sb.toString().getBytes("UTF-8"));
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}
