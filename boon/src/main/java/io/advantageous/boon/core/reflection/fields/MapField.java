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

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.TypeType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;

public class MapField implements FieldAccess {

    private final String name;

    public  MapField( String name ) {
        this.name = name;
    }

    @Override
    public boolean injectable() {
        return false;
    }

    @Override
    public boolean requiresInjection() {
        return false;
    }

    @Override
    public boolean isNamed() {
        return false;
    }

    @Override
    public boolean hasAlias() {
        return false;
    }

    @Override
    public String alias() {
        return name;
    }

    @Override
    public String named() {
        return alias();
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final Object getValue( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return map.get( name );
        }
        return Exceptions.die(Object.class, "Object must be a map but was a " + obj.getClass().getName());
    }

    @Override
    public final void setValue( Object obj, Object value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
            return;
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final void setFromValue( Object obj, Value value ) {
        setValue( obj, value.toValue() );
    }

    @Override
    public final boolean getBoolean( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toBoolean(map.get(name));
        }
        return Exceptions.die(Boolean.class, "Object must be a map");
    }

    @Override
    public final void setBoolean( Object obj, boolean value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final int getInt( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toInt( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return -1;
    }

    @Override
    public final void setInt( Object obj, int value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final short getShort( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toShort( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return -1;
    }

    @Override
    public final void setShort( Object obj, short value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final char getChar( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toChar( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return 0;
    }

    @Override
    public final void setChar( Object obj, char value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final long getLong( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toLong( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return -1;
    }

    @Override
    public final void setLong( Object obj, long value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final double getDouble( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toDouble( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return Double.NaN;
    }

    @Override
    public final void setDouble( Object obj, double value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final float getFloat( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toFloat( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return Float.NaN;
    }

    @Override
    public final void setFloat( Object obj, float value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final byte getByte( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return Conversions.toByte( map.get( name ) );
        }
        Exceptions.die("Object must be a map");
        return Byte.MAX_VALUE;
    }

    @Override
    public final void setByte( Object obj, byte value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");

    }

    @Override
    public final Object getObject( Object obj ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            return map.get( name );
        }
        Exceptions.die("Object must be a map");
        return -1;
    }

    @Override
    public final void setObject( Object obj, Object value ) {
        if ( obj instanceof Map ) {
            Map map = ( Map ) obj;
            map.put( name, value );
        }
        Exceptions.die("Object must be a map");
    }

    @Override
    public final TypeType typeEnum () {
        return TypeType.OBJECT;
    }


    @Override
    public final boolean isPrimitive () {
        return false;
    }


    @Override
    public final Field getField() {
        return Exceptions.die(Field.class, "Unsupported operation");

    }

    @Override
    public final boolean include () {
        return false;
    }

    @Override
    public final boolean ignore () {
        return false;
    }

    @Override
    public final ParameterizedType getParameterizedType() {
        return null;
    }

    @Override
    public final Class<?> getComponentClass() {
        return null;
    }

    @Override
    public final boolean hasAnnotation ( String annotationName ) {
        return false;
    }

    @Override
    public final Map<String, Object> getAnnotationData ( String annotationName ) {
        return Collections.EMPTY_MAP;
    }

    @Override
    public boolean isViewActive( String activeView ) {
        return true;
    }

    @Override
    public void setStaticValue(Object newValue) {

    }

    @Override
    public TypeType componentType() {
        return null;
    }

    @Override
    public final boolean isFinal() {
        return false;
    }

    @Override
    public final boolean isStatic() {
        return false;
    }

    @Override
    public final boolean isVolatile() {
        return false;
    }

    @Override
    public final boolean isQualified() {
        return false;
    }

    @Override
    public final boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isWriteOnly() {
        return false;
    }

    @Override
    public final Class<?> type() {
        return Object.class;
    }

    @Override
    public Class<?> declaringParent() {
        return null;
    }

    @Override
    public Object parent() {
        return null;
    }
}
