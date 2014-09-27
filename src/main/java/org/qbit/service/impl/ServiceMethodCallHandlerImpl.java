package org.qbit.service.impl;

import org.boon.Lists;
import org.boon.Pair;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.Conversions;
import org.boon.core.TypeType;
import org.boon.core.reflection.*;
import org.qbit.bindings.ArgParamBinding;
import org.qbit.bindings.MethodBinding;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.service.ServiceMethodHandler;
import org.qbit.service.method.impl.ResponseImpl;

import java.util.*;

import static org.boon.Exceptions.die;

/**
* Created by Richard on 9/8/14.
*/
public class ServiceMethodCallHandlerImpl implements ServiceMethodHandler {
    private  ClassMeta<Class<?>> classMeta;
    private  Object service;
    private  MethodAccess queueEmpty;
    private  MethodAccess queueLimit;
    private  MethodAccess queueShutdown;
    private  MethodAccess queueIdle;

    private  String address="";
    private List<String> addresses = new ArrayList<>();

    //private List<String> roots = new ArrayList<>();

    private Map<String, Pair<MethodBinding, MethodAccess>> methodMap
            = new LinkedHashMap<>();


    @Override
    public Response<Object> receiveMethodCall(MethodCall<Object> methodCall) {


        if (!Str.isEmpty(methodCall.name())) {
            return invokeByName(methodCall);
        } else {
            return invokeByAddress(methodCall);
        }
    }

    private Response<Object> invokeByAddress(MethodCall<Object> methodCall) {
        String address = methodCall.address();



        final Pair<MethodBinding, MethodAccess> binding = methodMap.get(address);
        if (binding!=null) {
            return invokeByAddressWithSimpleBinding(methodCall, binding);
        } else {
            return invokeByAddressWithComplexBinding(methodCall);
        }
    }

    private Response<Object> invokeByAddressWithComplexBinding(MethodCall<Object> methodCall) {

        String mAddress = methodCall.address();

        final String[] split = StringScanner.split(mAddress, '/');



        for (String root : addresses) {
            if (mAddress.startsWith(root)) {
                mAddress = root;
                break;
            }
        }



        Pair<MethodBinding, MethodAccess> binding = methodMap.get(mAddress);

        final MethodBinding methodBinding = binding.getFirst();

        final MethodAccess methodAccess = binding.getSecond();
        final List<ArgParamBinding> parameters = methodBinding.parameters();
        final Class<?>[] parameterTypes = methodAccess.parameterTypes();
        final List<TypeType> paramEnumTypes = methodAccess.paramTypeEnumList();

        final List<Object> args = new ArrayList<>(parameterTypes.length);

        for (int index = 0; index < parameterTypes.length; index++) {
            args.add(null);
        }


        final List<List<AnnotationData>> annotationDataForParams = methodAccess.annotationDataForParams();

        for (ArgParamBinding param : parameters) {
            final int paramPosition = param.getMethodParamPosition();
            final String paramName = param.getMethodParamName();
            if (paramPosition!=-1) {

                if (paramPosition > parameterTypes.length) {
                    die("Parameter position is more than param length of method", methodAccess);
                } else {
                    Object arg = Conversions.coerce(paramEnumTypes.get(paramPosition),
                            parameterTypes[paramPosition],
                            split[paramPosition]);
                    args.set(paramPosition, arg);
                }
            } else {
                if (Str.isEmpty(paramName)) {
                    die("Parameter name not supplied in URI path var");
                }

                for ( int index = 0; index < parameterTypes.length; index++ ) {
                    final List<AnnotationData> paramsAnnotationData = annotationDataForParams.get(index);
                    String name = "";
                    for (AnnotationData paramAnnotation : paramsAnnotationData) {
                        if (paramAnnotation.getName().equals("name")) {
                            name = (String) paramAnnotation.getValues().get("value");
                            if (!Str.isEmpty(name)) {
                                break;
                            }
                        }
                    }
                    if (paramName.equals(name)) {

                        Object arg = Conversions.coerce(paramEnumTypes.get(index),
                                parameterTypes[index],
                                split[index]);
                        args.set(index, arg);
                    }
                }

            }
        }


        if (methodAccess.returnType() == Void.class) {

            methodAccess.invokeDynamicObject(service, args);
            return ServiceConstants.VOID;
        } else {
            Object returnValue =
                    methodAccess.invokeDynamicObject(service, args);
            Response<Object> response = ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), returnValue);

