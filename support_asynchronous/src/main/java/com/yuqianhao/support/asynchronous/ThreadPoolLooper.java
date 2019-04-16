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

    protected ThreadPoolLooper(){}

    private static final Handler HANDLER=new Handler(Looper.getMainLooper());
    private static final Executor EXECUTOR=new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>()
    );

    public void run(Runnable runnable){
        EXECUTOR.execute(runnable);
    }

    public void runUI(Runnable runnable){
        HANDLER.post(runnable);
    }



}
