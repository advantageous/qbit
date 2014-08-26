package org.qbit.bindings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 7/22/14.
 */
public class MethodBinding {
    private String methodName;

    private String uri;

    private List<ArgParamBinding> parameters = new ArrayList<ArgParamBinding>();

    public static MethodBinding method(String methodName, String uri) {
        return new MethodBinding(methodName, uri);
    }

    public MethodBinding(String methodName, String uri) {
        this.methodName = methodName;
        this.uri = uri;
    }

    public String getMethodName() {
        return methodName;
    }
    public String getUri() {
        return uri;
    }

    public MethodBinding bind(ArgParamBinding... bindings) {

        int index=0;
        for (ArgParamBinding binding : bindings) {
            binding.position = index;
            parameters.add( binding );
            index++;
        }
        return this;
    }
}