            return response;
        }


    }

    private Response<Object> invokeByAddressWithSimpleBinding(MethodCall<Object> methodCall, Pair<MethodBinding, MethodAccess> binding) {

        MethodAccess methodAccess = binding.getSecond();

        if (methodAccess.returnType() == Void.class) {

            methodAccess.invokeDynamicObject(service, methodCall.body());
            return ServiceConstants.VOID;
        } else {
            Object returnValue =
                    methodAccess.invokeDynamicObject(service, methodCall.body());
            Response<Object> response = ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), returnValue);

            return response;
        }

    }

    private Response<Object> invokeByName(MethodCall<Object> methodCall) {
        final MethodAccess m = classMeta.method(methodCall.name());
        if (m.returnType() == Void.class) {

            Invoker.invokeFromObject(service, methodCall.name(), methodCall.body());
            return ServiceConstants.VOID;
        } else {
            Object returnValue =
                    Invoker.invokeFromObject(service, methodCall.name(), methodCall.body());

            Response<Object> response = ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), returnValue);

            return response;
        }
    }


    public void init(Object service, String rootAddress, String serviceAddress) {

        this.service = service;


        classMeta = (ClassMeta<Class<?>>)(Object) ClassMeta.classMeta(service.getClass());


        if (Str.isEmpty(serviceAddress)) {
            serviceAddress = readAddressFromAnnotation(classMeta);
        }

        if (serviceAddress.endsWith("/")) {
            serviceAddress = Str.slc(serviceAddress, 0, -1);
        }


        if (!Str.isEmpty(rootAddress)) {

            if (rootAddress.endsWith("/")) {
                rootAddress = Str.slc(rootAddress, 0, -1);
            }

            if (serviceAddress.startsWith("/")) {
                this.address = Str.add(rootAddress, serviceAddress);
            }else {
                this.address = Str.add(rootAddress, "/", serviceAddress);

            }
        } else {
            this.address = serviceAddress;
        }


        readMethodMetaData();

        initQueueHandlerMethods();

    }

    private void initQueueHandlerMethods() {
        queueLimit = classMeta.method("queueLimit");
        queueEmpty = classMeta.method("queueEmpty");
        queueShutdown = classMeta.method("queueShutdown");
        queueIdle = classMeta.method("queueIdle");
    }

    private void readMethodMetaData() {



        final Iterable<MethodAccess> methods = classMeta.methods();
        for (MethodAccess methodAccess : methods) {

            if (!methodAccess.isPublic()) {
                continue;
            }

            registerMethod( methodAccess );
        }

        addresses = Lists.list(methodMap.keySet());

        //this.roots = new ArrayList<>(addresses.size());


        Collections.sort(addresses, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length() > o2.length()) {
                    return -1;
                } else if (o1.length() > o2.length()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });


//        if (!Str.isEmpty(address)) {
//            for (String addr : addresses) {
//                roots.add(StringScanner.substringAfter(addr, this.address));
//            }
//        } else {
//            roots = Lists.list(addresses);
//        }


    }

    private void registerMethod(MethodAccess methodAccess) {

        if (!methodAccess.hasAnnotation("RequestMapping")
                || !methodAccess.hasAnnotation("ServiceMethod")
                ) {


            String methodAddress = readAddressFromAnnotation(methodAccess);

            if (!Str.isEmpty(methodAddress)) {

                doRegisterMethodUnderURI(methodAccess, methodAddress);


            }

        }


        doRegisterMethodUnderURI(methodAccess, methodAccess.name());
        doRegisterMethodUnderURI(methodAccess,
                methodAccess.name().toLowerCase());
        doRegisterMethodUnderURI(methodAccess,
                methodAccess.name().toUpperCase());




    }

    private void doRegisterMethodUnderURI(MethodAccess methodAccess, String methodAddress) {

        if (methodAddress.startsWith("/")) {
            methodAddress = Str.slc(methodAddress, 1);
        }
        MethodBinding methodBinding =
                new MethodBinding(methodAccess.name(),
                        Str.join('/', address, methodAddress));

        this.methodMap.put(methodBinding.address(),
                new Pair<>(methodBinding, methodAccess));
    }


    @Override
    public List<String> addresses() {
        return addresses;
    }

    private String readAddressFromAnnotation(Annotated annotated) {
        String address = getAddress("RequestMapping", annotated);

        if (Str.isEmpty(address)) {
            address = getAddress("Name", annotated);
        }

        if (Str.isEmpty(address)) {
            address = getAddress("Service", annotated);
        }


        if (Str.isEmpty(address)) {
            address = getAddress("ServiceMethod", annotated);
        }

        return address == null ? "" : address;
    }

    private String getAddress(String name, Annotated annotated) {
        AnnotationData requestMapping = annotated.annotation(name);

        if (requestMapping!=null) {
            Object value = requestMapping.getValues().get("value");

            if (value instanceof String[]) {

                String[] values = (String[]) value;
                if (values.length > 0 && !Str.isEmpty(values[0])) {
                    return values[0];
                }
            } else {

                String sVal = (String) value;
                return sVal;
            }
        }
        return null;
    }


    @Override
    public String address() {
        return address;
    }


    @Override
    public void receive(MethodCall<Object> item) {

        die("Not supported");
    }

    @Override
    public void empty() {


        if (queueEmpty != null) {
            queueEmpty.invoke(service);
        }
    }

    @Override
    public void limit() {


        if (queueLimit != null) {
            queueLimit.invoke(service);
        }
    }

    @Override
    public void shutdown() {



        if (queueShutdown != null) {
            queueShutdown.invoke(service);
        }
    }

    @Override
    public void idle() {


        if (queueIdle != null) {
            queueIdle.invoke(service);
        }
    }

    public Map<String, Pair<MethodBinding, MethodAccess>> methodMap() {
        return methodMap;
    }
}
