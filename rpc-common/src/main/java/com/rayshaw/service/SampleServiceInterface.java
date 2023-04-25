package com.rayshaw.service;

import com.rayshaw.annotation.client.FastRpcClient;

@FastRpcClient
public interface SampleServiceInterface {
    String helloWorld();
    String helloWorld(String hello);
    Integer helloWorld(String hello, String world);
}
