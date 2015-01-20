package io.advantageous.qbit.bindings;

import java.util.ArrayList;
import java.util.List;

/**
 * This binds a method to an annotated method in a client.
 * <p>
 * Created by Richard on 7/22/14.
 */
public class MethodBinding {

    private final boolean hasURIParams;
    private final String methodName;
    private final String method;

    private final String address;

    private final List<ArgParamBinding> parameters = new ArrayList<>();

    private final List<RequestParamBinding> requestParamBindings = new ArrayList<>();

    private RequestParamBinding[] requestParamBindingsMap;

    public MethodBinding(String method, String methodName, String uri) {
        this.methodName = methodName;
        this.method = method;

        final String[] split = uri.split("/");

        boolean found = false;
        int indexOfFirstParam = -1;

        int index = 0;
        for (String item : split) {
            if (item.startsWith("{") && item.endsWith("}")) {
                if (indexOfFirstParam == -1) {
                    indexOfFirstParam = index;
                }
                found = true;
                item = item.substring(1, item.length() - 1);
                ArgParamBinding binding;
                if (item.matches("\\d+")) {
                    binding = new ArgParamBinding(Integer.parseInt(item), index, "");
                } else {
                    binding = new ArgParamBinding(-1, index, item);
                }
                parameters.add(binding);
            }
            index++;
        }

        if (indexOfFirstParam != -1) {
            final int end = uri.indexOf('{');
            this.address = uri.substring(0, end > -1 ? end : uri.length());
        } else {
            this.address = uri;

        }
        hasURIParams = found;
    }

    public String methodName() {
        return methodName;
    }

    public String method() {
        return method;
    }

    public String address() {
        return address;
    }

    public List<ArgParamBinding> parameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "MethodBinding{" +
                "hasURIParams=" + hasURIParams +
                ", methodName='" + methodName + '\'' +
                ", method='" + methodName + '\'' +
                ", address='" + address + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    public void addRequestParamBinding(int length, int index, String pathVarName,
                                       boolean required, String defaultValue) {

        if (requestParamBindingsMap == null) {
            requestParamBindingsMap = new RequestParamBinding[length];
        }
        RequestParamBinding paramBinding = new RequestParamBinding(pathVarName, index,
                required, defaultValue);

        requestParamBindings.add(paramBinding);

        requestParamBindingsMap[index] = paramBinding;
    }

    public RequestParamBinding requestParamBinding(int index) {

        return requestParamBindingsMap[index];
    }

    public boolean hasRequestParamBindings() {
        return requestParamBindings.size() > 0;
    }
}
