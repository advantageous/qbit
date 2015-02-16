/*******************************************************************************
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
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import org.boon.Str;
import org.boon.core.Sys;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
import static org.boon.core.reflection.ClassMeta.classMeta;

/**
 * Created by rhightower on 2/11/15.
 */
public class BoonEventBusProxyCreator implements EventBusProxyCreator {

    /* I don't think anyone will ever want to change this but they can via a system property. */
    public static final String EVENT_CHANNEL_ANNOTATION_NAME = Sys.sysProp("io.advantageous.qbit.events.EventBusProxyCreator.eventChannelName", "EventChannel");

    @Override
    public <T> T createProxy(final EventManager eventManager, final Class<T> eventBusProxyInterface) {

        return createProxyWithChannelPrefix(eventManager, eventBusProxyInterface, null);
    }

    @Override
    public <T> T createProxyWithChannelPrefix(final EventManager eventManager, final Class<T> eventBusProxyInterface, final String channelPrefix) {

        if ( !eventBusProxyInterface.isInterface() ) {
            throw new IllegalArgumentException("Must be an interface for eventBusProxyInterface argument");
        }
        final Map<String, String> methodToChannelMap = createMethodToChannelMap(channelPrefix, eventBusProxyInterface);

        final InvocationHandler invocationHandler = (proxy, method, args) -> {

            if ( method.getName().equals("clientProxyFlush") ) {
                flushServiceProxy(eventManager);
                return null;
            }
            final String channelName = methodToChannelMap.get(method.toString());
            eventManager.sendArray(channelName, args);

            return null;
        };
        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{eventBusProxyInterface, ClientProxy.class}, invocationHandler);
        return ( T ) o;

    }

    private <T> Map<String, String> createMethodToChannelMap(String channelPrefix, Class<T> eventBusProxyInterface) {

        final Map<String, String> methodToChannelMap = new ConcurrentHashMap<>(20);
        final ClassMeta<T> classMeta = classMeta(eventBusProxyInterface);

        AnnotationData classAnnotation = classMeta.annotation(EVENT_CHANNEL_ANNOTATION_NAME);
        //They could even use enum as we are getting a string value
        final String classEventBusName = classAnnotation != null ? classAnnotation.getValues().get("value").toString() : null;


        classMeta.methods().forEach(methodAccess -> {

            AnnotationData methodAnnotation = methodAccess.annotation("EventChannel");
            final String methodEventBusName = methodAnnotation != null ? methodAnnotation.getValues().get("value").toString() : null;


            final String channelName = createChannelName(channelPrefix, classEventBusName, methodEventBusName);
            methodToChannelMap.put(methodAccess.method().toString(), channelName);
        });

        return methodToChannelMap;
    }

    private String createChannelName(final String channelPrefix, final String classChannelNamePart, final String methodChannelNamePart) {

        if ( methodChannelNamePart == null ) {
            throw new IllegalArgumentException("Each method must have an event bus channel name");
        }

        //If Channel prefix is null then just use class channel name and method channel name
        if ( channelPrefix == null ) {

            //If the class channel name is null just return the method channel name.
            if ( classChannelNamePart == null ) {
                return methodChannelNamePart;
            } else {

                //Channel name takes the form ${classChannelNamePart.methodChannelNamePart}
                return Str.join('.', classChannelNamePart, methodChannelNamePart);
            }
        } else {
            //If classChannelNamePart null then channel name takes the form ${channelPrefix.methodChannelNamePart}
            if ( classChannelNamePart == null ) {
                return Str.join('.', channelPrefix, methodChannelNamePart);
            } else {
                //Nothing was null so the channel name takes the form ${channelPrefix.classChannelNamePart.methodChannelNamePart}
                return Str.join('.', channelPrefix, classChannelNamePart, methodChannelNamePart);
            }
        }
    }
}
