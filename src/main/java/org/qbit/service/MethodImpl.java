package org.qbit.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 8/11/14.
 */
public class MethodImpl implements Method {

    private String name;
    private String address;
    private Map<String, Object> params;
    private List<Object> body;

    private long id;

    private static volatile long idSequence;


    public static Method method(String name, String body) {
        MethodImpl method = new MethodImpl();
        method.name = name;
        method.body = Collections.singletonList((Object)body);
        method.id = idSequence++;
        return method;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public Map<String, Object> params() {
        return params;
    }

    @Override
    public List<Object> body() {
        return body;
    }
}
