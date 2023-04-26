package com.rayshaw.holder.server;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.ToNumberPolicy;
import com.rayshaw.message.FastRpcRequest;
import com.rayshaw.message.FastRpcResponse;
import com.rayshaw.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FastRcpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FastRcpServerHandler.class);

    private static final Gson GSON_TOOL = new GsonBuilder().serializeNulls().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

    private FastRpcServerHolder holder;


    public FastRcpServerHandler(FastRpcServerHolder holder) {
        this.holder = holder;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        long start = System.currentTimeMillis();

        FastRpcRequest request = GSON_TOOL.fromJson(o.toString().replace("\n\r", ""), FastRpcRequest.class);

        Class[] types = null;
        if(request.getParamsTypes() != null) {
            Arrays.stream(request.getParamsTypes()).map(typeString -> {
                try {
                    return Class.forName(typeString);
                } catch (ClassNotFoundException e) {
                    logger.error("conver type error", e);
                }
                return null;
            }).collect(Collectors.toList()).toArray(new Class[0]);
        }
        Object object = holder.invoke(request.getInterfaceName(), request.getMethod(), request.getParamsValues(), types);
        Response response = new FastRpcResponse("no msg", object, object.getClass().getName());
        String result = GSON_TOOL.toJson(response, response.getClass());
        ctx.writeAndFlush(result);

        long end = System.currentTimeMillis();
        logger.warn("request time:{} ms, url:{}", end - start, ctx);
    }
}
