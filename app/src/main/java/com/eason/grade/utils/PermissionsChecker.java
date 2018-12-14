package com.eason.grade.utils;

import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import com.eason.grade.BaseApplication;

/**
 * 权限检查
 *
 * @author Eason
 */
public class PermissionsChecker {

    /**
     * 判断是否拥有传入的每一个权限
     *
     * @param permissions 权限组
     * @return
     */
    public static boolean hasEachPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    // 判断是否缺少权限
    private static boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(BaseApplication.Companion.getApplication(), permission) ==
                PackageManager.PERMISSION_GRANTED;
    }


}
