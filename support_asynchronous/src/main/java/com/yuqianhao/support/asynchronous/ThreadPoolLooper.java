package com.yuqianhao.support.asynchronous;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by machunyan on 2019/4/16.
 */
public class ThreadPoolLooper {

    /**异步使用单线程等待执行*/
    public static final int TYPE_SIGNAL=0;

    /**异步使用多线程执行*/
    public static final int TYPE_MULTITHREADING=1;

    private Handler HANDLER=new Handler(Looper.getMainLooper());
    private Executor EXECUTOR;

    protected ThreadPoolLooper(){
        this(TYPE_MULTITHREADING);
    }

    protected ThreadPoolLooper(int type){
        EXECUTOR=new ThreadPoolExecutor(
                (type==TYPE_MULTITHREADING?0:1),
                (type==TYPE_MULTITHREADING?Integer.MAX_VALUE:1),
                1,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }



    public void run(Runnable runnable){
        EXECUTOR.execute(runnable);
    }

    public void runUI(Runnable runnable){
        HANDLER.post(runnable);
    }



}
