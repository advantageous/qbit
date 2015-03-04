/*
 * Copyright 2013-2014 Richard M. Hightower
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
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.Annotations;
import io.advantageous.boon.core.reflection.ConstructorAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Exceptions.handle;

/**
 * Created by Richard on 2/20/14.
 * @author Rick Hightower
 */
public class ConstructorAccessImpl <T> implements ConstructorAccess {


    final Constructor<T> constructor;
    final List<AnnotationData> annotationData;
    final Map<String, AnnotationData> annotationMap;

    ConstructorAccessImpl() {
        constructor =null;
        annotationData=null;
        annotationMap=null;
    }

    public ConstructorAccessImpl( Constructor<T> method ) {
        this.constructor = method;
        this.constructor.setAccessible(true);
        this.annotationData = Annotations.getAnnotationDataForMethod(method);

        annotationMap = new ConcurrentHashMap<>(  );
        for (AnnotationData data : annotationData) {
            annotationMap.put( data.getName(), data );
            annotationMap.put( data.getSimpleClassName(), data );
            annotationMap.put( data.getFullClassName(), data );
        }

    }


    @Override
    public Iterable<AnnotationData> annotationData() {
        return new Iterable<AnnotationData>() {
            @Override
            public Iterator<AnnotationData> iterator() {
                return annotationData.iterator();
            }
        };
    }

    @Override
    public boolean hasAnnotation( String annotationName ) {
        return this.annotationMap.containsKey( annotationName );
    }

    @Override
    public AnnotationData annotation(String annotationName) {
        return this.annotationMap.get(annotationName);
    }

    @Override
    public Class<?>[] parameterTypes() {
        return constructor.getParameterTypes();
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return constructor.getGenericParameterTypes();
    }

    @Override
    public T create(Object... args) {
        try {
            return constructor.newInstance( args );
        } catch ( Exception ex ) {
            return Exceptions.handle(constructor.getDeclaringClass(), ex,
                    "\nunable to invoke constructor", constructor,
                    "\n on object ", constructor.getDeclaringClass(),
                    "\nwith arguments", args,
                    "\nparams", constructor.getParameterTypes());

        }

    }

    @Override
    public String toString() {
        return constructor.toString();
    }
}
