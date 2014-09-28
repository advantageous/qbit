package org.qbit.service.method.impl;

import org.boon.Exceptions;
import org.boon.collections.LazyMap;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;

import java.util.Map;

/**
 * Created by Richard on 8/11/14.
 */
public class ResponseImpl<T> implements Response<T> {

    private final String address;

    private final String returnAddress;
    private final Map<String, Object> params;
    private final Object body;
    private final long id;
    private final long timestamp;
    private  Object transformedBody;
    private boolean errors;


    public static Response<Object> response(long id, long timestamp, String address, String returnAddress, Object body) {
        return new ResponseImpl(id, timestamp, address, returnAddress, null, body);
    }


    public ResponseImpl(MethodCall<Object> methodCall,
            Throwable ex) {

        this.returnAddress = methodCall.returnAddress();
        this.timestamp = methodCall.timestamp();
        this.id = methodCall.id();

        final LazyMap body = new LazyMap(10);
        this.body = body;
        this.address = methodCall.address();
        body.put("Error", ex.getMessage());
        body.put("Cause", "" + ex.getCause());
        body.put("Message", "Problem while calling method " + methodCall.name());

        if (ex instanceof  Exception) {
            body.put("Details",
               Exceptions.asMap((Exception) ex));
        }
        this.errors = true;
        this.params=null;

    }

    public ResponseImpl(long id, long timestamp, String address, String returnAddress, Map<String, Object> params, Object body) {
        this.address = address;
        this.params = params;
        this.body = body;
        this.id = id;
        this.timestamp = timestamp;
        this.returnAddress = returnAddress;
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

    @Override
    public boolean wasErrors() {
        return errors;
    }

    public void body(T newBody){

        transformedBody = newBody;

    }

    @Override
    public String returnAddress() {
        return returnAddress;
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
