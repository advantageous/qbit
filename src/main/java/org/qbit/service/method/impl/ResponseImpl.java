package org.qbit.service.method.impl;

import org.qbit.message.Response;

import java.util.Map;

/**
 * Created by Richard on 8/11/14.
 */
public class ResponseImpl<T> implements Response<T> {

    private final String address;
    private final Map<String, Object> params;
    private final Object body;
    private final long id;
    private final long timestamp;
    private  Object transformedBody;


    public static Response response(long id, Object body) {
        return new ResponseImpl(id, 0, null, null, body);
    }


    public static Response response(long id, long timestamp, String address, Object body) {
        return new ResponseImpl(id, timestamp, address, null, body);
    }

    public ResponseImpl(long id, long timestamp, String address, Map<String, Object> params, Object body) {
        this.address = address;
        this.params = params;
        this.body = body;
        this.id = id;
        this.timestamp = timestamp;
    }


    @Override
    public long id() {
        return id;
    }


    @Override
    public T body() {

        if (transformedBody==null) {
            return (T) body;
        } else {
            return (T) transformedBody;
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void body(T newBody){

        transformedBody = newBody;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseImpl)) return false;

        ResponseImpl response = (ResponseImpl) o;

        if (address != null ? !address.equals(response.address) : response.address != null) return false;
        if (body != null ? !body.equals(response.body) : response.body != null) return false;
        if (params != null ? !params.equals(response.params) : response.params != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResponseImpl{" +
                "address='" + address + '\'' +
                ", params=" + params +
                ", body=" + body +
                '}';
    }
}
