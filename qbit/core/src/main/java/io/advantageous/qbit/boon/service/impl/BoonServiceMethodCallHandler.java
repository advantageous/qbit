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

package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.Annotated;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceMethodHandler;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.qbit.annotation.AnnotationUtils.*;

/**
 * created by Richard on 9/8/14.
 *
 * @author Rick Hightower
 */
public class BoonServiceMethodCallHandler implements ServiceMethodHandler {


    final Map<String, Boolean> hasHandlerMap = new HashMap<>();
    private final boolean invokeDynamic;
    private final MapAndInvoke mapAndInvoke;
    private final TreeSet<String> addresses = new TreeSet<>();
    private final Map<String, MethodAccess> methodMap = new LinkedHashMap<>();
    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    private final Object context = Sys.contextToHold();
    private final Map<String, MethodAccess> eventMap = new ConcurrentHashMap<>();
    protected Object service;
    SendQueue<Response<Object>> responseSendQueue;
    private ClassMeta<Class<?>> classMeta;
    private QueueCallBackHandler queueCallBackHandler;
    private String address = "";
    private String name = "";


    public BoonServiceMethodCallHandler(final boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;

        if (invokeDynamic) {
            this.mapAndInvoke = new MapAndInvokeDynamic(this);
        } else {
            this.mapAndInvoke = new MapAndInvokeImpl(this);
        }
    }

    @Override
    public Response<Object> receiveMethodCall(MethodCall<Object> methodCall) {

        try {
            return invokeByName(methodCall);
        } catch (Exception ex) {


            if (ex.getCause() instanceof InvocationTargetException) {
                InvocationTargetException tex = (InvocationTargetException) ex.getCause();
                return new ResponseImpl<>(methodCall, tex.getTargetException());
            }
            return new ResponseImpl<>(methodCall, ex);
        }
    }

