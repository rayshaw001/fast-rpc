package com.rayshaw.message;

import java.util.Map;

public class FastRpcRequest implements Request{

    private String interfaceName;
    private String methodName;
    private String returnType;
    private Class[] paramsTypes;
    private Object[] paramsValues;

    public FastRpcRequest(String interfaceName, String methodName, String returnType, Class[] paramsTypes, Object[] paramsValues) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.paramsTypes = paramsTypes;
        this.paramsValues = paramsValues;
    }

    @Override
    public String getMethod() {
        return this.methodName;
    }

    @Override
    public Class[] getParamsTypes() {
        return this.paramsTypes;
    }

    @Override
    public Object[] getParamsValues() {
        return paramsValues;
    }

    @Override
    public String getReturnType() {
        return this.returnType;
    }

    @Override
    public String getInterfaceName(){
        return this.interfaceName;
    }
}
