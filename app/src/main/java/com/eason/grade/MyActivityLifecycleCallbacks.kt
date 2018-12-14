package com.car.offroadsports

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import java.util.*

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class MyActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    private var activities: MutableList<Activity> = LinkedList()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    /**
     * 添加Activity
     */
    private fun addActivity(activity: Activity) {

        if (!activities.contains(activity)) {
            activities.add(activity)//把当前Activity添加到集合中
        }
    }

    /**
     * 移除Activity
     */
    private fun removeActivity(activity: Activity) {
        if (activities.contains(activity)) {
            activities.remove(activity)
        }

    }


    /**
     * 销毁所有activity
     */
    fun removeAllActivities() {
        for (activity in activities) {
            activity.finish()
            activity.overridePendingTransition(0, sAnimationId)
        }
    }

    companion object {
        var sAnimationId = 0
    }
}
