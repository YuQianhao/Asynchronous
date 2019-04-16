package com.yuqianhao.support.asynchronous;

/**
 * Created by YuQianhao on 2019/4/16.
 * 执行器接口,实现该接口可以创建一个执行者,该接口拥有两个泛型类型参数,
 * _Part 执行器需要的参数类型
 * _Rex  执行器的返回结果类型
 *
 */
public interface IExecutor<_Part, _Rex> {

    /**
     * 执行函数
     * @param params
     * @return
     */
    _Rex run(_Part...params);

    /**
     * 这个执行任务的id
     * @return
     */
    int id();

}
