package com.huari.Presenter.Interface;

public interface Controler {
    public void networkStart(Requestcallback requestcallback);//开启连接

    public void networkEnd(Requestcallback requestcallback);//关闭连接

//    public boolean enterOnlinePage(LineUI lineUI);//进入在线页面

    public boolean exitOnlinePage();//离开在线页面
}
