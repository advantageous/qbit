package org.qbit.service.impl;

import org.boon.*;
import org.boon.collections.MultiMap;
import org.boon.core.Conversions;
import org.boon.core.Handler;
import org.boon.core.TypeType;
import org.boon.core.reflection.*;
import org.boon.primitive.Arry;
import org.qbit.GlobalConstants;
import org.qbit.bindings.ArgParamBinding;
import org.qbit.bindings.MethodBinding;
import org.qbit.bindings.RequestParamBinding;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.SendQueue;
import org.qbit.service.ServiceMethodHandler;
import org.qbit.service.method.impl.ResponseImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/8/14.
 */
public class ServiceMethodCallHandlerImpl implements ServiceMethodHandler {
    private ClassMeta<Class<?>> classMeta;
    private Object service;
    private MethodAccess queueEmpty;
    private MethodAccess queueLimit;
    private MethodAccess queueShutdown;
    private MethodAccess queueIdle;

    private String address = "";
    private TreeSet<String> addresses = new TreeSet<>();

    private Map<String, Pair<MethodBinding, MethodAccess>> methodMap
            = new LinkedHashMap<>();


    private Logger logger = Boon.logger(ServiceMethodCallHandlerImpl.class);


    private SendQueue<Response<Object>> responseSendQueue;


