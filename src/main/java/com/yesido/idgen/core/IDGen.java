package com.yesido.idgen.core;

public interface IDGen {

    /**
     * 初始化
     * 
     * @author yesido
     * @date 2020年12月17日 上午11:44:43
     * @return
     */
    default boolean init() {
        return true;
    }

    /**
     * 获取id
     * 
     * @author yesido
     * @date 2020年12月17日 上午11:44:50
     * @param key
     * @return
     */
    long nextId(String key);

    /**
     * 获取id
     * 
     * @author yesido
     * @date 2020年12月17日 下午3:12:43
     * @return
     */
    long nextId();
}
