package theliars.nammasit.util;

import android.util.Log;

/**
 * @fileName LogUtils.java
 * @package
 */

public class LogUtils {

    private static boolean isShow = true;

    public static void setLogStatus(boolean flag){
        isShow = flag;
    }

    public static void d(String tag, String msg) {
        if (isShow)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isShow)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isShow)
            Log.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isShow)
            Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isShow)
            Log.w(tag, msg);
    }

    public static void wtf(String tag, String msg) {
        if (isShow)
            Log.wtf(tag, msg);
    }

}