    @Override
    public Response<Object> receiveMethodCall(MethodCall<Object> methodCall) {

        if (GlobalConstants.DEBUG) {
            logger.info(Boon.className(this), "::receiveMethodCall",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }

        try {

            if (!Str.isEmpty(methodCall.name())) {
                return invokeByName(methodCall);
            } else {
                return invokeByAddress(methodCall);
            }
        } catch (Exception ex) {

            if (ex.getCause() instanceof InvocationTargetException) {
                InvocationTargetException tex = (InvocationTargetException) ex.getCause();
                return new ResponseImpl<>(methodCall, tex.getTargetException());
            }
            return new ResponseImpl<>(methodCall, ex);
        }
    }

    private Response<Object> invokeByAddress(MethodCall<Object> methodCall) {
        String address = methodCall.address();


        final Pair<MethodBinding, MethodAccess> binding = methodMap.get(address);
        if (binding != null) {
            return invokeByAddressWithSimpleBinding(methodCall, binding);
        } else {
            return invokeByAddressWithComplexBinding(methodCall);
        }
    }

    private Response<Object> invokeByAddressWithComplexBinding(MethodCall<Object> methodCall) {

        String mAddress = addresses.lower(methodCall.address());

        if (!methodCall.address().startsWith(mAddress)) {
            die("Method not found", methodCall);
        }

        final String[] split = StringScanner.split(methodCall.address(), '/');




        Pair<MethodBinding, MethodAccess> binding = methodMap.get(mAddress);

        final MethodBinding methodBinding = binding.getFirst();

        final MethodAccess methodAccess = binding.getSecond();
        final List<ArgParamBinding> parameters = methodBinding.parameters();
        final Class<?>[] parameterTypes = methodAccess.parameterTypes();
        final List<TypeType> paramEnumTypes = methodAccess.paramTypeEnumList();

        final List<Object> args = prepareArgumentList(methodCall, methodAccess.parameterTypes());


        final List<List<AnnotationData>> annotationDataForParams = methodAccess.annotationDataForParams();

        for (ArgParamBinding param : parameters) {
            final int uriPosition = param.getUriPosition();
            final int methodParamPosition = param.getMethodParamPosition();
            final String paramName = param.getMethodParamName();
            if (uriPosition != -1) {

                if (uriPosition > split.length) {
                    die("Parameter position is more than param length of method", methodAccess);
                } else {
                    String paramAtPos = split[uriPosition];
                    Object arg = Conversions.coerce(paramEnumTypes.get(methodParamPosition),
                            parameterTypes[methodParamPosition], paramAtPos
                    );
                    args.set(methodParamPosition, arg);
                }
            } else {
                if (Str.isEmpty(paramName)) {
                    die("Parameter name not supplied in URI path var");
                }

                for (int index = 0; index < parameterTypes.length; index++) {
                    final List<AnnotationData> paramsAnnotationData = annotationDataForParams.get(index);
                    String name = "";
                    for (AnnotationData paramAnnotation : paramsAnnotationData) {
                        if (paramAnnotation.getName().equalsIgnoreCase("name")
                                || paramAnnotation.getName().equalsIgnoreCase("PathVariable")) {
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


        Object returnValue =
                methodAccess.invokeDynamicObject(service, args);
        return response(methodAccess, methodCall, returnValue);


    }

    private Response<Object> invokeByAddressWithSimpleBinding(MethodCall<Object> methodCall, Pair<MethodBinding, MethodAccess> pair) {

        final MethodBinding binding = pair.getFirst();

        final MethodAccess method = pair.getSecond();


        if (binding.hasRequestParamBindings()) {

            Object body = bodyFromRequestParams(method, methodCall, binding);
            Object returnValue = method.invokeDynamicObject(service, body);
            return response(method, methodCall, returnValue);
        }

        return mapArgsAsyncHandlersAndInvoke(methodCall, method);


    }

    private Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> methodCall, MethodAccess method) {
        if (method.parameterTypes().length == 0) {

            Object returnValue = method.invokeDynamicObject(service, null);
            return response(method, methodCall, returnValue);

        }

        if (method.parameterTypes().length ==1) {


            Object body = methodCall.body();

            if (body==null || (body instanceof String && Str.isEmpty(body)) ) {
                if (method.parameterTypes()[0] != Handler.class) {
                    body = methodCall.params();
                    Object returnValue = method.invokeDynamicObject(service, body);
                    return response(method, methodCall, returnValue);
                }
            }

        }

        Object returnValue;


        Object body = methodCall.body();
        List<Object> argsList = prepareArgumentList(methodCall,
                method.parameterTypes());

        if (body instanceof List) {


            List<Object> list = (List) body;

            if (list.size() -1 == method.parameterTypes().length) {
                if (list.get(0) instanceof Handler) {
                    list = Lists.slc(list, 1);
                }
            }

            final Iterator<Object> iterator = list.iterator();

            for (int index = 0; index < argsList.size(); index++) {

                final Object o = argsList.get(index);
                if (o instanceof Handler) {
                    continue;
                }

                if (!iterator.hasNext()) {
                    break;
                }

                argsList.set(index, iterator.next());
            }
        } else if  (body instanceof Object[]) {

            Object [] args = (Object[]) body;


            if (args.length -1 == method.parameterTypes().length) {
                if (args[0] instanceof Handler) {
                    args = Arry.slc(args, 1);
                }
            }


            for (int index = 0; index < argsList.size(); index++) {

                final Object o = argsList.get(index);
                if (o instanceof Handler  ) {
                    continue;
                }

                argsList.set(index, args[index]);
            }
        } else {
            if (argsList.size() == 1 && !(argsList.get(0) instanceof Handler)) {
                argsList.set(0, body);
            }
        }

        returnValue =
                    method.invokeDynamicObject(service, argsList);

        return response(method, methodCall, returnValue);
    }

    private Response<Object> response(MethodAccess methodAccess,
                                      MethodCall<Object> methodCall, Object returnValue) {

        if (methodAccess.returnType() == Void.class) {
            return ServiceConstants.VOID;
        }
        return ResponseImpl.response(
                methodCall.id(),
                methodCall.timestamp(),
                methodCall.name(),
                methodCall.returnAddress(),
                returnValue);
    }

    private Object bodyFromRequestParams(MethodAccess method,
                                         MethodCall<Object> methodCall,
                                         MethodBinding binding) {
        final MultiMap<String, String> params = methodCall.params();

        final Class<?>[] parameterTypes = method.parameterTypes();

        List<Object> argsList = prepareArgumentList(methodCall, parameterTypes);

        boolean methodBodyUsed = false;

        for (int index = 0; index < parameterTypes.length; index++) {

            RequestParamBinding paramBinding = binding.requestParamBinding(index);
            if (paramBinding == null) {
                if (methodBodyUsed) {
                    die("Method body was already used for methodCall\n",
                            methodCall, "\nFor method binding\n", binding,
                            "\nFor method\n", method);
                }
                methodBodyUsed = true;
                if (methodCall.body() instanceof List) {
                    List bList = (List) methodCall.body();
                    if (bList.size() == 1) {
                        argsList.set(index, bList.get(0));
                    }
                } else {
                    argsList.set(index, methodCall.body());
                }
            } else {
                if (paramBinding.isRequired()) {
                    if (!params.containsKey(paramBinding.getName())) {
                        die("Method call missing required parameter",
                                "\nParam Name", paramBinding.getName(), "\nMethod Call",
                                methodCall, "\nFor method binding\n", binding,
                                "\nFor method\n", method);

                    }
                }
                final String name = paramBinding.getName();
                Object objectItem = params.getSingleObject(name);
                objectItem = Conversions.coerce(parameterTypes[index], objectItem);

                argsList.set(index, objectItem);
            }

        }


        return argsList;
    }

    private List<Object> prepareArgumentList(final MethodCall<Object> methodCall, Class<?>[] parameterTypes) {
        List<Object> argsList = new ArrayList<>(parameterTypes.length);

        for (int index = 0; index < parameterTypes.length; index++) {
            if (parameterTypes[index] == Handler.class) {

                argsList.add(createCallBackHandler(methodCall));
                continue;
            }
            argsList.add(null);
        }
        return argsList;
    }

    private Handler<Object> createCallBackHandler(final  MethodCall<Object> methodCall) {
        return new Handler<Object>() {
            @Override
            public void handle(Object returnValue) {
                responseSendQueue.send(
                        ResponseImpl.response(methodCall, returnValue)

                );
            }
        };
    }

    private Response<Object> invokeByName(MethodCall<Object> methodCall) {

        final MethodAccess method = classMeta.method(methodCall.name());


        return mapArgsAsyncHandlersAndInvoke(methodCall, method);


    }


    public void init(Object service, String rootAddress, String serviceAddress) {

        this.service = service;


        classMeta = (ClassMeta<Class<?>>) (Object) ClassMeta.classMeta(service.getClass());


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
            } else {
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

            registerMethod(methodAccess);
        }

        addresses.addAll(methodMap.keySet());





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


        final List<List<AnnotationData>> annotationDataForParams = methodAccess.annotationDataForParams();

        int index = 0;
        for (List<AnnotationData> annotationDataListForParam : annotationDataForParams) {
            for (AnnotationData annotationData : annotationDataListForParam) {
                if (annotationData.getName().equalsIgnoreCase("RequestParam")) {
                    String name = (String) annotationData.getValues().get("value");

                    boolean required = (Boolean) annotationData.getValues().get("required");

                    String defaultValue = (String) annotationData.getValues().get("defaultValue");
                    methodBinding.addRequestParamBinding(methodAccess.parameterTypes().length, index, name, required, defaultValue);
                } else if (annotationData.getName().equalsIgnoreCase("Name")) {
                    String name = (String) annotationData.getValues().get("value");
                    methodBinding.addRequestParamBinding(methodAccess.parameterTypes().length, index, name, false, null);

                }
            }
            index++;
        }

        this.methodMap.put(methodBinding.address(),
                new Pair<>(methodBinding, methodAccess));
    }


    @Override
    public TreeSet<String> addresses() {
        return addresses;
    }

    @Override
    public void initQueue(SendQueue<Response<Object>> responseSendQueue) {
        this.responseSendQueue = responseSendQueue;
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

        if (requestMapping != null) {
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
