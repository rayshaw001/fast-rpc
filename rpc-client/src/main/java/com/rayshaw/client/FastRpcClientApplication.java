package com.rayshaw.client;

import com.rayshaw.annotation.client.FastRpcClient;
import com.rayshaw.holder.client.FastRpcClientHolder;
import com.rayshaw.service.SampleServiceInterface;

@FastRpcClient
public class FastRpcClientApplication {
    public static void main(String[] args) throws Exception{
        FastRpcClientHolder fastRpcClientHolder = new FastRpcClientHolder("/com.rayshaw.FastRpcProviderApplication", SampleServiceInterface.class);
        SampleServiceInterface sampleServiceInterface = (SampleServiceInterface)fastRpcClientHolder.getInterfaceProxy(SampleServiceInterface.class);
        String result = sampleServiceInterface.helloWorld("he");
        System.out.println(result);
//        fastRpcClientHolder.shutdown();
//        int a = sampleServiceInterface.helloWorld("1+1","=2");
//        System.out.println(a);

        result = sampleServiceInterface.helloWorld();
        System.out.println(result);

        fastRpcClientHolder.shutdown();
    }
}
