package com.rayshaw.holder.server;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.rayshaw.message.FastRpcRequest;
import com.rayshaw.message.FastRpcResponse;
import com.rayshaw.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastRcpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FastRcpServerHandler.class);

    private static final Gson GSON_TOOL = new GsonBuilder().create();

    private FastRpcServerHolder holder;


    public FastRcpServerHandler(FastRpcServerHolder holder) {
        this.holder = holder;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        long start = System.currentTimeMillis();

        FastRpcRequest request = GSON_TOOL.fromJson(o.toString().replace("\n\r", ""), FastRpcRequest.class);
        Object object = holder.invoke(request.getInterfaceName(), request.getMethod(), request.getParamsValues(), request.getParamsTypes());
        Response response = new FastRpcResponse("no msg", object, object.getClass().getName());
        String result = GSON_TOOL.toJson(response, response.getClass());
        ctx.writeAndFlush(result);

        long end = System.currentTimeMillis();
        logger.warn("request time:{} ms, url:{}", end - start, ctx);
    }
}
