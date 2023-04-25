package com.rayshaw.holder.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rayshaw.message.FastRpcResponse;
import com.rayshaw.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastRcpClientHandler extends ChannelInboundHandlerAdapter {

    private static Gson GSON_TOOL = new GsonBuilder().create();

    private static Logger logger = LoggerFactory.getLogger(FastRcpClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        FastRpcResponse response = GSON_TOOL.fromJson(o.toString(), FastRpcResponse.class);
        logger.info(response.toString());
    }
}
