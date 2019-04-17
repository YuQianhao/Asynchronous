package com.yuqianhao.support.asynchronous;

import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by machunyan on 2019/4/16.
 */
public class Async {

    public static interface HandleMap<_Tx,_Ty>{void map(Map<_Tx,_Ty> map);}
    public static interface HandleSet<_Tx>{void set(Set<_Tx> set);}
    public static interface HandleList<_Tx>{void list(List<_Tx> list);}
    public static interface HandleObject<_Tx>{void obj(_Tx obj);}


    //线程执行器
    private static final ThreadPoolLooper THREAD_POOL_LOOPER=new ThreadPoolLooper();

    //执行器结果和 id 映射表
    private Map<Integer,ExectuorValue> exectuorValueMap=new HashMap<>();

    //执行器 id 和捕获执行器结果的接口映射
    private Map<Integer,IExecutorComplete> executorCompleteMap=new HashMap<>();

    //执行器 id 和执行的线程类型映射表
    private Map<Integer,ExecutorThread> executorThreadMap=new HashMap<>();

    //默认的线程类型
    private ExecutorThread mRunThread=ExecutorThread.MAIN;

    //临时对象
    private Object tmpObject;

    //临时对象
    private List tmpList;

    //临时对象
    private Map tmpMap;

    //临时对象
    private Set tmpSet;

    //通过crate创建的所有Async对象引用
    private static final List<WeakReference<Async>> ASYNC_WEAKREF_LIST=new ArrayList<>();

    //默认的结果投放
    private final IExecutorComplete globalExecutorComplete=new IExecutorComplete() {
        @Override
        public void onComplete(ExectuorValue exectuorValue) {
            ExecutorThread thread=executorThreadMap.get(exectuorValue.getId());
            if(thread==null){
                thread=mRunThread;
            }
            if(executorCompleteMap.containsKey(exectuorValue.getId())){
                IExecutorComplete executorComplete=executorCompleteMap.get(exectuorValue.getId());
                postExecutorValue(thread,exectuorValue,executorComplete);
            }
        }
    };

    /**
     * 切换到主线程
     * @return
     */
    public Async ui(){
        mRunThread=ExecutorThread.MAIN;
        return this;
    }

    /**
     * 切换到子线程
     * @return
     */
    public Async io(){
        mRunThread=ExecutorThread.IO;
        return this;
    }

    /**
     * 执行某个代码块
     * @param thread 要执行的线程
     * @param executor 执行器,详见{@link IExecutor}
     * @param params 执行器的参数,可以不填
     * @return
     */
    public <_Tx> Async call(ExecutorThread thread, final IExecutor executor, final _Tx ...params){
        post(thread, new Runnable() {
            @Override
            public void run() {
                Object o =executor.run(params);
                ExectuorValue exectuorValue=new ExectuorValue();
                exectuorValue.setRetValue(o);
                exectuorValue.setId(executor.id());
                if(exectuorValueMap.containsKey(exectuorValue.getId())){
                    exectuorValueMap.remove(exectuorValue.getId());
                }
                exectuorValueMap.put(exectuorValue.getId(),exectuorValue);
                globalExecutorComplete.onComplete(exectuorValue);
            }
        });
        return this;
    }

    /**
     * 在当前线程中执行某个代码块
     * @param executor 执行器,详见{@link IExecutor}
     * @param params 执行器的参数,可以不填
     * @return
     */
    public <_Tx> Async call(IExecutor executor,_Tx ...params){
        call(mRunThread,executor,params);
        return this;
    }


    /**
     * 捕获执行器的返回结果
     * @param thread 要执行的线程
     * @param id 要捕获的执行器的 id
     * @param executorComplete 捕获的结果接收器
     * @return
     */
    public <_Tx> Async capture(ExecutorThread thread, final int id, final IExecutorComplete<_Tx> executorComplete){
        post(thread, new Runnable() {
            @Override
            public void run() {
                if(executorCompleteMap.containsKey(id)){
                    executorCompleteMap.remove(id);
                }
                executorCompleteMap.put(id,executorComplete);
                ExectuorValue value=exectuorValueMap.get(id);
                if(value!=null){
                    executorComplete.onComplete(value);
                }
            }
        });
        return this;
    }

