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

package io.advantageous.boon;


import io.advantageous.boon.core.reflection.*;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.primitive.CharBuf;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.boon.Lists.toListOrSingletonList;
import static io.advantageous.boon.Maps.fromMap;

/**
 * This class contains some utility methods and acts as facade
 * over the most popular Boon features.
 */
public class Boon {


    /**
     * Sets where Boon looks for config information.
     */
    public static final String BOON_SYSTEM_CONF_DIR = "BOON_SYSTEM_CONF_DIR";


    /**
     * Turns debugging on.
     */
    private static AtomicBoolean debug = new AtomicBoolean(false);



    /**
     * Checks to see if two objects are equal.
     *
     * @param a a
     * @param b b
     * @return eq?
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Prints a simple message to the console.
     *
     * @param message string to print.
     */
    public static void println(String message) {
        Sys.println(message);
    }

    /**
     * Adds a newline to the console.
     */
    public static void println() {
        Sys.println("");
    }

    /**
     * Prints an object to the console.
     *
     * @param message object to print.
     */
    public static void println(Object message) {

        print(message);
        println();
    }

    /**
     * Prints to console.
     *
     * @param message message
     */
    public static void print(String message) {
        Sys.print(message);
    }

    /**
     * Print a single object to the console.
     * If null prints out &gt;NULL&lt;
     * If char[] converts to String.
     * If array prints out string version of array
     * by first converting array to a list.
     * If any object, then it uses the toString to print out the object.
     *
     * @param message the object that you wish to print.
     */
    public static void print(Object message) {

        if (message == null) {
            print("<NULL>");
        } else if (message instanceof char[]) {
            print(FastStringUtils.noCopyStringFromChars((char[]) message));
        } else if (message.getClass().isArray()) {
            print(toListOrSingletonList(message).toString());
        } else {
            print(message.toString());
        }
    }

    /**
     * Like print, but prints out a whole slew of objects on the same line.
     *
     * @param messages objects you want to print on the same line.
     */
    public static void puts(Object... messages) {

        for (Object message : messages) {
            print(message);
            if (!(message instanceof Terminal.Escape)) print(' ');
        }
        println();

    }




    /**
     * <p>
     * Like puts but prints out each object on its own line.
     * If the object is a list or array,
     * then each item in the list gets printed out on its own line.
     * </p>
     *
     * @param messages the stuff you want to print out.
     */
    public static void putl(Object... messages) {

        for (Object message : messages) {

            if (message instanceof Collection || Typ.isArray(message)) {
                Iterator iterator = Conversions.iterator(message);
                while (iterator.hasNext()) {
                    puts(iterator.next());
                }
                continue;
            }
            print(message);
            println();
        }
        println();

    }

    /**
     * like putl but writes to a string.
     *
     * @param messages the stuff you want to print out.
     * @return string
     */
    public static String sputl(Object... messages) {
        CharBuf buf = CharBuf.create(100);
        return sputl(buf, messages).toString();
    }

    /**
     * Like puts but writes to a String.
     *
     * @param messages the stuff you want to print out.
     * @return string
     */
    public static String sputs(Object... messages) {
        CharBuf buf = CharBuf.create(80);
        return sputs(buf, messages).toString();
    }


    /**
     * Writes to a char buf. A char buf is like a StringBuilder.
     *
     * @param buf      char buf
     * @param messages messages
     * @return charbuf
     */
    public static CharBuf sputl(CharBuf buf, Object... messages) {

        for (Object message : messages) {
            if (message == null) {
                buf.add("<NULL>");
            } else if (message.getClass().isArray()) {
                buf.add(toListOrSingletonList(message).toString());
            } else {
                buf.add(message.toString());
            }
            buf.add('\n');
        }
        buf.add('\n');

        return buf;


    }

