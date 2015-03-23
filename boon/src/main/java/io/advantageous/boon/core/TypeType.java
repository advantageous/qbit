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

package io.advantageous.boon.core;

import java.util.*;

public enum TypeType {


    //PRIMITIVE
    BOOLEAN(false, true), BYTE(false, true), SHORT(false, true), CHAR(false, true),
    INT(false, true),  FLOAT(false, true), LONG(false, true), DOUBLE(false, true),

    //Wrappers
    LONG_WRAPPER(LONG), INTEGER_WRAPPER(INT), SHORT_WRAPPER(SHORT),
    CHAR_WRAPPER(CHAR), BOOLEAN_WRAPPER(BOOLEAN),
    BYTE_WRAPPER(BYTE), FLOAT_WRAPPER(FLOAT), DOUBLE_WRAPPER(DOUBLE),


    //Concepts
    TRUE(BOOLEAN), FALSE(BOOLEAN), INSTANCE, NULL,
    INTERFACE, ABSTRACT, SYSTEM, VOID, UNKNOWN, BASIC_TYPE,

    //BASE
    CHAR_SEQUENCE, NUMBER, OBJECT, CLASS, ENUM,


    //BASIC TYPES 1st Class
    STRING(CHAR_SEQUENCE), CALENDAR, DATE,


    //SECOND TIER BASIC TYPES
    URL(BASIC_TYPE), URI(BASIC_TYPE), LOCALE(BASIC_TYPE),
    TIME_ZONE(BASIC_TYPE), CURRENCY(BASIC_TYPE),
    FILE(BASIC_TYPE), PATH(BASIC_TYPE), UUID(BASIC_TYPE),



     //Numeric
     BIG_INT(NUMBER), BIG_DECIMAL(NUMBER),

    //COLLECTIONS
    COLLECTION, LIST(COLLECTION), SET(COLLECTION),
    MAP,
    MAP_STRING_OBJECT(MAP),

    ARRAY(true),
    ARRAY_INT(true, INT),
    ARRAY_BYTE(true, SHORT),
    ARRAY_SHORT(true, SHORT),
    ARRAY_FLOAT(true, FLOAT),
    ARRAY_DOUBLE(true, DOUBLE),
    ARRAY_LONG(true, LONG),
    ARRAY_STRING(true, STRING),
    ARRAY_OBJECT(true, OBJECT),



    //BOON
    VALUE_MAP, VALUE;


    final TypeType baseTypeOrWrapper;
    private final boolean array;
    private final boolean primitive;

    TypeType() {
        baseTypeOrWrapper =null;
        array=false;
        primitive=false;
    }


    TypeType(TypeType type) {
        baseTypeOrWrapper =type;
        array=false;
        primitive=false;

    }

    TypeType(boolean isarray) {
        this.array = isarray;
        baseTypeOrWrapper=null;
        primitive=false;

    }


    TypeType(boolean isarray, TypeType type) {
        this.array = isarray;
        baseTypeOrWrapper=type;
        primitive=false;

    }

    TypeType(boolean array, boolean primitive) {
        this.array = array;
        this.primitive = primitive;
        baseTypeOrWrapper = null;
    }

    public  static TypeType getInstanceType ( Object object ) {


             if (object == null) {
                 return NULL;
             } else {
                 return getType(object.getClass (), object);
             }
    }



    public static TypeType getType ( Class<?> clazz ) {
        return getType(clazz, null);
    }

    public static TypeType getType ( Class<?> clazz, Object object ) {

        final String className = clazz.getName();
        TypeType type =  getType( className );

        if (type != UNKNOWN) {
            return type;
        }

        if ( clazz.isInterface() ) {
            type = INTERFACE;
        } else if (clazz.isEnum()) {
            type = ENUM;
        } else if (clazz.isArray()) {
            type = getArrayType(clazz);
        } else if (Typ.isAbstract(clazz)) {
            type = ABSTRACT;
        } else if ( className.startsWith("java")) {
            if ( Typ.isCharSequence ( clazz ) ) {
                type = CHAR_SEQUENCE;
            } else if (Typ.isCollection ( clazz )) {
                if (Typ.isList ( clazz )) {
                    type = LIST;
                } else if (Typ.isSet ( clazz )) {
                    type = SET;
                } else {
                    type = COLLECTION;
                }
            } else if (Typ.isMap ( clazz )) {
                type = MAP;
            }
            else {
                type = SYSTEM;
            }
        } else if (className.startsWith("com.sun") || className.startsWith("sun.")) {
            type = SYSTEM;
        } else if (object !=null) {


            if (object instanceof Map) {
                type = MAP;
            } else if (object instanceof Collection) {

                type = COLLECTION;
                if (object instanceof List) {
                    type = LIST;
                } else if (object instanceof Set) {
                    type = SET;
                }
            } else {
                type = INSTANCE;
            }

        } else {
            type = INSTANCE;
        }

        return type;



    }

    private static TypeType getArrayType(Class<?> clazz) {
        TypeType type;
        final TypeType componentType = getType(clazz.getComponentType());
        switch(componentType) {


            case BYTE:
                type = ARRAY_BYTE;
                break;

            case SHORT:
                type = ARRAY_SHORT;
                break;

            case INT:
                type = ARRAY_INT;
                break;

            case FLOAT:
                type = ARRAY_FLOAT;
                break;

            case DOUBLE:
                type = ARRAY_DOUBLE;
                break;

            case LONG:
                type = ARRAY_LONG;
                break;

            case STRING:
                type = ARRAY_STRING;
                break;

            case OBJECT:
                type = ARRAY_OBJECT;
                break;

            default:
                type = ARRAY;
                break;

        }
        return type;
    }

