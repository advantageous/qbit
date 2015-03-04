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

import java.lang.reflect.Field;


public class ReflectField extends BaseField {
    private final Field field;

    public ReflectField ( Field field ) {
        super ( field );
        this.field = field;
    }



    @Override
    public Object getObject( Object obj ) {
        try {
            return field.get( obj );
        } catch ( Exception e ) {
            e.printStackTrace();
            analyzeError( e, obj );
            return null;
        }
    }


    public boolean getBoolean( Object obj ) {
        try {
            return field.getBoolean( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return false;
        }

    }

    @Override
    public int getInt( Object obj ) {
        try {
            return field.getInt( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }

    @Override
    public short getShort( Object obj ) {
        try {
            return field.getShort( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }

    @Override
    public char getChar( Object obj ) {
        try {
            return field.getChar( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }

    @Override
    public long getLong( Object obj ) {
        try {
            return field.getLong( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }

    @Override
    public double getDouble( Object obj ) {
        try {
            return field.getDouble( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }

    }

    @Override
    public float getFloat( Object obj ) {
        try {
            return field.getFloat( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }

    @Override
    public byte getByte( Object obj ) {
        try {
            return field.getByte( obj );
        } catch ( Exception e ) {
            analyzeError( e, obj );
            return 0;
        }
    }


    public boolean getStaticBoolean() {
        return getBoolean( null );
    }

    public int getStaticInt() {
        return getInt( null );

    }

    public short getStaticShort() {
        return getShort( null );
    }


    public long getStaticLong() {
        return getLong( null );
    }


    public double getStaticDouble() {
        return getDouble( null );
    }

    public float getStaticFloat() {
        return getFloat( null );
    }

    public byte getStaticByte() {
        return getByte( null );
    }

    public Object getObject() {
        return getObject( null );
    }

    @Override
    public final Field getField() {
        return field;
    }


    @Override
    public void setStaticValue(Object newValue) {
        try {
            field.set(null, newValue);
        } catch (IllegalAccessException e) {
            Exceptions.handle(e);
        }
    }


    @Override
    public void setBoolean( Object obj, boolean value ) {
        try {
            field.setBoolean( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setInt( Object obj, int value ) {
        try {
            field.setInt( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setShort( Object obj, short value ) {
        try {
            field.setShort( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setChar( Object obj, char value ) {
        try {
            field.setChar( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setLong( Object obj, long value ) {
        try {
            field.setLong( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setDouble( Object obj, double value ) {
        try {
            field.setDouble( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setFloat( Object obj, float value ) {
        try {
            field.setFloat( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setByte( Object obj, byte value ) {
        try {
            field.setByte( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }

    @Override
    public void setObject( Object obj, Object value ) {
        try {
            field.set( obj, value );
        } catch ( IllegalAccessException e ) {
            analyzeError( e, obj );
        }

    }



}
