package com.rayshaw.holder.server;

import com.rayshaw.annotation.server.FastRpcServer;
import com.rayshaw.annotation.server.FastRpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class FastRpcServerHolder {

    private static final Logger logger = LoggerFactory.getLogger(FastRpcServerHolder.class);
    //1. 扫描basepackage
    //2. cache interface method mapping relationship
    // interface name -> Object
    Map<String, Object> interfaceObjectMap = new HashMap<>();
    // full methodName with parameters mapping objects
    Map<String, Object> methodWithParametersObjectMap = new HashMap<>();
    //x-1. 启动netty 服务
    //x. 注册到zk上
    public FastRpcServerHolder(Class clazz) throws Exception{

        initMethodCache(clazz.getPackage().getName());

        InputStream stream = FastRpcServerHolder.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(stream);
        int port = Integer.valueOf(properties.getProperty("server.port","9999"));
        String zkPath = properties.getProperty("zk.path", clazz.getName());
        if(!zkPath.startsWith("/")) {
            zkPath = "/" + zkPath;
        }

        initNettyServerAndRegisterToZk(port, zkPath);

    }

    private void initMethodCache(String packageToScan) throws Exception{
        Reflections entryPkg = new Reflections(packageToScan);

        Set clazz = entryPkg.getTypesAnnotatedWith(FastRpcServer.class);
        if(clazz.size() == 0) {
            throw new Exception("no FastRpcServer marked");
        }
        Set<Class<?>> items = entryPkg.getTypesAnnotatedWith(FastRpcService.class);
        for(Class<?> item: items) {
            String superClassName = item.getInterfaces()[0].getName();
            Object realObject = interfaceObjectMap.get(superClassName);
            if(realObject == null) {
                realObject = item.getConstructor().newInstance();
                interfaceObjectMap.put(superClassName, realObject);
            }
            for(Method m:item.getDeclaredMethods()) {
                StringBuilder methodName = new StringBuilder();
                methodName.append(superClassName).append("$").append(m.getName());
                for(Class type:m.getParameterTypes()){
                    methodName.append("$").append(type.getName());
                }
                methodWithParametersObjectMap.put(methodName.toString(), realObject);
            }
        }
    }

    private void initNettyServerAndRegisterToZk(int serverPort, String zkPath) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new FastRpcServerInitializer(this));
            Channel ch = b.bind(serverPort).sync().channel();
            registerToZk(zkPath);
            ch.closeFuture().sync();
        } catch (Exception e){
            logger.error("zk register error: ", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void registerToZk(String zkPath) throws Exception{
        ZooKeeper zkClient = new ZooKeeper("10.221.128.100:9999", 20000, null);
        InputStream stream = FastRpcServerHolder.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(stream);
        String port = properties.getProperty("server.port","9999");
        String ip = getLocalHostIp();
        String toRegisterPath = zkPath + "/" + ip+":"+port;
        ACL acl = new ACL();
        List<ACL> acls = new ArrayList<>();
        acls.add(acl);
        zkClient.create(toRegisterPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    if(zkClient.exists(toRegisterPath, null) != null) {
                        zkClient.delete(toRegisterPath, 0);
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String getLocalHostIp() {
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    String hostAddress = addr.getHostAddress();
                    if (addr.isSiteLocalAddress()
                            && !addr.isLoopbackAddress()
                            && !(addr instanceof Inet6Address)
                            && !hostAddress.equals("172.17.42.1")) {
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
        }
        throw new IllegalStateException("Couldn't find the local machine ip.");
    }


    public Object invoke(String interfaceName,String methodName, Object[] args, Class[] argTypes) throws Exception{
        String cacheKey = interfaceName + "$" + methodName;
        if(argTypes != null) {
            for (Class clazz : argTypes) {
                cacheKey += "$" + clazz.getName();
            }
        }
        Object instance = methodWithParametersObjectMap.get(cacheKey);
        Method method = instance.getClass().getMethod(methodName, argTypes);
        return method.invoke(instance, args);
    }

}
