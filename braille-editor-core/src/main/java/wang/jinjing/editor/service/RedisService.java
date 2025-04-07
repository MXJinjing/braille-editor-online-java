package wang.jinjing.editor.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface RedisService {


    /**
     * 保存对象，无超时
     */
    <T> void setCacheObject(String key, T value);

    /**
     * 保存对象，含超时
     */
    <T> void setCacheObject(String key, T value, long timeout, TimeUnit timeUnit);

    /**
     * 保存属性，含超时
     */
    void set(String key, String value, long expire, TimeUnit timeUnit);

    /**
     * 保存属性，无超时
     */
    void set(String key, String value);

    /**
     * 获取属性
     */
    String get(String key);

    /**
     * 删除属性
     */
    Boolean del(String key);

    /**
     * 批量删除属性
     */
    Long del(List<String> keys);

    /**
     * 设置过期时间
     */
    Boolean expire(String key, int timeout, TimeUnit timeUnit);

    /**
     * 获取过期时间
     */
    Long getExpire(String key);

    /**
     * 判断属性存在
     */
    Boolean hasKey(String key);
}
