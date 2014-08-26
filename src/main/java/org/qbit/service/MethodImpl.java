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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodImpl)) return false;

        MethodImpl method = (MethodImpl) o;

        if (id != method.id) return false;
        if (address != null ? !address.equals(method.address) : method.address != null) return false;
        if (body != null ? !body.equals(method.body) : method.body != null) return false;
        if (name != null ? !name.equals(method.name) : method.name != null) return false;
        if (params != null ? !params.equals(method.params) : method.params != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}
