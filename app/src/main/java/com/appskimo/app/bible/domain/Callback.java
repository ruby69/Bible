package com.appskimo.app.bible.domain;

public abstract class Callback<T> {

    public void before(){};
    public void onSuccess(T t){}
    public void onFinish(){}
    public void onError(){}

}
