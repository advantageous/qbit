package org.qbit.service.method.impl;

import org.boon.Boon;
import org.boon.Lists;
import org.boon.collections.MultiMap;
import org.boon.concurrent.Timer;
import org.boon.json.annotations.JsonIgnore;
import org.qbit.message.MethodCall;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 8/11/14.
 */
public class MethodCallImpl implements MethodCall<Object> {

    private String name="";
    private String address="";
    private MultiMap<String, String> params = new MultiMap<>();
    private Object body=Collections.emptyList();

    @JsonIgnore
    private static transient Timer timer = Timer.timer();

    private long timestamp;

    private long id;

    private static volatile long idSequence;
    private String objectName;
    private String returnAddress;


    public static MethodCall<Object> methodWithArgs(String method, Object... args) {
        return method(method, Lists.list(args));
    }

    public static MethodCall<Object> method(String name, Object body) {
        MethodCallImpl method = new MethodCallImpl();
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        return method;
    }

    public static MethodCall<Object> method(String name,
                                            String address,
                                            Object body) {
        MethodCallImpl method = new MethodCallImpl();
        method.address = address;
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        return method;
    }

    public static MethodCall<Object> method(String name,
                                            String address,
                                            MultiMap<String, String> params,
                                            Object body) {
        MethodCallImpl method = new MethodCallImpl();
        method.address = address;
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        method.params = params;
        return method;
    }


    public static MethodCall<Object> method(String name,
                                            String objectName,
                                            String address,
                                            String returnAddress,
                                            MultiMap<String, String> params,
                                            List<?> body) {
        MethodCallImpl method = new MethodCallImpl();
        method.address = address;
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        method.params = params;
        method.objectName = objectName;
        method.returnAddress = returnAddress;
        return method;
    }


    public static MethodCall<Object> method(String name, String body) {
        MethodCallImpl method = new MethodCallImpl();
        method.name = name;
        method.body = Collections.singletonList((Object)body);
        method.id = idSequence++;
        method.timestamp = timer.time();
        return method;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public long timestamp() {

        return timestamp;
    }

    @Override
    public String objectName() {
        return objectName;
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
    public String returnAddress() {
        return returnAddress;
    }

    @Override
    public MultiMap<String, String> params() {

        return this.params;
    }


    @Override
    public List<Object> body() {
        return (List)body;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean hasBody() {
        return body!=null && Boon.len(body)>0;
    }

    public boolean hasParams() {
        return params !=null && params.size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodCallImpl)) return false;

        MethodCallImpl method = (MethodCallImpl) o;

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

    public static MethodCall transformed(MethodCall methodCall, Object arg) {
        MethodCallImpl transformedMethod = new MethodCallImpl();
        transformedMethod.address = methodCall.address();

        if (arg instanceof List) {
            transformedMethod.body = (List)arg;
        } else {
            transformedMethod.body = Collections.singletonList(arg);
        }
        transformedMethod.params = methodCall.params();
        transformedMethod.name = methodCall.name();
        transformedMethod.timestamp = methodCall.timestamp();
        transformedMethod.id = methodCall.id();
        return transformedMethod;
    }
}
