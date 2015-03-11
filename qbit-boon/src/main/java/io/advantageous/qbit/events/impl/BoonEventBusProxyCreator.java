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

package io.advantageous.qbit.events.impl;

import io.advantageous.boon.Sets;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.boon.core.reflection.AnnotationData;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.core.reflection.ClassMeta.classMeta;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

public class BoonEventBusProxyCreator implements EventBusProxyCreator {

    /* I don't think anyone will ever want to change this but they can via a system property. */
    public static final String EVENT_CHANNEL_ANNOTATION_NAME =
            Sys.sysProp("io.advantageous.qbit.events.EventBusProxyCreator.eventChannelName", "EventChannel");


    private static final String flushMethodNames =  Sys.sysProp("io.advantageous.qbit.events.EventBusProxyCreator.flushMethodNames",
            "clientProxyFlush,flushEvents");


    private final Set<String> flushMethodNameSet = Sets.set(Str.split(flushMethodNames, ','));


    @Override
    public <T> T createProxy(final EventManager eventManager, final Class<T> eventBusProxyInterface) {

        return createProxyWithChannelPrefix(eventManager, eventBusProxyInterface, null);
    }

    @Override
    public <T> T createProxyWithChannelPrefix(final EventManager eventManager, final Class<T> eventBusProxyInterface, final String channelPrefix) {

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
            eventManager.sendArray(channelName, args);

            return null;
        };
        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{eventBusProxyInterface, ClientProxy.class}, invocationHandler);
        return (T) o;

    }

    private <T> Map<String, String> createMethodToChannelMap(String channelPrefix, Class<T> eventBusProxyInterface) {

        final Map<String, String> methodToChannelMap = new ConcurrentHashMap<>(20);
        final ClassMeta<T> classMeta = classMeta(eventBusProxyInterface);

        AnnotationData classAnnotation = classMeta.annotation(EVENT_CHANNEL_ANNOTATION_NAME);
        //They could even use enum as we are getting a string value
        final String classEventBusName = classAnnotation != null ? classAnnotation.getValues().get("value").toString() : null;


        classMeta.methods().forEach(methodAccess -> {

            AnnotationData methodAnnotation = methodAccess.annotation("EventChannel");
            if (methodAnnotation !=null) {
                final String methodEventBusName = methodAnnotation.getValues().get("value").toString();


                final String channelName = createChannelName(channelPrefix, classEventBusName, methodEventBusName);
                methodToChannelMap.put(methodAccess.method().toString(), channelName);
            }
        });

        return methodToChannelMap;
    }

    private String createChannelName(final String channelPrefix, final String classChannelNamePart, final String methodChannelNamePart) {

        if (methodChannelNamePart == null) {
            throw new IllegalArgumentException("Each method must have an event bus channel name");
        }

        //If Channel prefix is null then just use class channel name and method channel name
        if (channelPrefix == null) {

            //If the class channel name is null just return the method channel name.
            if (classChannelNamePart == null) {
                return methodChannelNamePart;
            } else {

                //Channel name takes the form ${classChannelNamePart.methodChannelNamePart}
                return Str.join('.', classChannelNamePart, methodChannelNamePart);
            }
        } else {
            //If classChannelNamePart null then channel name takes the form ${channelPrefix.methodChannelNamePart}
            if (classChannelNamePart == null) {
                return Str.join('.', channelPrefix, methodChannelNamePart);
            } else {
                //Nothing was null so the channel name takes the form ${channelPrefix.classChannelNamePart.methodChannelNamePart}
                return Str.join('.', channelPrefix, classChannelNamePart, methodChannelNamePart);
            }
        }
    }
}
