package com.rayshaw.holder.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FastRpcClientProxy implements InvocationHandler {
    // 1. 找到对应的
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        method.invoke(proxy, args);
        return null;
    }
}