    /**
     * Like puts but writes to a CharBuf.
     *
     * @param buf      char buf
     * @param messages messages to write.
     * @return string created.
     */
    public static CharBuf sputs(CharBuf buf, Object... messages) {

        int index = 0;
        for (Object message : messages) {
            if (index != 0) {
                buf.add(' ');
            }
            index++;

            if (message == null) {
                buf.add("<NULL>");
            } else if (message.getClass().isArray()) {
                buf.add(toListOrSingletonList(message).toString());
            } else {
                buf.add(message.toString());
            }
        }
        buf.add('\n');

        return buf;

    }

    public static StringBuilder sputs(StringBuilder buf, Object... messages) {

        int index = 0;
        for (Object message : messages) {
            if (index != 0) {
                buf.append(' ');
            }
            index++;

            if (message == null) {
                buf.append("<NULL>");
            } else if (message.getClass().isArray()) {
                buf.append(toListOrSingletonList(message).toString());
            } else {
                buf.append(message.toString());
            }
        }
        buf.append('\n');

        return buf;

    }

    public static boolean isArray(Object obj) {
        return Typ.isArray(obj);
    }

    public static boolean isStringArray(Object obj) {
        return Typ.isStringArray(obj);
    }

    public static int len(Object obj) {
        return Conversions.len(obj);
    }


    public static Iterator iterator(final Object o) {
        return Conversions.iterator(o);
    }

