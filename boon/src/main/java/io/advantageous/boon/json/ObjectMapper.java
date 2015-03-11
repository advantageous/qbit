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

package io.advantageous.boon.json;


import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;



/**
 * This mapper (or, data binder, or codec) provides functionality for
 * converting between Java objects (instances of JDK provided core classes,
 * beans), and matching JSON constructs.
 * It will use instances of {@link JsonParserAndMapper} and {@link JsonSerializer}
 * for implementing actual reading/writing of JSON.
 *
 */
public interface ObjectMapper {

    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     * @param src soruce
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(String src, Class<T> valueType);

    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param src source
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(File src, Class<T> valueType);


    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param src source
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(byte[] src, Class<T> valueType);


    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param src source
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(char[] src, Class<T> valueType);


    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param src source
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(Reader src, Class<T> valueType);


    /**
     * Method to deserialize JSON content into a non-container
     * type typically a bean or wrapper type.
     *
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param src source
     * @param valueType value type
     * @param <T> T
     * @return T
     */
    <T> T readValue(InputStream src, Class<T> valueType);



    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(String src, Class<T> valueType, Class<C> componentType);
    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(File src, Class<T> valueType,
                                             Class<C> componentType);
    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(byte[] src, Class<T> valueType,
                                             Class<C> componentType);
    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(char[] src, Class<T> valueType,
                                             Class<C> componentType);
    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(Reader src, Class<T> valueType,
                                             Class<C> componentType);
    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(InputStream src, Class<T> valueType, Class<C> componentType);

    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param charset charset
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(byte[] src, Charset charset,
                                             Class<T> valueType,
                                             Class<C> componentType);

    /**
     * Method to deserialize JSON content into a container like Set or List.
     *
     * Note: this method should  be used if the result type is a
     * container ({@link java.util.Collection}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected without using this method.
     *
     * @param src source
     * @param valueType value type
     * @param componentType component type
     * @param charset charset
     * @param <T> T
     * @param <C> C
     * @return T
     */
    <T extends Collection<C>, C> T readValue(InputStream src, Charset charset,
                                             Class<T> valueType,
                                             Class<C> componentType);

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, written to File provided.
     * @param dest destination
     * @param value value
     */
    void writeValue(File dest, Object value);

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using output stream provided (using encoding
     * UTF8.
     *
     * Note: method does not close the underlying stream explicitly
     * here.
     *
     * @param dest destination
     * @param value value
     */
    public void writeValue(OutputStream dest, Object value);


    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using Writer provided.
     * Note: method does not close the underlying stream explicitly
     * here.
     * @param dest destination
     * @param value value
     */
    public void writeValue(Writer dest, Object value);


    /**
     * Method that can be used to serialize any Java value as
     * a String. Functionally equivalent to calling
     * {@link #writeValue(Writer,Object)} with {@link java.io.StringWriter}
     * and constructing String, but more efficient.
     * @param value value
     * @return value
     */
    public String writeValueAsString(Object value);




    /**
     * Method that can be used to serialize any Java value as
     * a char[]. Functionally equivalent to calling
     * {@link #writeValue(Writer,Object)} with {@link java.io.StringWriter}
     * and constructing String, but more efficient.
     * @param value value
     * @return value
     */
    public char[] writeValueAsCharArray(Object value);

    /**
     * Method that can be used to serialize any Java value as
     * a byte array.
     * Encoding used will be UTF-8.
     * @param value value
     * @return value
     */
    public byte[] writeValueAsBytes(Object value);

    /**
     * Method that can be used to serialize any Java value as
     * a byte array.
     * @param value value
     * @param charset charset
     * @return value
     */
    public byte[] writeValueAsBytes(Object value, Charset charset);




    public JsonParserAndMapper parser();

    public JsonSerializer serializer();



    public String toJson(Object value);
    public void toJson(Object value, Appendable appendable);
    public <T> T fromJson(String json, Class<T> clazz);
    public <T> T fromJson(byte[] bytes, Class<T> clazz);
    public <T> T fromJson(char[] chars, Class<T> clazz);
    public <T> T fromJson(Reader reader, Class<T> clazz);
    public <T> T fromJson(InputStream reader, Class<T> clazz);

    public Object fromJson(String json);
    public Object fromJson(Reader reader);
    public Object fromJson(byte[] bytes);
    public Object fromJson(char[] chars);
    public Object fromJson(InputStream reader);



}
