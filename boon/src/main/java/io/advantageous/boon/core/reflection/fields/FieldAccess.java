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

package io.advantageous.boon.core.reflection.fields;

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;


public interface FieldAccess {
    boolean injectable();
    boolean requiresInjection();
    boolean isNamed();
    boolean hasAlias();

    String alias();

    String named();

    String name();
    Object getValue( Object obj );

    void setValue( Object obj, Object value );
    public void setFromValue( Object obj, Value value );

    boolean getBoolean( Object obj );
    void setBoolean( Object obj, boolean value );


    int getInt( Object obj );
    void setInt( Object obj, int value );


    short getShort( Object obj );
    void setShort( Object obj, short value );

    char getChar( Object obj );
    void setChar( Object obj, char value );


    long getLong( Object obj );
    void setLong( Object obj, long value );


    double getDouble( Object obj );
    void setDouble( Object obj, double value );


    float getFloat( Object obj );
    void setFloat( Object obj, float value );


    byte getByte( Object obj );
    void setByte( Object obj, byte value );

    Object getObject( Object obj );
    void setObject( Object obj, Object value );


    TypeType typeEnum();


    boolean isPrimitive();

    boolean isFinal();
    boolean isStatic();
    boolean isVolatile();
    boolean isQualified();
    boolean isReadOnly();
    boolean isWriteOnly();

    Class<?> type();


    Class<?> declaringParent();

    Object parent();
    Field getField();



    boolean include();
    boolean ignore();

    ParameterizedType getParameterizedType();
    Class<?> getComponentClass();
    boolean hasAnnotation(String annotationName) ;
    Map<String, Object> getAnnotationData(String annotationName) ;
    boolean isViewActive (String activeView);


    void setStaticValue(Object newValue);

    TypeType componentType();
}