    /**
     * Join by for array.
     */
    public static String joinBy(char delim, Object... args) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        for (Object arg : args) {
            builder.add(arg.toString());
            if (!(index == args.length - 1)) {
                builder.add(delim);
            }
            index++;
        }
        return builder.toString();
    }

    /**
     * Join by for collection.
     */
    public static String joinBy(char delim, Collection<?> collection) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        int size = collection.size();
        for (Object arg : collection) {
            builder.add(arg.toString());
            if (!(index == size - 1)) {
                builder.add(delim);
            }
            index++;
        }
        return builder.toString();
    }


    /**
     * Join by for iterable.
     */
    public static String joinBy(char delim, Iterable<?> iterable) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        for (Object arg : iterable) {
            builder.add(arg.toString());
            builder.add(delim);
            index++;
        }
        if (index > 1) {
            builder.removeLastChar();
        }
        return builder.toString();
    }


    /**
     * Map by which is really contained in Lists
     *
     * @param objects  objects to map
     * @param function function to use for mapping
     * @return list
     */
    public static List<?> mapBy(Iterable<?> objects, Object function) {
        return Lists.mapBy(objects, function);
    }


    /**
     * Helper method to quickly convert a Java object into JSON.
     * Facade into the JSON system.
     *
     * @param value Java object
     * @return JSON-ified Java object
     */
    public static String toJson(Object value) {
        return JsonFactory.toJson(value);
    }


    /**
     * Helper method to quickly convert JSON into a Java object.
     * Facade into the JSON system.
     *
     * @param value JSON content
     * @return Java object
     */
    public static Object fromJson(String value) {
        return JsonFactory.fromJson(value);
    }

    /**
     * Helper method to quickly convert JSON into a Java object.
     * Facade into the JSON system.
     *
     * @param value JSON content
     * @param clazz type you want to convert the JSON to
     * @return Java object
     */
    public static <T> T fromJson(String value, Class<T> clazz) {
        return JsonFactory.fromJson(value, clazz);
    }

    /**
     * converts JSON into strongly typed list
     *
     * @param value value
     * @param clazz class
     * @param <T>   T
     * @return new list
     */
    public static <T> List<T> fromJsonArray(String value, Class<T> clazz) {
        return JsonFactory.fromJsonArray(value, clazz);
    }


    /**
     * Does path lookupWithDefault.
     * Facade over BeanUtils.
     *
     * @param value value to read
     * @param path  property path to read from value
     * @return value from property path
     */
    public static Object atIndex(Object value, String path) {
        return BeanUtils.idx(value, path);
    }


    /**
     * Gets input from console.
     *
     * @return String from console.
     */
    public static String gets() {
        Scanner console = new Scanner(System.in);
        String input = console.nextLine();
        return input.trim();
    }

    /**
     * Adds a bunch of Strings together.
     */
    public static String add(String... args) {
        return Str.add(args);
    }

    /**
     * Gets the string value of an object path.
     *
     * @param value object value
     * @param path  property path to read from value
     * @return string version of results
     */
    public static String stringAtIndex(Object value, String path) {
        return Conversions.toString(BeanUtils.idx(value, path));
    }


    /**
     * Facade method over Boon invoker system.
     * Allow you to easily invoke methods from Java objects using reflection.
     *
     * TODO change this to invoke missingMethod if the method is not found.
     * First arg is the name of the missing method.
     * (If missingMethod is implemented on the object value).
     *
     * @param value  object value
     * @param method method you want to invoke on the object value
     * @return results of object invocation.
     */
    public static Object call(Object value, String method) {
        if (value instanceof Class) {
            return Invoker.invoke((Class) value, method);
        } else {
            return Invoker.invoke(value, method);
        }
    }


    /**
     * Common helper method for string slice.
     *
     * @param string string you want to slice
     * @param start  start location
     * @param stop   end location
     * @return new sliced up string.
     */
    public static String sliceOf(String string, int start, int stop) {
        return Str.sliceOf(string, start, stop);
    }


    public static String sliceOf(String string, int start) {
        return Str.sliceOf(string, start);
    }


    public static String endSliceOf(String string, int end) {
        return Str.endSliceOf(string, end);
    }


    /**
     * Quickly grab a system property.
     *
     * @param propertyName property value
     * @param defaultValue default value if not found.
     * @return value of system property
     */
    public static String sysProp(String propertyName, Object defaultValue) {
        return Sys.sysProp(propertyName, defaultValue);
    }


    public static boolean hasSysProp(String propertyName) {
        return Sys.hasSysProp(propertyName);
    }


    public static void putSysProp(String propertyName, Object value) {
        Sys.putSysProp(propertyName, value);
    }


    /**
     * Press enter to continue. Used for console apps.
     *
     * @param pressEnterKeyMessage message
     */
    public static void pressEnterKey(String pressEnterKeyMessage) {
        puts(pressEnterKeyMessage);
        gets();
    }


    /**
     * Used by console apps.
     */
    public static void pressEnterKey() {
        puts("Press enter key to continue");
        gets();
    }

    /**
     * Checks to see if an object responds to a method.
     * Helper facade over Reflection library.
     *
     * @param object object in question
     * @param method method name in question.
     * @return true or false
     */
    public static boolean respondsTo(Object object, String method) {
        if (object instanceof Class) {
            return Reflection.respondsTo((Class) object, method);
        } else {
            return Reflection.respondsTo(object, method);
        }
    }


    /**
     * Loads a resource from the file system or classpath if not found.
     * This allows you to have resources that exist in the jar
     * and that can be configured outside of the jar easily.
     *
     * Classpath is only used if file system resource is not found.
     *
     * @param path path to resource
     * @return resource returned.
     */
    public static String resource(String path) {
        if (!IO.exists(IO.path(path))) {
            path = add("classpath:/", path);
        }

        String str = IO.read(path);
        return str;
    }


    /**
     * Loads a resource from the file system or classpath if not found.
     * This allows you to have resources that exist in the jar
     * and that can be configured outside of the jar easily.
     *
     * Classpath is only used if file system resource is not found.
     *
     * @param path path to resource
     * @return resource returned.
     */
    public static String resource(Path path) {
        String str = IO.read(path);
        return str;
    }








    /**
     * Load JSON object as resource
     * Looks in file system first and then classpath.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static Object jsonResource(String path) {
        if (!IO.exists(IO.path(path))) {
            path = add("classpath:/", path);
        }

        String str = IO.read(path);
        if (str != null) {
            return fromJson(str);
        }
        return null;
    }


    /**
     * Load JSON object as resource
     * Looks in file system.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static Object jsonResource(Path path) {

        String str = IO.read(path);
        if (str != null) {
            return fromJson(str);
        }
        return null;
    }





    /**
     * <p>
     * </p>
     * Looks in file system first and then classpath.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static Map<String, Object> resourceMap(String path) {
        return (Map<String, Object>) jsonResource(path);
    }


    /**
     * <p>
     * </p>
     * Looks in file system first.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static Map<String, Object> resourceMap(Path path) {
        return (Map<String, Object>) jsonResource(path);
    }



    /**
     * <p>
     * Load JSON list as resource.
     *
     * </p>
     * Looks in file system first and then classpath.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static <T> T resourceObject(String path, Class<T> type) {
        return fromMap(resourceMap(path), type);
    }


    /**
     * <p>
     * Load JSON object as resource.
     * </p>
     * Looks in file system.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static <T> T resourceObject(Path path, Class<T> type) {
        return fromMap(resourceMap(path), type);
    }



    /**
     * <p>
     * Load JSON list as resource .
     * </p>
     * Looks in file system first and then classpath.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static List<?> resourceList(String path) {
        return (List<?>) jsonResource(path);
    }


    /**
     * <p>
     * Load JSON list as resource .
     * </p>
     * Looks in file system first.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static List<?> resourceList(Path path) {
        return (List<?>) jsonResource(path);
    }


    /**
     * <p>
     * Load JSON list as resource .
     * </p>
     * Looks in file system first and then classpath.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static <T> List<T> resourceList(String path, Class<T> listOf) {

        List<Map> list = (List) jsonResource(path);

        return MapObjectConversion.convertListOfMapsToObjects(listOf, list);

    }


    /**
     * <p>
     * Load JSON list as resource .
     * </p>
     * Looks in file system.
     *
     * @param path path to resource
     * @return JSON object loaded as resource
     */
    public static <T> List<T> resourceList(Path path, Class<T> listOf) {

        List<Map> list = (List) jsonResource(path);

        return MapObjectConversion.convertListOfMapsToObjects(listOf, list);

    }




    /**
     * Gets class name from object.
     * It is null safe.
     *
     * @param object class name
     * @return class name of object
     */
    public static String className(Object object) {
        return object == null ? "CLASS<NULL>" : object.getClass().getName();
    }

    /**
     * Gets class name from object.
     * It is null safe.
     *
     * @param object class name
     * @return class name of object
     */
    public static Class<?> cls(Object object) {
        return object == null ? null : object.getClass();
    }

    /**
     * Gets simple class name from object.
     *
     * @param object object to get class name from
     * @return returns the class name
     */
    public static String simpleName(Object object) {
        return object == null ? "CLASS<NULL>" : object.getClass().getSimpleName();
    }



    /**
     * Checks to see if debugging is turned on.
     *
     * @return on?
     */
    public static boolean debugOn() {
        return debug.get();
    }


    /**
     * Turns debugging on.
     */
    public static void turnDebugOn() {
        debug.set(true);
    }


    /**
     * Turns debugging off.
     */
    public static void turnDebugOff() {
        debug.set(false);
    }



    public static boolean equalsOrDie(Object expected, Object got) {

        if (expected == null && got == null) {
            return true;
        }

        if (expected == null && got != null) Exceptions.die();
        if (!expected.equals(got)) Exceptions.die("Expected was", expected, "but we got", got);

        return true;
    }


    public static boolean equalsOrDie(String message, Object expected, Object got) {

        if (expected == null && got != null) Exceptions.die(message, "Expected was", expected, "but we got", got);
        if (!expected.equals(got)) Exceptions.die(message, "Expected was", expected, "but we got", got);

        return true;
    }

    public static String toPrettyJson(Object object) {

        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintObject(object, false, 0).toString();
    }


    public static String toPrettyJsonWithTypes(Object object) {

        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintObject(object, true, 0).toString();
    }

    public static boolean isEmpty(Object object) {
        return len(object) == 0;
    }
}