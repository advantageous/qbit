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
import io.advantageous.boon.core.Typ;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static io.advantageous.boon.Exceptions.die;


public abstract class UnsafeField extends BaseField {

    protected long offset;
    protected final Object base;

    private final Field field;


    protected UnsafeField ( Field field  )  {
        super(field);
        if ( super.isStatic() ) {
            base = unsafe.staticFieldBase( field );
            offset = unsafe.staticFieldOffset( field );
        } else {
            offset = unsafe.objectFieldOffset( field );
            base = null;
        }
        this.field = field;
    }


    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField( "theUnsafe" );
            f.setAccessible( true );
            return ( Unsafe ) f.get( null );
        } catch ( Exception e ) {
            return null;
        }
    }

    protected static final Unsafe unsafe = getUnsafe();

    public static UnsafeField createUnsafeField( Field field ) {
        Class<?> type = field.getType();
        boolean isVolatile = Modifier.isVolatile( field.getModifiers() );
        if ( !isVolatile ) {
            if ( type == Typ.intgr ) {
                return new IntUnsafeField( field );
            } else if ( type == Typ.lng ) {
                return new LongUnsafeField( field );
            } else if ( type == Typ.bt ) {
                return new ByteUnsafeField( field );
            } else if ( type == Typ.shrt ) {
                return new ShortUnsafeField( field );
            } else if ( type == Typ.chr ) {
                return new CharUnsafeField( field );
            } else if ( type == Typ.dbl ) {
                return new DoubleUnsafeField( field );
            } else if ( type == Typ.flt ) {
                return new FloatUnsafeField( field );
            } else if ( type == Typ.bln ) {
                return new BooleanUnsafeField( field );
            } else {
                return new ObjectUnsafeField( field );
            }
        } else {
            if ( type == Typ.intgr ) {
                return new VolatileIntUnsafeField( field );
            } else if ( type == Typ.lng ) {
                return new VolatileLongUnsafeField( field );
            } else if ( type == Typ.bt ) {
                return new VolatileByteUnsafeField( field );
            } else if ( type == Typ.shrt ) {
                return new VolatileShortUnsafeField( field );
            } else if ( type == Typ.chr ) {
                return new VolatileCharUnsafeField( field );
            } else if ( type == Typ.dbl ) {
                return new VolatileDoubleUnsafeField( field );
            } else if ( type == Typ.flt ) {
                return new VolatileFloatUnsafeField( field );
            } else if ( type == Typ.bln ) {
                return new VolatileBooleanUnsafeField( field );
            } else {
                return new ObjectUnsafeField( field );
            }

        }
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
    public int getInt( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }

    @Override
    public boolean getBoolean( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return false;
    }


    @Override
    public short getShort( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public char getChar( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public long getLong( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public double getDouble( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public float getFloat( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public byte getByte( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
        return 0;
    }


    @Override
    public Object getObject( Object obj ) {
        Exceptions.die(String.format("Can't call this method on this type %s for field %s", this.type, this.name));
        return 0;
    }


    public boolean getStaticBoolean() {
        return getBoolean( base );
    }


    public int getStaticInt() {
        return getInt( base );
    }


    public short getStaticShort() {
        return getShort( base );
    }


    public long getStaticLong() {
        return getLong( base );
    }

    public double getStaticDouble() {
        return getDouble( base );
    }


    public float getStaticFloat() {
        return getFloat( base );
    }


    public byte getStaticByte() {
        return getByte( base );
    }


    public Object getObject() {
        return getObject( base );
    }


    @Override
    public Field getField() {
        return field;
    }



    public Object getBase() {
        return base;
    }




    @Override
    public void setBoolean( Object obj, boolean value ) {

        Exceptions.die(String.format("Can't call this method on this type %s", this.type));

    }


    @Override
    public void setInt( Object obj, int value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));

    }


    @Override
    public void setShort( Object obj, short value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));


    }


    @Override
    public void setChar( Object obj, char value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));

    }


    @Override
    public void setLong( Object obj, long value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));

    }


    @Override
    public void setDouble( Object obj, double value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));

    }


    @Override
    public void setFloat( Object obj, float value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
    }


    @Override
    public void setByte( Object obj, byte value ) {
        Exceptions.die(String.format("Can't call this method on this type %s", this.type));
    }


    @Override
    public void setObject( Object obj, Object value ) {
        Exceptions.die(String.format("Can't call this method on this type %s name = %s  value type = %s", this.type, this.name,
                value == null ? "null" : value.getClass()));

    }




    private static final class IntUnsafeField extends UnsafeField {

        protected IntUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public final void setInt( Object obj, int value ) {
            unsafe.putInt( obj, offset, value );
        }


        @Override
        public final int getInt( Object obj ) {
            return unsafe.getInt( obj, offset );
        }
    }

    private static class LongUnsafeField extends UnsafeField {

        protected LongUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setLong( Object obj, long value ) {
            unsafe.putLong( obj, offset, value );
        }

        @Override
        public long getLong( Object obj ) {
            return unsafe.getLong( obj, offset );
        }
    }

    private static class CharUnsafeField extends UnsafeField {

        protected CharUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setChar( Object obj, char value ) {
            unsafe.putChar( obj, offset, value );
        }

        @Override
        public char getChar( Object obj ) {
            return unsafe.getChar( obj, offset );
        }
    }

    private static class ByteUnsafeField extends UnsafeField {

        protected ByteUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setByte( Object obj, byte value ) {
            unsafe.putByte( obj, offset, value );
        }

        @Override
        public byte getByte( Object obj ) {
            return unsafe.getByte( obj, offset );
        }
    }

    private static class ShortUnsafeField extends UnsafeField {

        protected ShortUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setShort( Object obj, short value ) {
            unsafe.putShort( obj, offset, value );
        }

        @Override
        public short getShort( Object obj ) {
            return unsafe.getShort( obj, offset );
        }
    }

    private static class ObjectUnsafeField extends UnsafeField {

        protected ObjectUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setObject( Object obj, Object value ) {
            unsafe.putObject( obj, offset, value );
        }

        @Override
        public Object getObject( Object obj ) {
            return unsafe.getObject( obj, offset );
        }
    }

    private static class FloatUnsafeField extends UnsafeField {

        protected FloatUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setFloat( Object obj, float value ) {
            unsafe.putFloat( obj, offset, value );
        }

        @Override
        public float getFloat( Object obj ) {
            return unsafe.getFloat( obj, offset );
        }
    }

    private static class DoubleUnsafeField extends UnsafeField {

        protected DoubleUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setDouble( Object obj, double value ) {
            unsafe.putDouble( obj, offset, value );
        }

        @Override
        public double getDouble( Object obj ) {
            return unsafe.getDouble( obj, offset );
        }
    }


    private static class BooleanUnsafeField extends UnsafeField {

        protected BooleanUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setBoolean( Object obj, boolean value ) {
            unsafe.putBoolean( obj, offset, value );
        }

        @Override
        public boolean getBoolean( Object obj ) {
            return unsafe.getBoolean( obj, offset );
        }
    }


    private static class VolatileIntUnsafeField extends UnsafeField {

        protected VolatileIntUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setInt( Object obj, int value ) {
            unsafe.putIntVolatile( obj, offset, value );
        }

        @Override
        public int getInt( Object obj ) {
            return unsafe.getIntVolatile( obj, offset );
        }
    }


    private static class VolatileBooleanUnsafeField extends UnsafeField {

        protected VolatileBooleanUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setBoolean( Object obj, boolean value ) {
            unsafe.putBooleanVolatile( obj, offset, value );
        }

        @Override
        public boolean getBoolean( Object obj ) {
            return unsafe.getBooleanVolatile( obj, offset );
        }
    }

    private static class VolatileLongUnsafeField extends UnsafeField {

        protected VolatileLongUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setLong( Object obj, long value ) {
            unsafe.putLongVolatile( obj, offset, value );
        }

        @Override
        public long getLong( Object obj ) {
            return unsafe.getLongVolatile( obj, offset );
        }
    }

    private static class VolatileCharUnsafeField extends UnsafeField {

        protected VolatileCharUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setChar( Object obj, char value ) {
            unsafe.putCharVolatile( obj, offset, value );
        }

        @Override
        public char getChar( Object obj ) {
            return unsafe.getCharVolatile( obj, offset );
        }
    }

    private static class VolatileByteUnsafeField extends UnsafeField {

        protected VolatileByteUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setByte( Object obj, byte value ) {
            unsafe.putByteVolatile( obj, offset, value );
        }

        @Override
        public byte getByte( Object obj ) {
            return unsafe.getByteVolatile( obj, offset );
        }
    }

    private static class VolatileShortUnsafeField extends UnsafeField {

        protected VolatileShortUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setShort( Object obj, short value ) {
            unsafe.putShortVolatile( obj, offset, value );
        }

        @Override
        public short getShort( Object obj ) {
            return unsafe.getShortVolatile( obj, offset );
        }
    }

    private static class VolatileObjectUnsafeField extends UnsafeField {

        protected VolatileObjectUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setObject( Object obj, Object value ) {
            unsafe.putObjectVolatile( obj, offset, value );
        }

        @Override
        public Object getObject( Object obj ) {
            return unsafe.getObjectVolatile( obj, offset );
        }
    }

    private static class VolatileFloatUnsafeField extends UnsafeField {

        protected VolatileFloatUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setFloat( Object obj, float value ) {
            unsafe.putFloatVolatile( obj, offset, value );
        }

        @Override
        public float getFloat( Object obj ) {
            return unsafe.getFloatVolatile( obj, offset );
        }
    }

    private static class VolatileDoubleUnsafeField extends UnsafeField {

        protected VolatileDoubleUnsafeField( Field f ) {
            super( f );
        }

        @Override
        public void setDouble( Object obj, double value ) {
            unsafe.putDoubleVolatile( obj, offset, value );
        }

        @Override
        public double getDouble( Object obj ) {
            return unsafe.getDoubleVolatile( obj, offset );
        }
    }


}
