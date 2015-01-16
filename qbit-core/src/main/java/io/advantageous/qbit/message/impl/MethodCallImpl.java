package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.annotation.JsonIgnore;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.Protocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is runs a method call for an RPC call.
 * <p>
 * Created by Richard on 8/11/14.
 */
public class MethodCallImpl implements MethodCall<Object> {


    private final long timestamp;
    private final long id;
    private final String name;
    private final String address;
    private final MultiMap<String, String> params;
    private final MultiMap<String, String> headers;
    private final Object body;
    private  Object transformedBody;

    private final String objectName;
    private final String returnAddress;
    private Request<Object> originatingRequest;


    public MethodCallImpl(long timestamp, long id, String name, String address, MultiMap<String, String> params, MultiMap<String, String> headers, Object body, String objectName, String returnAddress, Request<Object> originatingRequest) {
        this.timestamp = timestamp;
        this.id = id;
        this.name = name;
        this.address = address;
        this.params = params;
        this.headers = headers;
        this.body = body;
        this.objectName = objectName;
        this.returnAddress = returnAddress;
        this.originatingRequest = originatingRequest;
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
    public boolean isHandled() {
        return false;
    }

    @Override
    public void handled() {

    }

    @Override
    public String objectName() {
        return objectName;
    }

    @Override
    public Request<Object> originatingRequest() {
        return originatingRequest;
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
    public MultiMap<String, String> headers() {
        return this.headers;
    }

    @Override
    public Object body() {
        return transformedBody == null ? body : transformedBody;
    }



    public void setBody(Object[] body) {

        this.transformedBody = body;
    }


    public void originatingRequest(Request<Object> originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean hasParams() {
        return params != null && params.size() > 0;
    }

    @Override
    public boolean hasHeaders() {

        return headers != null && headers.size() > 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodCallImpl)) return false;
        final MethodCallImpl method = (MethodCallImpl) o;
        return id == method.id && !(address != null ? !address.equals(method.address) : method.address != null)
                && !(body != null ? !body.equals(method.body) : method.body != null)
                && !(name != null ? !name.equals(method.name) : method.name != null)
                && !(params != null ? !params.equals(method.params) : method.params != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MethodCallImpl{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", params=" + params +
                ", body=" + body +
                ", timestamp=" + timestamp +
                ", id=" + id +
                ", objectName='" + objectName + '\'' +
                ", returnAddress='" + returnAddress + '\'' +
                '}';
    }


}
