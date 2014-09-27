package org.qbit.bindings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Richard on 7/22/14.
 */
public class HttpObjectBinding extends ObjectBinding {

    private String uri;
    private Map<String, MethodBinding> methodBindingMap = new HashMap<String, MethodBinding>();
    private HttpMethod httpMethod;

    public String uri() {
        return uri;
    }

    public HttpObjectBinding uri(String uri) {
        this.uri = uri;
        return this;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }

    public HttpObjectBinding httpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public HttpObjectBinding bind(MethodBinding... bindings) {

        for (MethodBinding binding : bindings) {
            methodBindingMap.put(binding.methodName(), binding);
        }
        return this;
    }

}
