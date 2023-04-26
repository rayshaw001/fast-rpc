package com.rayshaw.holder.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rayshaw.message.FastRpcRequest;
import com.rayshaw.message.Request;
import com.rayshaw.message.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FastRpcClientProxy implements InvocationHandler {
    // 1. 找到对应的method.转换为Request对象
    // 2. 使用netty 发送请求
    // 3. 转换结果
    private FastRpcClientHolder holder;
    FastRpcClientProxy(FastRpcClientHolder holder) {
        this.holder = holder;
    }

    private static final Gson GSON_TOOL = new GsonBuilder().create();
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = tranformToRequest(method, args);
        String requesString = GSON_TOOL.toJson(request);
        CompletableFuture<Response> result = holder.sendRequest(requesString);
//        return "null";
        Response response = result.get();
        return Class.forName(response.getResponseType()).cast(response.getResposeContent());

    }

    private Request tranformToRequest(Method method, Object[] args) {
        String interfaceName = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String returnType = method.getReturnType().getName();
        Object[] paramsValues = args;
        String[] paramsTypes = null;
        if(args!=null){
            paramsTypes = Arrays.stream(args).map( obj -> obj.getClass().getName()).collect(Collectors.toList()).toArray(new String[0]);
        }
        Request request = new FastRpcRequest(interfaceName, methodName, returnType, paramsTypes, paramsValues);
        return request;
    }
}
