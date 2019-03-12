package com.coolweather.android;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类用于销毁活动，finishAll（）方法可以直接退出程序，当然担心没有彻底退出，还可以加上杀掉进程
 * 的语句
 */
public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    /**
     * 通过类名可以直接调用该方法，然后销毁所有添加到活动收集器里的活动
     */
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }
}