    public static TypeType getType ( String typeName ) {

            switch ( typeName ) {
                case "int":
                    return TypeType.INT;
                case "short":
                    return TypeType.SHORT;
                case "byte":
                    return TypeType.BYTE;
                case "float":
                    return TypeType.FLOAT;
                case "double":
                    return TypeType.DOUBLE;
                case "boolean":
                    return TypeType.BOOLEAN;
                case "char":
                    return TypeType.CHAR;
                case "long":
                    return TypeType.LONG;

                case "java.lang.String":
                    return TypeType.STRING;
                case "java.lang.Boolean":
                    return TypeType.BOOLEAN_WRAPPER;
                case "java.lang.Byte":
                    return TypeType.BYTE_WRAPPER;
                case "java.lang.Short":
                    return TypeType.SHORT_WRAPPER;
                case "java.lang.Integer":
                    return TypeType.INTEGER_WRAPPER;
                case "java.lang.Double":
                    return TypeType.DOUBLE_WRAPPER;
                case "java.lang.Float":
                    return TypeType.FLOAT_WRAPPER;
                case "java.lang.Character":
                    return TypeType.CHAR_WRAPPER;
                case "java.lang.Number":
                    return TypeType.NUMBER;

                case "java.lang.Class":
                    return TypeType.CLASS;




                case "java.lang.Void":
                    return TypeType.VOID;





                case "java.lang.Long":
                    return TypeType.LONG_WRAPPER;


                case "java.util.Set":
                case "java.util.HashSet":
                case "java.util.TreeSet":
                    return TypeType.SET;

                case "java.util.List":
                case "java.util.ArrayList":
                case "java.util.LinkedList":
                case "ValueList":
                    return TypeType.LIST;

                case "java.util.Map":
                case "io.advantageous.boon.collections.LazyMap":
                case "java.util.HashMap":
                case "java.util.LinkedHashMap":
                case "java.util.TreeMap":
                case "io.advantageous.boon.core.value.LazyValueMap":
                    return TypeType.MAP;

                case "java.lang.CharSequence":
                    return TypeType.CHAR_SEQUENCE;

                case "java.math.BigDecimal":
                    return TypeType.BIG_DECIMAL;
                case "java.math.BigInteger":
                    return TypeType.BIG_INT;

                case "java.util.Date":
                case "java.sql.Date":
                case "java.sql.Time":
                case "java.sql.Timestamp":
                    return TypeType.DATE;



                case "java.util.Calendar":
                    return TypeType.CALENDAR;

                case "ValueMapImpl":
                    return TypeType.VALUE_MAP;

                case "io.advantageous.boon.core.value.NumberValue":
                case "io.advantageous.boon.core.value.CharSequenceValue":
                    return TypeType.VALUE;


                case "java.lang.Object":
                    return TypeType.OBJECT;

                case "java.io.File":
                    return TypeType.FILE;

                case "java.net.URI":
                    return TypeType.URI;

                case "java.net.URL":
                    return TypeType.URL;

                case "java.nio.file.Path":
                    return TypeType.PATH;

                case "java.util.UUID":
                    return TypeType.UUID;


                case "java.util.Locale":
                    return TypeType.LOCALE;


                case "java.util.TimeZone":
                    return TypeType.TIME_ZONE;

                case "java.util.Currency":
                    return TypeType.CURRENCY;

            }
            return TypeType.UNKNOWN;

    }


    public boolean  hasLength (  ) {

        switch ( this ) {
            case LIST:
            case MAP:
            case STRING:
            case CHAR_SEQUENCE:
            case SET:
            case COLLECTION:
                return true;
            default:
                return this.isArray() || this.isCollection();
        }
    }

    public  boolean isCollection (  ) {

        switch ( this ) {
            case LIST:
            case SET:
            case COLLECTION:
                return true;
            default:
                return false;
        }
    }



    public  static List<Object> gatherTypes ( List<?> list ) {

        List<Object> types = new ArrayList<>();

        for (Object o : list) {
            if (o instanceof List) {
                types.add(gatherTypes((List) o));
            }
            else {
                types.add(TypeType.getInstanceType(o));
            }
        }

        return types;
    }



    public  static List<Object> gatherActualTypes ( List<?> list ) {

        List<Object> types = new ArrayList<>();

        for (Object o : list) {
            if (o instanceof List) {
                types.add(gatherActualTypes((List) o));
            }
            else {
                types.add(TypeType.getActualType(o));
            }
        }

        return types;
    }

    private static Object getActualType(Object o) {
        if (o == null) {
            return NULL;
        } else {
            return o.getClass().getSimpleName();
        }
    }

    public  static List<TypeType> gatherTypes ( Object... list ) {

        List<TypeType> types = new ArrayList();

        for (Object o : list) {
            types.add(TypeType.getInstanceType(o)) ;
        }

        return types;
    }

    public TypeType wraps() {
        return baseTypeOrWrapper;
    }


    public TypeType componentType() {
        return baseTypeOrWrapper == null ? OBJECT : baseTypeOrWrapper;
    }


    public boolean isArray() {
        return array;
    }


    public boolean isPrimitive() {
        return primitive;
    }
}
