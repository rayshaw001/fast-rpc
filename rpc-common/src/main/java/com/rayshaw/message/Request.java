package com.rayshaw.message;

public interface Request {
    String getMethod();

    String[] getParamsTypes();

    Object[] getParamsValues();

    String getReturnType();

    String getInterfaceName();
}