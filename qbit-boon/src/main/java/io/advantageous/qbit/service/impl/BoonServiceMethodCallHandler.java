/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.bindings.ArgParamBinding;
import io.advantageous.qbit.bindings.MethodBinding;
import io.advantageous.qbit.bindings.RequestParamBinding;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceMethodHandler;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Lists;
import org.boon.Pair;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.Conversions;
import org.boon.core.TypeType;
import org.boon.core.reflection.Annotated;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;
import org.boon.primitive.Arry;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.qbit.annotation.AnnotationUtils.getListenAnnotation;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/8/14.
 *
 * @author Rick Hightower
 */
public class BoonServiceMethodCallHandler implements ServiceMethodHandler {
    private final boolean invokeDynamic;
    private ClassMeta<Class<?>> classMeta;
    private Object service;
    private QueueCallBackHandler queueCallBackHandler;
    private String address = "";

    private String name = "";
    private TreeSet<String> addresses = new TreeSet<>();

    private Map<String, Map<String, Pair<MethodBinding, MethodAccess>>> methodMap = new LinkedHashMap<>();

    private SendQueue<Response<Object>> responseSendQueue;
    private Map<String, MethodAccess> eventMap = new ConcurrentHashMap<>();

    public BoonServiceMethodCallHandler(final boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
    }

