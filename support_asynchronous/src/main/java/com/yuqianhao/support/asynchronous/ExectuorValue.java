package com.yuqianhao.support.asynchronous;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by machunyan on 2019/4/16.
 */
public class ExectuorValue {

    private Object _retValue;

    private int id;

    private static final Async ASYNC=Async.create();

    protected ExectuorValue(){}

    public Object getRetValue() {
        return _retValue;
    }

    public void setRetValue(Object _retValue) {
        this._retValue = _retValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public <_RetType> _RetType cast(){
        return (_RetType)_retValue;
    }

    public final void finish(){
        ASYNC.io()
                .sendList(Async.getAsyncWeakrefList())
                .handleList(new Async.HandleList<WeakReference<Async>>() {
                    @Override
                    public void list(List<WeakReference<Async>> list) {
                        for(WeakReference<Async> asyncWeakReference:list){
                            if(asyncWeakReference!=null){
                                Map<Integer,ExectuorValue> exectuorValueMap=
                                        asyncWeakReference.get().getExectuorValueMap();
                                if(exectuorValueMap.containsKey(getId())){
                                    exectuorValueMap.remove(getId());
                                }
                            }
                        }
                    }
                });
    }
}
