package io.advantageous.qbit.message;

import io.advantageous.qbit.message.impl.ResponseImpl;

import java.util.Map;

/**
 * Builds a response.
 */
public class ResponseBuilder {


    /**
     * The request that originated the response.
     */
    private Request<Object> request;

    /**
     * Address.
     */
    private String address;


    /**
     * Return Address.
     */
    private String returnAddress;


    /**
     * Params.
     */
    private Map<String, Object> params;
    private Object body;
    private long id;
    private long timestamp;
    private boolean errors;


    public static ResponseBuilder responseBuilder() {
        return new ResponseBuilder();
    }

    public static Response<Object> fromMethodCall(MethodCall<Object> methodCall, Object returnValue) {
        return new ResponseImpl<>(methodCall, returnValue);
    }

    public boolean isErrors() {
        return errors;
    }

    public ResponseBuilder setErrors(boolean errors) {
        this.errors = errors;
        return this;
    }

    public Request<Object> getRequest() {
        return request;
    }

    public ResponseBuilder setRequest(Request<Object> request) {

        this.request = request;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ResponseBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public ResponseBuilder setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public ResponseBuilder setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public ResponseBuilder setBody(Object body) {
        this.body = body;
        return this;
    }

    public long getId() {
        return id;
    }

    public ResponseBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ResponseBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Response<Object> build() {
        return new ResponseImpl<>(getId(), getTimestamp(), getAddress(),
                getReturnAddress(), getParams(), getBody(), getRequest(),
                isErrors());
    }
}
