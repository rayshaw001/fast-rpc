package com.rayshaw.message;

import java.lang.reflect.Type;
import java.util.Map;

public interface Request {
    String getMethod();

    Class[] getParamsTypes();

    Object[] getParamsValues();

    String getReturnType();

    String getInterfaceName();
}