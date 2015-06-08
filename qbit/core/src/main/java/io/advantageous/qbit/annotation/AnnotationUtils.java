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

package io.advantageous.qbit.annotation;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.Annotated;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;

/**
 * This class is used to read annotation data.
 * Currently it is mostly used by the event system.
 * Note: It could use some refactoring and perhaps a builder so that it is easier
 * to change annotation processing names, which will happen when somebody needs it and is willing to submit a patch.
 * AnnotationUtils
 * created by rhightower on 2/4/15.
 */
public class AnnotationUtils {


    /* I don't think anyone will ever want to change this but they can via a system property. */
    public static final String EVENT_CHANNEL_ANNOTATION_NAME =
            Sys.sysProp("io.advantageous.qbit.events.EventBusProxyCreator.eventChannelName", "EventChannel");


    public static AnnotationData getListenAnnotation(MethodAccess methodAccess) {
        AnnotationData listen = methodAccess.annotation("Listen");

        if (listen == null) {
            listen = methodAccess.annotation("OnEvent");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Subscribe");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Consume");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Hear");
        }
        return listen;
    }


    public static String readServiceName(Object annotated) {

        final ClassMeta classMeta = ClassMeta.classMeta(annotated.getClass());

        final String name = readServiceName(classMeta);

        if (name == null || name.isEmpty()) {
            return annotated.getClass().getSimpleName();
        } else {
            return name;
        }
    }

    public static String readServiceName(Annotated annotated) {
        String name = readValue("Service", annotated);

        if (Str.isEmpty(name)) {
            name = readValue("ServiceName", annotated);
        }

        if (Str.isEmpty(name)) {
            name = readValue("Named", annotated);
        }

        if (Str.isEmpty(name)) {
            name = readValue("Name", annotated);
        }


        return name == null ? "" : name;
    }

    private static String readValue(String name, Annotated annotated) {
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


    public static <T> String getClassEventChannelName(ClassMeta<T> classMeta, AnnotationData classAnnotation) {
        //They could even use enum as we are getting a string value

        String classEventBusName = classAnnotation != null
                && classAnnotation.getValues().get("value") != null ? classAnnotation.getValues().get("value").toString() : null;

        if (Str.isEmpty(classEventBusName) && classAnnotation != null) {
            classEventBusName = classMeta.longName();
        }
        return classEventBusName;
    }


    public static String createChannelName(final String channelPrefix, final String classChannelNamePart, final String methodChannelNamePart) {

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