    private Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> methodCall, MethodAccess serviceMethod) {
        return this.mapAndInvoke.mapArgsAsyncHandlersAndInvoke(methodCall, serviceMethod);
    }

    /**
     * False is unknown, true is no callbacks.
     *
     * @param name name of method
     * @return false signifies maybe, true means never.
     */
    public boolean couldHaveCallback(final String name) {

        final Boolean has = hasHandlerMap.get(name);
        if (has == null) {
            return true;
        }

        return has;
    }

    protected boolean hasHandlers(MethodCall<Object> methodCall, MethodAccess serviceMethod) {

        Boolean has = hasHandlerMap.get(methodCall.name());

        boolean hasHandlers;
        if (has == null) {
            hasHandlers = hasHandlers(methodCall);
            hasHandlers = hasHandlers(serviceMethod) || hasHandlers;
            hasHandlerMap.put(methodCall.name(), hasHandlers);
        } else {
            hasHandlers = has;
        }

        return hasHandlers;

    }

    private boolean hasHandlers(MethodAccess serviceMethod) {

        if (PromiseHandle.class.isAssignableFrom(serviceMethod.returnType())) {
            return true;
        }

        for (Class<?> paramType : serviceMethod.parameterTypes()) {
            if (paramType == Callback.class) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHandlers(MethodCall<Object> methodCall) {

        if (methodCall.hasCallback()) {
            return true;
        }
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

    protected void extractHandlersFromArgumentList(Callback<Object> callback, MethodAccess method, Object body, List<Object> argsList) {
        if (body instanceof List) {

            @SuppressWarnings("unchecked") List<Object> list = (List<Object>) body;

            extractHandlersFromArgumentListBodyIsList(callback, method, argsList, list);

        } else if (body instanceof Object[]) {
            extractHandlersFromArgumentListArrayCase(callback, method, (Object[]) body, argsList);
        }
    }

    private void extractHandlersFromArgumentListArrayCase(final Callback<Object> callback,
                                                          final MethodAccess serviceMethod,
                                                          final Object[] inputParams,
                                                          final List<Object> preparedArgumentList) {
        final Class<?>[] parameterTypes = serviceMethod.method().getParameterTypes();


        /** If the argument need by the service method is greater than the arguments than we have in the
         * inputParams than we need to find the callback.
         */
        if (inputParams.length < preparedArgumentList.size()) {
            for (int index = 0, arrayIndex = 0; index < preparedArgumentList.size(); index++, arrayIndex++) {
                final Object o = preparedArgumentList.get(index);
                if (parameterTypes[index] == Callback.class) {
                    if (o == null) {
                        preparedArgumentList.set(index, callback);
                    }
                    arrayIndex--;
                    continue;
                }
                if (arrayIndex >= inputParams.length) {
                    break;
                }
                preparedArgumentList.set(index, inputParams[arrayIndex]);

            }
        } else {


            if (inputParams.length > preparedArgumentList.size()) {
                for (int index = 0, arrayIndex = 0; index < preparedArgumentList.size(); index++, arrayIndex++) {

                    final Object in = inputParams[arrayIndex];
                    if (in instanceof Callback) {
                        arrayIndex++;
                    }
                    if (arrayIndex >= inputParams.length) {
                        break;
                    }
                    preparedArgumentList.set(index, inputParams[arrayIndex]);

                }

            } else {

                for (int index = 0, arrayIndex = 0; index < preparedArgumentList.size(); index++, arrayIndex++) {
                    final Object paramPrepare = preparedArgumentList.get(index);
                    if (paramPrepare instanceof Callback) {
                        continue;
                    }
                    if (arrayIndex >= inputParams.length) {
                        break;
                    }
                    preparedArgumentList.set(index, inputParams[arrayIndex]);

                }
            }
        }

    }

    private void extractHandlersFromArgumentListBodyIsList(final Callback<Object> callback,
                                                           final MethodAccess serviceMethod,
                                                           final List<Object> argsList,
                                                           List<Object> list) {


        final Class<?>[] parameterTypes = serviceMethod.method().getParameterTypes();

        /** Check to see if the array size is one less that the argument size
         * If it is then we think this is a method that has a Callback
         * as the first argument.
         */
        if (serviceMethod.method().getReturnType() == void.class && (list.size() - 1 == serviceMethod.parameterTypes().length)) {
            if (parameterTypes[0] == Callback.class) {
                list = Lists.slc(list, 1); //remove the callback from the args bc the callback is called
                // when the method returns.
            }
        }

        if (list.size() < argsList.size()) {
            for (int index = 0, arrayIndex = 0; index < argsList.size(); index++, arrayIndex++) {
                final Object o = argsList.get(index);
                if (parameterTypes[index] == Callback.class) {
                    if (o == null) {
                        argsList.set(index, callback);
                    }
                    arrayIndex--;
                    continue;
                }
                if (arrayIndex >= list.size()) {
                    break;
                }
                argsList.set(index, list.get(arrayIndex));

            }
        } else {

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
    }

    protected Response<Object> response(MethodAccess methodAccess, MethodCall<Object> methodCall, Object returnValue) {

        if (methodAccess.returnType() == void.class || methodAccess.returnType() == Void.class) {
            return ServiceConstants.VOID;
        }
        return ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.address(), methodCall.returnAddress(), returnValue, methodCall);
    }

    protected List<Object> prepareArgumentList(final MethodCall<Object> methodCall, Class<?>[] parameterTypes) {
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

    protected Callback<Object> createCallBackHandler(final MethodCall<Object> methodCall) {

        return new BoonCallBackWrapper(responseSendQueue, methodCall);

    }

    private Response<Object> invokeByName(MethodCall<Object> methodCall) {
        final MethodAccess method = classMeta.method(methodCall.name());

        if (method != null) {
            return mapArgsAsyncHandlersAndInvoke(methodCall, method);
        } else {

            if (methodCall.name().equals("toString")) {
                return ResponseImpl.response(
                        methodCall.id(),
                        methodCall.timestamp(),
                        methodCall.address(),
                        methodCall.returnAddress(),
                        sputs("Method Call toString was called", methodCall.objectName(), methodCall.name(), methodCall.address()),
                        methodCall, false);

            } else {
                return ResponseImpl.response(
                        methodCall.id(),
                        methodCall.timestamp(),
                        methodCall.address(),
                        methodCall.returnAddress(),
                        new Exception("Unable to find method " + methodCall.name()),
                        methodCall, true);
            }
        }
    }

    @Override
    public void init(
            Object service,
            String rootAddress,
            String serviceAddress,
            SendQueue<Response<Object>> responseSendQueue) {

        this.responseSendQueue = responseSendQueue;
        this.service = service;
        //noinspection unchecked
        classMeta = (ClassMeta<Class<?>>) ClassMeta.classMeta(service.getClass());

        addresses.add(Str.add(rootAddress, "/", classMeta.longName()));
        addresses.add(Str.add(rootAddress, "/", classMeta.longName().toLowerCase()));

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

        final Class<?>[] interfaces = classMeta.cls().getInterfaces();
        for (Class<?> interfaceClass : interfaces) {

            if (interfaceClass.getName().equals("groovy.lang.GroovyObject")) continue;

            final ClassMeta<?> interfaceMeta = ClassMeta.classMeta(interfaceClass);

            final AnnotationData channelAnnotation =
                    interfaceMeta.annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);

            if (channelAnnotation == null) {
                continue;
            }

            final String classEventBusName = getClassEventChannelName(interfaceMeta, channelAnnotation);


            interfaceMeta.methods().forEach(methodAccess -> {

                AnnotationData methodAnnotation = methodAccess.annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);

                String methodEventBusName = methodAnnotation != null && methodAnnotation.getValues().get("value") != null
                        ? methodAnnotation.getValues().get("value").toString() : null;

                if (Str.isEmpty(methodEventBusName)) {
                    methodEventBusName = methodAccess.name();
                }

                final String channelName = createChannelName(null, classEventBusName, methodEventBusName);
                eventMap.put(channelName, methodAccess);

            });
        }

        readMethodMetaData();
        initQueueHandlerMethods();

    }

    private void initQueueHandlerMethods() {

        this.queueCallBackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(service);

    }

    @Override
    public void init() {
        queueCallBackHandler.queueInit();
    }

    @Override
    public void startBatch() {
        queueCallBackHandler.queueStartBatch();

    }

    private void readMethodMetaData() {


        final Iterable<MethodAccess> methods = classMeta.methods();
        for (MethodAccess methodAccess : methods) {

            registerMethod(methodAccess);
        }

        addresses.addAll(methodMap.keySet());
    }

    private void registerMethod(MethodAccess methodAccess) {

        if (!methodAccess.isPrivate()) {


            String methodAddress = readAddressFromAnnotation(methodAccess);


            if (methodAddress != null && !methodAddress.isEmpty()) {

                doRegisterMethodUnderURI(methodAccess, methodAddress);


            }

        }


        doRegisterMethodUnderURI(methodAccess, methodAccess.name());
        doRegisterMethodUnderURI(methodAccess, methodAccess.name().toLowerCase());
        doRegisterMethodUnderURI(methodAccess, methodAccess.name().toUpperCase());


    }

    private void doRegisterMethodUnderURI(final MethodAccess methodAccess, String methodAddress) {

        if (methodAddress.startsWith("/")) {
            methodAddress = Str.slc(methodAddress, 1);
        }


        this.methodMap.put(Str.add(this.address, "/", methodAddress), methodAccess);

    }

    @Override
    public TreeSet<String> addresses() {
        return addresses;
    }

    @Override
    public void handleEvent(Event<Object> event) {

        MethodAccess methodAccess = eventMap.get(event.channel());


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

    private String readNameFromAnnotation(Annotated annotated) {
        String name = getAddress("Named", annotated);

        if (Str.isEmpty(name)) {
            name = getAddress("Name", annotated);
        }

        if (Str.isEmpty(name)) {
            name = getAddress("Service", annotated);
        }

        if (Str.isEmpty(name)) {
            name = getAddress("ServiceName", annotated);
        }


        return name == null ? "" : name;
    }

    private String getHttpMethod(@SuppressWarnings("SameParameterValue") String name, Annotated annotated) {
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

                return (String) value;
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

    static class BoonCallBackWrapper implements Callback<Object> {
        final SendQueue<Response<Object>> responseSendQueue;
        final MethodCall<Object> methodCall;

        BoonCallBackWrapper(final SendQueue<Response<Object>> responseSendQueue, final MethodCall<Object> methodCall) {

            this.responseSendQueue = responseSendQueue;
            this.methodCall = methodCall;
        }

        @Override
        public void accept(Object result) {
            responseSendQueue.send(ResponseImpl.response(methodCall, result));
        }


        @Override
        public void onError(Throwable error) {
            responseSendQueue.send(ResponseImpl.error(methodCall, error));
        }

        @Override
        public void onTimeout() {
            responseSendQueue.send(ResponseImpl.error(methodCall, new TimeoutException("Method call " + methodCall.name() + " timed out ")));
        }
    }


}
