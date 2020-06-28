package com.huari.Presenter.Interface;


/**
 * @author xt
 * @version 1.0
 * @date 2020/5/11 11:21
 */
public interface DataWork<T> {//对数据来源的封装

    void initialize(DataWorkCallback dataWorkCallback);//初始化

    void resume();//继续

    void pause();//暂停

    void close();//关闭

    void sendMessage(T t);
}
