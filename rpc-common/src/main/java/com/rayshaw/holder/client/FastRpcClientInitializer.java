package com.rayshaw.holder.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rayshaw.message.FastRpcResponse;
import com.rayshaw.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class FastRpcClientInitializer extends ChannelInitializer<SocketChannel> {


    private static Gson GSON_TOOL = new GsonBuilder().create();

    private static Logger logger = LoggerFactory.getLogger(FastRpcClientInitializer.class);

    private volatile Response result = null;

    private CountDownLatch latch = new CountDownLatch(1);

    public Response getResult() {
        try {
            latch.await();
        } finally {
            return result;
        }
    }

    public FastRpcClientInitializer(){

    }
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast(new FastRcpClientHandler());
    }

    class FastRcpClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
            try {
                FastRpcResponse response = GSON_TOOL.fromJson(o.toString(), FastRpcResponse.class);
                FastRpcClientInitializer.this.result = response;
                FastRpcClientInitializer.this.logger.info(response.toString());
            } finally {
                latch.countDown();
            }
        }
    }
}

