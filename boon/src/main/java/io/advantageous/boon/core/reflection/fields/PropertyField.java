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
import java.lang.reflect.Method;

import static io.advantageous.boon.Boon.sputs;

public class PropertyField extends BaseField {
    final Method getter;
    final Method setter;


    public PropertyField( String name, Method setter, Method getter ) {

        super (  name,  getter,  setter);

        this.getter = getter;
        this.setter = setter;



//        MethodHandles.Lookup lookupWithDefault = MethodHandles.lookupWithDefault();
//        MethodType methodType
//                = MethodType.methodType ( this.type );
//        MethodHandle methodHandle = null;
//        CallSite callSiteMethod;
//
//        if ( parentType != null && getter != null ) {
//            try {
//                methodHandle = lookupWithDefault.findVirtual ( this.parentType, getter.name (), methodType );
//            } catch ( NoSuchMethodException e ) {
//               Exceptions.handle ( e );
//            } catch ( IllegalAccessException e ) {
//                Exceptions.handle ( e );
//            }
//            callSiteMethod = new ConstantCallSite(methodHandle);
//            this.getter = callSiteMethod.dynamicInvoker();
//
//        }  else {
//            this.getter = null;
//        }
//
//
//        if ( parentType != null && setter != null ) {
//
//            methodType
//                    = MethodType.methodType ( void.class, this.type() );
//
//
//            try {
//                methodHandle = lookupWithDefault.findVirtual ( this.parentType, setter.name(), methodType );
//            } catch ( NoSuchMethodException e ) {
//                Exceptions.handle ( e );
//            } catch ( IllegalAccessException e ) {
//                Exceptions.handle ( e );
//            }
//
//            callSiteMethod = new ConstantCallSite(methodHandle);
//            this.setter = callSiteMethod.dynamicInvoker ();
//        } else {
//            this.setter = null;
//        }
    }

    @Override
    public Object getObject( Object obj ) {
        try {
            return getter.invoke ( obj );
        } catch ( Throwable e ) {
            return Exceptions.handle( Object.class, sputs( "unable to call getObject for property ", this.name,
                    "for class ", this.type ), e );
        }
    }




    @Override
    public final void setObject( Object obj, Object value ) {
        try {
            if (!isReadOnly())
            setter.invoke ( obj, value );
        } catch ( Throwable e ) {
            Exceptions.handle( String.format( "You tried to modify property %s of %s for instance %s " +
                    "with set %s using %s, and this property read only status is %s",
                    name, obj.getClass().getSimpleName(), obj, value, name(), isReadOnly () ), e );

        }

    }



    public final boolean getBoolean( Object obj ) {
        try {
            return ( Boolean ) this.getObject ( obj );
        } catch ( Exception e ) {
            return Exceptions.handle( boolean.class, sputs( "unable to call getObject for property", this.name ), e );
        }

    }

    @Override
    public final int getInt( Object obj ) {
        try {
            return ( Integer ) this.getObject ( obj );
        } catch ( Exception e ) {
            return Exceptions.handle( int.class, sputs( "unable to call getObject for property", this.name ), e );
        }
    }

    @Override
    public final short getShort( Object obj ) {
        try {
            return ( Short ) this.getObject ( obj );
        } catch ( Exception e ) {
            return Exceptions.handle( short.class, sputs( "unable to call getObject for property", this.name ), e );
        }
    }

    @Override
    public final char getChar( Object obj ) {
        try {
            return ( Character ) this.getObject ( obj );
        } catch ( Exception e ) {
            return Exceptions.handle( char.class, sputs( "unable to call getObject for property", this.name ), e );
        }
    }

    @Override
    public final long getLong( Object obj ) {
        try {
            return ( Long ) this.getObject ( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public final double getDouble( Object obj ) {
        try {
            return ( Double ) this.getObject ( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final float getFloat( Object obj ) {
        try {
            return ( Float ) this.getObject ( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public final byte getByte( Object obj ) {
        try {
            return ( Byte ) this.getObject ( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public final Field getField() {
        return null;
    }

    @Override
    public void setStaticValue(Object newValue) {

    }


    @Override
    public final void setBoolean( Object obj, boolean value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setInt( Object obj, int value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setShort( Object obj, short value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setChar( Object obj, char value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setLong( Object obj, long value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setDouble( Object obj, double value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setFloat( Object obj, float value ) {
        try {
            this.setObject( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public final void setByte( Object obj, byte value ) {
        try {
            this.setObject ( obj, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );

        }

    }

}
