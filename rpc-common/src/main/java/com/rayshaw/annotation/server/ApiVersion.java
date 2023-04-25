package com.rayshaw.annotation.server;

public @interface ApiVersion {
    String value() default "v1";
}
