package net.youmi.android.libs.common.cache;

import java.io.Serializable;

public interface Interface_Serializable extends Serializable {

    /**
     * 将该类序列化为json字符串<br/>
     * 注意，这里是推荐序列化为json字符串，但并不强制要求，只要deserialize可以保证反序列化即可。
     *
     * @return
     */
    String serialize();

    /**
     * 将json字符串序列为类，主要工作是为该类对象中的属性赋值<br/>
     * 注意，这里是推荐序列化的源为json字符串，但并不强制要求，只要字符串是来源于seriablize方法即可。
     *
     * @param json
     * @return
     */
    boolean deserialize(String json);

    /**
     * 该序列化对象可缓存的时间，单位为ms
     * 小于或等于0表示永久缓存。
     *
     * @return
     */
    long getValidCacheTime_ms();

    /**
     * 获取序列化对象在数据库中的索引。
     *
     * @return
     */
    String getCacheKey();
}
