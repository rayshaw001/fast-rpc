package com.rayshaw.message;

import java.util.Map;

public class FastRpcResponse implements Response{

    private String msgToLog;
    private Object responseContent;
    private String responseType;

    public FastRpcResponse(String msgToLog, Object responseContent, String responseType) {
        this.msgToLog = msgToLog;
        this.responseContent = responseContent;
        this.responseType = responseType;
    }

    @Override
    public String msgToLog() {
        return this.msgToLog;
    }

    @Override
    public Object getResposeContent() {
        return this.responseContent;
    }

    @Override
    public String getResponseType() {
        return this.responseType;
    }

    @Override
    public String toString() {
        return "FastRpcResponse{" +
                "msgToLog='" + msgToLog + '\'' +
                ", responseContent=" + responseContent +
                ", responseType='" + responseType + '\'' +
                '}';
    }
}