    /**
     * 在主线程中捕获执行器返回的结果
     * @param id 要捕获的执行器的 id
     * @param executorComplete 捕获的结果接收器
     * @return
     */
    public  <_Tx> Async capture(final int id,final IExecutorComplete<_Tx> executorComplete){
        capture(mRunThread,id,executorComplete);
        return this;
    }

    public <_Tx> Async push(_Tx o){
        tmpList.add(o);
        return this;
    }

    public <_Tx> Async send(_Tx o){
        tmpObject=o;
        return this;
    }

    public <_Tx> Async sendList(List<_Tx> txList){
        tmpList=txList;
        return this;
    }

    public Async clearList(){
        tmpList.clear();
        return this;
    }

    public <_Tx> Async sendSet(Set<_Tx> txSet){
        tmpSet=txSet;
        return this;
    }

    public Async clearSet(){
        tmpSet.clear();
        return this;
    }

    public <_Tx,_Ty> Async sendMap(Map<_Tx,_Ty> txMap){
        tmpMap=txMap;
        return this;
    }

    public Async clearMap(){
        tmpMap.clear();
        return this;
    }

    public <_Tx> Async handleObject(final HandleObject<_Tx> handleObject){
        post(mRunThread, new Runnable() {
            @Override
            public void run() {
                handleObject.obj((_Tx) tmpObject);
            }
        });
        return this;
    }

    public <_Tx> Async handleList(final HandleList<_Tx> handleList){
        post(mRunThread, new Runnable() {
            @Override
            public void run() {
                handleList.list(tmpList);
            }
        });
        return this;
    }

    public <_Tx> Async handleSet(final HandleSet<_Tx> handleSet){
        post(mRunThread, new Runnable() {
            @Override
            public void run() {
                handleSet.set(tmpSet);
            }
        });
        return this;
    }

    public <_Tx,_Ty> Async handleMap(final HandleMap<_Tx,_Ty> handleMap){
        post(mRunThread, new Runnable() {
            @Override
            public void run() {
                handleMap.map(tmpMap);
            }
        });
        return this;
    }

    public synchronized Async run(Runnable runnable){
        post(mRunThread,runnable);
        return this;
    }


    protected Async(){
        mRunThread=(Looper.getMainLooper().equals(Looper.myLooper()))?ExecutorThread.MAIN:ExecutorThread.IO;
    }
    public static Async create(){
        Async async=new Async();
        ASYNC_WEAKREF_LIST.add(new WeakReference(async));
        return async;
    }

    protected final Map<Integer,ExectuorValue> getExectuorValueMap(){
        return exectuorValueMap;
    }

    protected static final List<WeakReference<Async>> getAsyncWeakrefList(){
        return ASYNC_WEAKREF_LIST;
    }

    //执行器结果分发
    private final void postExecutorValue(ExecutorThread thread,
                                         final ExectuorValue exectuorValue,
                                         final IExecutorComplete ...executorCompletes){
        post(thread, new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<executorCompletes.length;i++){
                    executorCompletes[i].onComplete(exectuorValue);
                }
            }
        });
    }


    private synchronized final void post(ExecutorThread thread,Runnable runnable){
        if(thread==ExecutorThread.MAIN){
            if(mRunThread==ExecutorThread.MAIN){
                runnable.run();
            }else{
                THREAD_POOL_LOOPER.runUI(runnable);
            }
        }else if(thread==ExecutorThread.IO){
            if(mRunThread==ExecutorThread.IO){
                runnable.run();
            }else{
                THREAD_POOL_LOOPER.run(runnable);
            }
        }
    }
}
