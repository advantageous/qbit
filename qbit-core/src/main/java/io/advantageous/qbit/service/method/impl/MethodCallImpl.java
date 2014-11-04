package io.advantageous.qbit.service.method.impl;

import io.advantageous.qbit.http.HttpRequest;
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

    @JsonIgnore
    private static transient Timer timer = Timer.timer();

    @JsonIgnore
    private static volatile long idSequence;

    private long timestamp;

    private long id;
    private String name = "";
    private String address = "";
    private MultiMap<String, String> params = MultiMap.empty();
    private MultiMap<String, String> headers = MultiMap.empty();
    private Object body = Collections.emptyList();
    private String objectName;
    private String returnAddress;
    private Request<Object> originatingRequest;

    public static MethodCall<Object> methodWithArgs(final String method,
                                                    final Object... args) {
        return method(method, Arrays.asList(args));
    }

    public static MethodCallImpl method(final long id,
                                        final String address,
                                        final String returnAddress,
                                        final String objectName,
                                        final String methodName,
                                        final long timestamp,
                                        final Object args,
                                        final MultiMap<String, String> params) {

        final MethodCallImpl method = new MethodCallImpl();
        method.returnAddress = returnAddress;
        method.address = address;
        method.name = methodName;
        method.objectName = objectName;
        method.params = params;
        method.body = args;

        if (id == 0L) {
            method.id = idSequence++;
        } else {
            method.id = id;
        }

        if (timestamp == 0L) {
            method.timestamp = timer.time();
        } else {
            method.timestamp = timestamp;
        }
        return method;
    }

    public static MethodCall<Object> method(final String name,
                                            final Object body) {
        final MethodCallImpl method = new MethodCallImpl();
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        return method;
    }

    public static MethodCall<Object> method(final String name,
                                            final String address,
                                            final Object body) {
        final MethodCallImpl method = new MethodCallImpl();
        method.address = address;
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        return method;
    }

    public static MethodCall<Object> method(final String name,
                                            final String address,
                                            final MultiMap<String, String> params,
                                            final Object body) {
        final MethodCallImpl method = new MethodCallImpl();
        method.address = address;
        method.name = name;
        method.body = body;
        method.id = idSequence++;
        method.timestamp = timer.time();
        method.params = params;
        return method;
    }

    public static MethodCall<Object> method(final String name,
                                            final String objectName,
                                            final String address,
                                            final String returnAddress,
                                            final MultiMap<String, String> params,
                                            final List<?> body) {
        final MethodCallImpl method = new MethodCallImpl();
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

    public static MethodCall<Object> method(final String name,
                                            final String body) {
        final MethodCallImpl method = new MethodCallImpl();
        method.name = name;
        method.body = Collections.singletonList((Object) body);
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
        return body;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean hasParams() {
        return params != null && params.size() > 0;
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

    public static MethodCall<Object> transformed(final MethodCall<Object> methodCall,
                                                 final Object arg) {
        final MethodCallImpl transformedMethod = new MethodCallImpl();
        transformedMethod.address = methodCall.address();
        transformedMethod.returnAddress = methodCall.returnAddress();
        transformedMethod.body = arg;
        transformedMethod.params = methodCall.params();
        transformedMethod.name = methodCall.name();
        transformedMethod.timestamp = methodCall.timestamp();
        transformedMethod.id = methodCall.id();
        return transformedMethod;
    }

    public void overridesFromParams() {
        if (params != null && params.size() > 0) {
            final String _addr = params.get(Protocol.ADDRESS_KEY);
            final String _objectName = params.get(Protocol.OBJECT_NAME_KEY);
            final String _methodName = params.get(Protocol.METHOD_NAME_KEY);
            final String _returnAddress = params.get(Protocol.RETURN_ADDRESS_KEY);
            this.address = _addr == null || _addr.isEmpty() ? address : _addr;
            this.returnAddress = _returnAddress == null || _returnAddress.isEmpty() ? returnAddress : _returnAddress;
            this.name = _methodName == null || _methodName.isEmpty() ? name : _methodName;
            this.objectName = _objectName == null || _objectName.isEmpty() ? objectName : _objectName;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public MethodCallImpl overrides(final MethodCallImpl methodCall) {
        if (methodCall.hasParams()) {
            if (!this.hasParams()) {
                this.params = methodCall.params;
            } else {
                this.params.putAll(methodCall.params);
            }
        }
        overridesFromParams();
        this.address = methodCall.address == null || methodCall.address.isEmpty() ? address : methodCall.address;
        this.returnAddress = methodCall.returnAddress == null || methodCall.returnAddress.isEmpty()
                ? returnAddress : methodCall.returnAddress;
        this.name = methodCall.name == null || methodCall.name.isEmpty() ? name : methodCall.name;
        this.objectName = methodCall.objectName == null || methodCall.objectName.isEmpty()
                ? objectName : methodCall.objectName;
        this.id = methodCall.id == 0L ? id : methodCall.id;
        this.timestamp = methodCall.timestamp == 0 ? timestamp : methodCall.timestamp;
        if (timestamp == 0) {
            timestamp = timer.now();
        }
        if (id == 0) {
            id = idSequence++;
        }
        return this;
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

    public void params(MultiMap<String, String> params) {
        this.params = params;
    }

    public void headers(MultiMap<String, String> headers) {
        this.headers = headers;
    }


    public void setBody(Object[] body) {
        this.body = body;
    }

    public static MethodCallImpl method(final Request<Object> request, final Object args) {
        MethodCallImpl methodCall =   method(request.id(), request.address(), request.returnAddress(), null, null, request.timestamp(), request.body(), request.params());
        methodCall.originatingRequest = request;
        methodCall.body = args;
        return methodCall;
    }
}
