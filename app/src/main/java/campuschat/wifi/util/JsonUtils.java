package campuschat.wifi.util;

import com.alibaba.fastjson.JSON;

/**
 * @fileName JsonUtils.java
 * @package szu.nammasit.android.util
 * @description
 */
public class JsonUtils {
 
    /**
     *
     * 
     * @param paramObject
     *
     */
    public static String createJsonString(Object paramObject) {
        String str = JSON.toJSONString(paramObject);
        return str;
    }

    /**
     *
     * 
     * @param <T>
     * @return
     */
    public static <T> T getObject(String paramJson, Class<T> paramCls) {
        T t = null;
        try {
            t = JSON.parseObject(paramJson, paramCls);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

}
