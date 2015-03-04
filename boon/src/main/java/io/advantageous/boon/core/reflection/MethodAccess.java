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

package io.advantageous.boon.core.reflection;

import io.advantageous.boon.core.TypeType;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Richard on 2/17/14.
 */
public interface MethodAccess extends BaseAccess, Comparable<MethodAccess>{

    public Object invokeDynamic(Object object, Object... args);
    public Object invoke(Object object, Object... args);
    boolean isStatic();
    boolean isPublic();
    boolean isPrivate();

    String name();

    Class<?> declaringType() ;


    Class<?> returnType() ;

    boolean respondsTo(Class<?>... types);

    boolean respondsTo(Object... args);


    Object invokeStatic(Object... args);

    MethodAccess bind(Object instance);

    MethodHandle methodHandle();

    MethodAccess methodAccess();

    Object bound();

    <T> ConstantCallSite invokeReducerLongIntReturnLongMethodHandle(T object);

    Method method();

    int score();


    List<TypeType> paramTypeEnumList();


    Object invokeDynamicObject(Object object, Object args);

    List<List<AnnotationData>> annotationDataForParams();


}