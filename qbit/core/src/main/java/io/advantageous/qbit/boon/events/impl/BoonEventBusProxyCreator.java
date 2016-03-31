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

package io.advantageous.qbit.boon.events.impl;

import io.advantageous.boon.core.Sets;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.core.reflection.ClassMeta.classMeta;
import static io.advantageous.qbit.annotation.AnnotationUtils.createChannelName;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

public class BoonEventBusProxyCreator implements EventBusProxyCreator {


    private static final String flushMethodNames = Sys.sysProp("io.advantageous.qbit.events.EventBusProxyCreator.flushMethodNames",
            "clientProxyFlush,flushEvents");
    private final Set<String> flushMethodNameSet = Sets.set(Str.split(flushMethodNames, ','));
    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();

    @Override
    public <T> T createProxy(final EventManager eventManager, final Class<T> eventBusProxyInterface) {

        return createProxyWithChannelPrefix(eventManager, eventBusProxyInterface, null);
    }

    @Override
    public <T> T createProxyWithChannelPrefix(final EventManager eventManager, final Class<T> eventBusProxyInterface,
                                              final String channelPrefix) {

        if (!eventBusProxyInterface.isInterface()) {
            throw new IllegalArgumentException("Must be an interface for eventBusProxyInterface argument");
        }
        final Map<String, String> methodToChannelMap = createMethodToChannelMap(channelPrefix, eventBusProxyInterface);

        final InvocationHandler invocationHandler = (proxy, method, args) -> {

            if (flushMethodNameSet.contains(method.getName())) {
                flushServiceProxy(eventManager);
                return null;
            }
            final String channelName = methodToChannelMap.get(method.toString());
            eventManager.sendArguments(channelName, args);

            return null;
        };
        final Object o = Proxy.newProxyInstance(eventBusProxyInterface.getClassLoader(), new Class[]{eventBusProxyInterface, ClientProxy.class}, invocationHandler);
        //noinspection unchecked
        return (T) o;

    }

    private <T> Map<String, String> createMethodToChannelMap(final String channelPrefix,
                                                             final Class<T> eventBusProxyInterface) {

        final Map<String, String> methodToChannelMap = new ConcurrentHashMap<>(20);
        final ClassMeta<T> classMeta = classMeta(eventBusProxyInterface);

        final AnnotationData classAnnotation = classMeta.annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);
        final String classEventBusName = AnnotationUtils.getClassEventChannelName(classMeta, classAnnotation);


        classMeta.methods().forEach(methodAccess -> {

            AnnotationData methodAnnotation = methodAccess.annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);

            String methodEventBusName = methodAnnotation != null && methodAnnotation.getValues().get("value") != null
                    ? methodAnnotation.getValues().get("value").toString() : null;

            if (Str.isEmpty(methodEventBusName)) {
                methodEventBusName = methodAccess.name();
            }

            final String channelName = createChannelName(channelPrefix, classEventBusName, methodEventBusName);
            methodToChannelMap.put(methodAccess.method().toString(), channelName);
        });

        return methodToChannelMap;
    }

}
