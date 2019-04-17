package com.yuqianhao.support.asynchronous;

/**
 * Created by machunyan on 2019/4/16.
 */
public interface IExecutorComplete<_Tx> {

    void onComplete(ExectuorValue<_Tx> exectuorValue);

}