    @Override
    public Response<Object> receiveMethodCall(MethodCall<Object> methodCall) {

        try {
            if (methodCall.name() != null && !methodCall.name().isEmpty()) {
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


        final Map<String, Pair<MethodBinding, MethodAccess>> mappings = methodMap.get(address);


        final Request<Object> request = methodCall.originatingRequest();

        Pair<MethodBinding, MethodAccess> binding = null;

        if (mappings != null && request instanceof HttpRequest) {
            HttpRequest httpRequest = ((HttpRequest) request);
            final String method = httpRequest.getMethod();
            binding = mappings.get(method);
        }

        if (mappings != null && mappings.size() == 1 && binding == null) {
            binding = mappings.values().iterator().next();
        }


        if (binding != null) {
            return invokeByAddressWithSimpleBinding(methodCall, binding);
        } else {
            return invokeByAddressWithComplexBinding(methodCall);
        }
    }

    private Response<Object> invokeByAddressWithComplexBinding(MethodCall<Object> methodCall) {
        String mAddress = addresses.lower(methodCall.address());

        final Map<String, Pair<MethodBinding, MethodAccess>> mappings = methodMap.get(mAddress);

        if (!methodCall.address().startsWith(mAddress)) {
            throw new IllegalArgumentException("Method not found: " + methodCall);
        }


        final Request<Object> request = methodCall.originatingRequest();

        Pair<MethodBinding, MethodAccess> binding = null;

        if (request instanceof HttpRequest) {
            HttpRequest httpRequest = ((HttpRequest) request);
            final String method = httpRequest.getMethod();
            binding = mappings.get(method);
        } else if (mappings != null && mappings.size() == 1) {
            binding = mappings.values().iterator().next();
        }


        final String[] split = StringScanner.split(methodCall.address(), '/');


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
                    Object arg = Conversions.coerce(paramEnumTypes.get(methodParamPosition), parameterTypes[methodParamPosition], paramAtPos);
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
                        if (paramAnnotation.getName().equalsIgnoreCase("name") || paramAnnotation.getName().equalsIgnoreCase("PathVariable")) {
                            name = (String) paramAnnotation.getValues().get("value");
                            if (!Str.isEmpty(name)) {
                                break;
                            }
                        }
                    }
                    if (paramName.equals(name)) {

                        Object arg = Conversions.coerce(paramEnumTypes.get(index), parameterTypes[index], split[index]);
                        args.set(index, arg);
                    }
                }

            }
        }


        Object returnValue = methodAccess.invokeDynamicObject(service, args);
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

        if (method.parameterTypes().length == 1) {


            Object body = methodCall.body();

            if (body == null || (body instanceof String && Str.isEmpty(body))) {
                if (method.parameterTypes()[0] != Callback.class) {
                    body = methodCall.params();
                    Object returnValue = method.invokeDynamicObject(service, body);
                    return response(method, methodCall, returnValue);
                }
            }

        }

        boolean hasHandlers = hasHandlers(methodCall);

        hasHandlers = hasHandlers(method) || hasHandlers;

        Object returnValue;


        if (hasHandlers) {
            Object body = methodCall.body();
            List<Object> argsList = prepareArgumentList(methodCall, method.parameterTypes());


            if (body instanceof List || body instanceof Object[]) {


                extactHandlersFromArgumentList(method, body, argsList);

            } else {
                if (argsList.size() == 1 && !(argsList.get(0) instanceof Callback)) {
                    argsList.set(0, body);
                }
            }


            if (invokeDynamic) {
                returnValue = method.invokeDynamicObject(service, argsList);
            } else {
                returnValue = method.invoke(service, argsList.toArray(new Object[argsList.size()]));
            }

        } else {

            if (invokeDynamic) {

                if (methodCall.body() instanceof List) {
                    final List argsList = (List) methodCall.body();
                    returnValue = method.invokeDynamic(service, argsList.toArray(new Object[argsList.size()]));
                } else if (methodCall.body() instanceof Object[]) {
                    final Object[] argsList = (Object[]) methodCall.body();
                    returnValue = method.invokeDynamic(service, argsList);
                } else {
                    returnValue = method.invokeDynamic(service, methodCall.body());
                }
            } else {
                if (methodCall.body() instanceof List) {
                    final List argsList = (List) methodCall.body();
                    returnValue = method.invoke(service, argsList.toArray(new Object[argsList.size()]));
                } else if (methodCall.body() instanceof Object[]) {
                    final Object[] argsList = (Object[]) methodCall.body();
                    returnValue = method.invoke(service, argsList);
                } else {
                    returnValue = method.invoke(service, methodCall.body());
                }
            }
        }


        return response(method, methodCall, returnValue);
    }


    private boolean hasHandlers(MethodAccess method) {

        for (Class<?> paramType : method.parameterTypes()) {
            if (paramType == Callback.class) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHandlers(MethodCall<Object> methodCall) {
        if (methodCall.body() instanceof List) {

            final List body = (List) methodCall.body();
            for (Object item : body) {
                if (item instanceof Callback) {
                    return true;
                }
            }
            return false;
        } else if (methodCall.body() instanceof Object[]) {

            final Object[] body = (Object[]) methodCall.body();
            for (Object item : body) {
                if (item instanceof Callback) {
                    return true;
                }
            }
            return false;

        } else {
            return methodCall.body() instanceof Callback;
        }
    }

    private void extactHandlersFromArgumentList(MethodAccess method, Object body, List<Object> argsList) {
        if (body instanceof List) {

            List<Object> list = (List<Object>) body;

            extractHandlersFromArgumentListBodyIsList(method, argsList, list);

        } else if (body instanceof Object[]) {
            extactHandlersFromArgumentListArrayCase(method, (Object[]) body, argsList);
        }
    }

    private void extactHandlersFromArgumentListArrayCase(MethodAccess method, Object[] array, List<Object> argsList) {

        if (array.length - 1 == method.parameterTypes().length) {
            if (array[0] instanceof Callback) {
                array = Arry.slc(array, 1);
            }
        }


        for (int index = 0, arrayIndex = 0; index < argsList.size(); index++) {

            final Object o = argsList.get(index);
            if (o instanceof Callback) {
                continue;
            }


            if (arrayIndex >= array.length) {
                break;
            }


            argsList.set(index, array[arrayIndex]);
            arrayIndex++;

        }
    }


    private void extactHandlersFromArgumentListArrayCaseOld(MethodAccess method, Object[] array, List<Object> argsList) {

        if (array.length - 1 == method.parameterTypes().length) {
            if (array[0] instanceof Callback) {
                array = Arry.slc(array, 1);
            }
        }


        final Object[] theArray = array;
        final Iterator<Object> iterator = new Iterator<Object>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                if (index >= theArray.length) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public Object next() {

                Object o = theArray[index];
                index++;
                return o;

            }
        };

        for (int index = 0; index < argsList.size(); index++) {

            final Object o = argsList.get(index);
            if (o instanceof Callback) {
                continue;
            }

            if (!iterator.hasNext()) {
                break;
            }

            argsList.set(index, iterator.next());
        }
    }

    private void extractHandlersFromArgumentListBodyIsList(MethodAccess method, List<Object> argsList, List<Object> list) {
        if (list.size() - 1 == method.parameterTypes().length) {
            if (list.get(0) instanceof Callback) {
                list = Lists.slc(list, 1);
            }
        }

        final Iterator<Object> iterator = list.iterator();

        for (int index = 0; index < argsList.size(); index++) {

            final Object o = argsList.get(index);
            if (o instanceof Callback) {
                continue;
            }

            if (!iterator.hasNext()) {
                break;
            }

            argsList.set(index, iterator.next());
        }
    }

    private Response<Object> response(MethodAccess methodAccess, MethodCall<Object> methodCall, Object returnValue) {

        if (methodAccess.returnType() == void.class || methodAccess.returnType() == Void.class) {
            return ServiceConstants.VOID;
        }
        return ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), methodCall.returnAddress(), returnValue, methodCall);
    }

    private Object bodyFromRequestParams(MethodAccess method, MethodCall<Object> methodCall, MethodBinding binding) {
        final MultiMap<String, String> params = methodCall.params();

        final Class<?>[] parameterTypes = method.parameterTypes();

        List<Object> argsList = prepareArgumentList(methodCall, parameterTypes);

        boolean methodBodyUsed = false;

        for (int index = 0; index < parameterTypes.length; index++) {

            RequestParamBinding paramBinding = binding.requestParamBinding(index);
            if (paramBinding == null) {
                if (methodBodyUsed) {
                    die("Method body was already used for methodCall\n", methodCall, "\nFor method binding\n", binding, "\nFor method\n", method);
                }
                methodBodyUsed = true;
                if (methodCall.body() instanceof List) {
                    List bList = (List) methodCall.body();
                    if (bList.size() == 1) {
                        argsList.set(index, bList.get(0));
                    }
                } else if (methodCall.body() instanceof Object[]) {

                    Object[] bList = (Object[]) methodCall.body();
                    if (bList.length == 1) {

                        argsList.set(index, bList[0]);
                    }
                } else {
                    argsList.set(index, methodCall.body());
                }
            } else {
                if (paramBinding.isRequired()) {
                    if (!params.containsKey(paramBinding.getName())) {
                        die("Method call missing required parameter", "\nParam Name", paramBinding.getName(), "\nMethod Call", methodCall, "\nFor method binding\n", binding, "\nFor method\n", method);

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
        final List<Object> argsList = new ArrayList<>(parameterTypes.length);
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType == Callback.class) {
                argsList.add(createCallBackHandler(methodCall));
                continue;
            }
            argsList.add(null);
        }
        return argsList;
    }

    private Callback<Object> createCallBackHandler(final MethodCall<Object> methodCall) {

        return new BoonCallBackWrapper(responseSendQueue, methodCall);

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
        if (Str.isEmpty(serviceAddress)) {

            serviceAddress = Str.camelCaseLower(classMeta.name());
        }

        this.name = readNameFromAnnotation(classMeta);

        if (Str.isEmpty(name)) {

            this.name = Str.uncapitalize(classMeta.name());

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


        final Iterable<MethodAccess> methods = classMeta.methods();

        for (MethodAccess methodAccess : methods) {
            final AnnotationData listen = getListenAnnotation(methodAccess);
            if (listen == null) {
                continue;
            }
            String channel = listen.getValues().get("value").toString();
            eventMap.put(channel, methodAccess);
        }

        readMethodMetaData();

        initQueueHandlerMethods();

    }

    private void initQueueHandlerMethods() {

        this.queueCallBackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(service);

    }

    public void queueStartBatch() {
        queueCallBackHandler.queueStartBatch();
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

        if (!methodAccess.hasAnnotation("RequestMapping") || !methodAccess.hasAnnotation("ServiceMethod")) {


            String methodAddress = readAddressFromAnnotation(methodAccess);

            String httpMethod = readHttpMethod(methodAccess);

            if (methodAddress != null && !methodAddress.isEmpty()) {

                doRegisterMethodUnderURI(httpMethod, methodAccess, methodAddress);


            }

        }


        doRegisterMethodUnderURI("GET", methodAccess, methodAccess.name());
        doRegisterMethodUnderURI("GET", methodAccess, methodAccess.name().toLowerCase());
        doRegisterMethodUnderURI("GET", methodAccess, methodAccess.name().toUpperCase());


    }

    private void doRegisterMethodUnderURI(String httpMethod, MethodAccess methodAccess, String methodAddress) {

        if (methodAddress.startsWith("/")) {
            methodAddress = Str.slc(methodAddress, 1);
        }


        MethodBinding methodBinding = new MethodBinding(httpMethod, methodAccess.name(), Str.join('/', address, methodAddress));


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

        Map<String, Pair<MethodBinding, MethodAccess>> mappings = this.methodMap.get(methodBinding.address());

        if (mappings == null) {
            mappings = new HashMap<>(4);

            this.methodMap.put(methodBinding.address(), mappings);
        }

        mappings.put(methodBinding.method(), new Pair<>(methodBinding, methodAccess));

    }

    @Override
    public TreeSet<String> addresses() {
        return addresses;
    }

    @Override
    public void initQueue(SendQueue<Response<Object>> responseSendQueue) {
        this.responseSendQueue = responseSendQueue;
    }

    @Override
    public void queueInit() {

        queueCallBackHandler.queueInit();

    }

    @Override
    public void handleEvent(Event<Object> event) {

        MethodAccess methodAccess = eventMap.get(event.topic());


        if (invokeDynamic) {
            final Object body = event.body();

            if (body instanceof List) {
                List list = ((List) body);
                methodAccess.invokeDynamic(service, list.toArray(new Object[list.size()]));

            } else if (body instanceof Object[]) {
                final Object[] array = (Object[]) body;
                methodAccess.invokeDynamic(service, array);

            } else {
                methodAccess.invokeDynamicObject(service, body);
            }
        } else {
            final Object body = event.body();

            if (body instanceof List) {
                List list = ((List) body);
                methodAccess.invoke(service, list.toArray(new Object[list.size()]));

            } else if (body instanceof Object[]) {
                final Object[] array = (Object[]) body;
                methodAccess.invoke(service, array);

            } else {
                methodAccess.invoke(service, body);
            }
        }
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

    private String readHttpMethod(Annotated annotated) {
        String method = getHttpMethod("RequestMapping", annotated);
        return method == null || method.isEmpty() ? "GET" : method;
    }

    private String readNameFromAnnotation(Annotated annotated) {
        String name = null;

        if (Str.isEmpty(name)) {
            name = getAddress("Name", annotated);
        }

        if (Str.isEmpty(name)) {
            name = getAddress("Service", annotated);
        }


        return name == null ? "" : name;
    }

    private String getHttpMethod(String name, Annotated annotated) {
        AnnotationData requestMapping = annotated.annotation(name);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("method");

            if (value instanceof String[]) {

                String[] values = (String[]) value;
                if (values.length > 0 && values[0] != null && !values[0].isEmpty()) {
                    return values[0];
                }
            }
            if (value instanceof RequestMethod[]) {

                RequestMethod[] values = (RequestMethod[]) value;
                if (values.length > 0 && values[0] != null) {
                    return values[0].toString();
                }
            } else {

                return value.toString();
            }
        }
        return null;
    }

    private String getAddress(String name, Annotated annotated) {
        AnnotationData requestMapping = annotated.annotation(name);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("value");

            if (value instanceof String[]) {

                String[] values = (String[]) value;
                if (values.length > 0 && values[0] != null && !values[0].isEmpty()) {
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
    public String name() {
        return name;
    }

    @Override
    public void receive(MethodCall<Object> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void empty() {

        queueCallBackHandler.queueEmpty();
    }

    @Override
    public void limit() {
        queueCallBackHandler.queueLimit();
    }

    @Override
    public void shutdown() {
        queueCallBackHandler.queueShutdown();
    }

    @Override
    public void idle() {
        queueCallBackHandler.queueIdle();
    }

    public Map<String, Map<String, Pair<MethodBinding, MethodAccess>>> methodMap() {
        return methodMap;
    }

    static class BoonCallBackWrapper implements Callback<Object> {
        final SendQueue<Response<Object>> responseSendQueue;
        final MethodCall<Object> methodCall;

        BoonCallBackWrapper(final SendQueue<Response<Object>> responseSendQueue, final MethodCall<Object> methodCall) {

            this.responseSendQueue = responseSendQueue;
            this.methodCall = methodCall;
        }

        @Override
        public void accept(Object result) {
            /* This wants to be periodic flush or flush based on size but this is a stop gap make something work for now.
             */
            responseSendQueue.sendAndFlush(ResponseImpl.response(methodCall, result));
        }
    }
}
