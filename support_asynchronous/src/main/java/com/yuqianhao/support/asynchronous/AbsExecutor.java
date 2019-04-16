package com.yuqianhao.support.asynchronous;

/**
 * Created by yuqianhao on 2019/4/16.
 */
public abstract class AbsExecutor<_Part, _Rex> implements IExecutor<_Part, _Rex> {
    @Override
    public abstract  _Rex run(_Part... params);

    @Override
    public int id() {
        return 0;
    }
}
