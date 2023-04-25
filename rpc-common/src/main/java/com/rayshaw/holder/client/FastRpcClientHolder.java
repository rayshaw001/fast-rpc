package com.rayshaw.holder.client;

import com.rayshaw.service.SampleServiceInterface;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;

public class FastRpcClientHolder {

    private static final Logger logger = LoggerFactory.getLogger(FastRpcClientHolder.class);
    private SampleServiceInterface sampleServiceInterface;

    private List<String> servers;

    public FastRpcClientHolder() {
        // 1. 加载zk 配置
        // 2. 对象代理
        // 3. 启动netty
    }

    private void loadFromZk(String zkPath) throws Exception{
        ZooKeeper zkClient = new ZooKeeper("localhost:9999", 20000, null);
        List<String> servers = zkClient.getChildren(zkPath, (watchedEvent) -> {
            if(watchedEvent.getType() == NodeChildrenChanged) {
                try {
                    loadFromZk(zkPath);
                } catch (Exception e) {
                    logger.error("log server error", e);
                }
            }
        });
        this.servers = servers;
    }

    @Test
    public void initInterface(){
        Class[] clazz = new Class[]{SampleServiceInterface.class};
        SampleServiceInterface sampleServiceInterface = (SampleServiceInterface)Proxy.newProxyInstance(FastRpcClientHolder.class.getClassLoader(), clazz, new FastRpcClientProxy());
        sampleServiceInterface.helloWorld();

    }

    private void startupNetty(){

    }
}
