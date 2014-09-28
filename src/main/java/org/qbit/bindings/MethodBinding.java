package org.qbit.bindings;

import org.boon.Str;
import org.boon.StringScanner;
import org.boon.primitive.Arry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 7/22/14.
 */
public class MethodBinding {

    private final boolean hasURIParams;
    private final String methodName;

    private final String address;

    private final List<ArgParamBinding> parameters = new ArrayList<ArgParamBinding>();

    private final List<RequestParamBinding> requestParamBindings = new ArrayList<>();
    RequestParamBinding[] requestParamBindingsMap;

    public static MethodBinding method(String methodName, String uri) {
        return new MethodBinding(methodName, uri);
    }


    public MethodBinding(String methodName, String uri) {
        this.methodName = methodName;

        final String[] split = StringScanner.split(uri, '/');


        boolean found=false;
        int indexOfFirstParam = -1;

        int index = 0;
        for (String item : split) {
            if ( item.startsWith("{") && item.endsWith("}")) {

                if (indexOfFirstParam == -1) {
                    indexOfFirstParam = index;
                }

                found = true;

                item = Str.slc(item, 1, -1);
                ArgParamBinding binding;
                if (StringScanner.isDigits(item)) {
                    binding = new ArgParamBinding(Integer.parseInt(item), index, "");


                } else {

                    binding = new ArgParamBinding(-1, index, item);
                }
                parameters.add(binding);

            }
            index++;
        }

        if (indexOfFirstParam!=-1) {
            final String[] slc = Arry.slc(split, 0, indexOfFirstParam);

            this.address = Str.add(Str.join('/', slc), "/");
        }else {
            this.address = uri;

        }
        hasURIParams = found;
    }

    public String methodName() {
        return methodName;
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

    public List<RequestParamBinding> requestParamBindings() {

        return requestParamBindings;
    }

    public RequestParamBinding requestParamBinding(int index) {

        return requestParamBindingsMap[index];
    }

    public boolean hasRequestParamBindings() {
        return requestParamBindings.size() > 0;
    }
}
