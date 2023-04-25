package com.rayshaw;

import com.rayshaw.holder.server.FastRpcServerHolder;
import com.rayshaw.annotation.server.FastRpcServer;

@FastRpcServer
public class FastRpcProviderApplication {
    public static void main(String[] args) throws Exception{

        FastRpcServerHolder server = new FastRpcServerHolder(FastRpcProviderApplication.class);
    }

}
