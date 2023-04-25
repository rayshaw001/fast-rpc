package com.rayshaw.service;

import com.rayshaw.annotation.server.FastRpcService;

@FastRpcService
public class HelloWorldImpl implements SampleServiceInterface{

    @Override
    public String helloWorld() {
        return "hello world !!!";
    }

    @Override
    public String helloWorld(String hello) {
        return hello;
    }

    @Override
    public Integer helloWorld(String hello, String world) {
        return hello.hashCode() + world.hashCode();
    }
}
