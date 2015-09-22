package io.advantageous.qbit.message;

import io.advantageous.qbit.message.impl.ResponseImpl;

import java.util.Map;

/** Builds a response. */
public class ResponseBuilder {


    /** The request that originated the response. */
    private Request<Object> request;

    /** Address. */
    private String address;


    /** Return Address. */
    private String returnAddress;


    /** Params. */
    private Map<String, Object> params;
    private Object body;
    private long id;
    private long timestamp;
    private boolean errors;


    public static ResponseBuilder responseBuilder() {
        return new ResponseBuilder();
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    public Request<Object> getRequest() {
        return request;
    }

    public void setRequest(Request<Object> request) {
        this.request = request;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public static Response<Object> fromMethodCall(MethodCall<Object> methodCall, Object returnValue) {
        return new ResponseImpl<>(methodCall, returnValue);
    }
    public Response<Object> build() {
        return new ResponseImpl<>(getId(), getTimestamp(), getAddress(),
                getReturnAddress(), getParams(), getBody(), getRequest(),
                isErrors());
    }
}
