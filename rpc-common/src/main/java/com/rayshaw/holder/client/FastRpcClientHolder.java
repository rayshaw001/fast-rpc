package com.rayshaw.holder.client;

import com.rayshaw.message.Response;
import com.rayshaw.service.SampleServiceInterface;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;

public class FastRpcClientHolder {

    private static final Logger logger = LoggerFactory.getLogger(FastRpcClientHolder.class);
    private SampleServiceInterface sampleServiceInterface;

    // interface mapping proxyObject
    private Map<String, Object> cachedProxy = new HashMap<>();

    private List<String> servers;

    private List<Bootstrap> clientBootStraps;

    private Lock resultLock = new ReentrantLock();

//    public FastRpcClientHolder(){}
    public FastRpcClientHolder(String zkPath, Class... clazzz) throws Exception {
//        // 1. 加载zk 配置
        loadFromZk(zkPath);
//        // 2. 创建代理对象
        initInterface(clazzz);
//        // 3. 启动netty
        startupNetty();
//    }
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

//    public void initInterface(String packageToScan){
    private void initInterface(Class[] clazzz){
//        Reflections entryPkg = new Reflections(packageToScan);
//        Reflections entryPkg = new Reflections("com.rayshaw.service");
//        Reflection reflection = new Reflection()
//        Class[] clazzz = entryPkg.getTypesAnnotatedWith(FastRpcClient.class).toArray(new Class[0]);
        for(Class clazz:clazzz) {
            Object interfaceProxy = Proxy.newProxyInstance(FastRpcClientHolder.class.getClassLoader(), new Class[]{clazz}, new FastRpcClientProxy(this));
            cachedProxy.put(clazz.getName(), interfaceProxy);
        }

    }

    public Object getInterfaceProxy(Class interfaceClazz) {
        return cachedProxy.get(interfaceClazz.getName());
    }

    private void startupNetty() throws Exception{
        List<Bootstrap> clientBootStraps = new ArrayList<>();

        for(String ipPort:servers) {
            NioEventLoopGroup group = new NioEventLoopGroup();
            String[] tmp = ipPort.split(":");
            int port = Integer.valueOf(tmp[1]);
            String ip = tmp[0];
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            //绑定远程服务地址与端口
            bootstrap.remoteAddress(ip, port);

            clientBootStraps.add(bootstrap);
        }

        this.clientBootStraps = clientBootStraps;
    }

    public CompletableFuture<Response> sendRequest(String requestString) throws Exception {
        final FastRpcClientInitializer fastRpcClientInitializer = new FastRpcClientInitializer();

        CompletableFuture.runAsync(() -> {
            try {
                Random random = new Random();
                Bootstrap bs = this.clientBootStraps.get(random.nextInt(1));
                bs.handler(fastRpcClientInitializer);
                ChannelFuture future = bs.connect().sync();
                Channel ch = future.channel();
                ChannelFuture f = ch.writeAndFlush(requestString);
            } catch (Exception e) {
                logger.error("error:",e);
            } finally {

            }
        });
//        f.sync();
//        f.get();
        CompletableFuture<Response> future1 = CompletableFuture.supplyAsync(() -> fastRpcClientInitializer.getResult());

        return future1;
        // to do ...
//        return "result";
    }

    private void shutdown() {
        for(Bootstrap bs: this.clientBootStraps) {
            bs.group().shutdownGracefully();
        }
    }
}
