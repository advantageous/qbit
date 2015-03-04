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

package io.advantageous.boon.json.implementation;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.json.*;
import io.advantageous.boon.IO;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ObjectMapperImpl implements ObjectMapper {


    private final JsonParserFactory parserFactory;
    private final JsonSerializerFactory serializerFactory;

    public ObjectMapperImpl (JsonParserFactory parserFactory, JsonSerializerFactory serializerFactory) {

        this.parserFactory = parserFactory;
        this.serializerFactory = serializerFactory;

    }

    public ObjectMapperImpl () {

        this.parserFactory = new JsonParserFactory();
        this.serializerFactory = new JsonSerializerFactory();

        this.serializerFactory.useFieldsOnly();

    }


    @Override
    public <T> T readValue( final String src, final Class<T> valueType ) {

        return this.parserFactory.create().parse( valueType, src );
    }

    @Override
    public <T> T readValue( File src, Class<T> valueType ) {

        return this.parserFactory.create().parseFile( valueType, src.toString() );
    }

    @Override
    public <T> T readValue( byte[] src, Class<T> valueType ) {
        return this.parserFactory.create().parse( valueType, src );
    }

    @Override
    public <T> T readValue( char[] src, Class<T> valueType ) {
        return this.parserFactory.create().parse( valueType, src );
    }

    @Override
    public <T> T readValue( Reader src, Class<T> valueType ) {
        return this.parserFactory.create().parse( valueType, src );
    }

    @Override
    public <T> T readValue( InputStream src, Class<T> valueType ) {
        return this.parserFactory.create().parse( valueType, src );
    }

    @Override
    public <T extends Collection<C>, C> T readValue( String src, Class<T> valueType, Class<C> componentType ) {

        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( File src, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseListFromFile( componentType, src.toString() );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseListFromFile( componentType, src.toString() ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseListFromFile( componentType, src.toString() ));
        } else {
            return(T) this.parserFactory.create().parseListFromFile( componentType, src.toString() );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( byte[] src, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( char[] src, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( Reader src, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( InputStream src, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( byte[] src, Charset charset, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src, charset );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src, charset ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src, charset ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src, charset );
        }
    }

    @Override
    public <T extends Collection<C>, C> T readValue( InputStream src, Charset charset, Class<T> valueType, Class<C> componentType ) {
        Class<?> type = valueType;
        if (type == List.class) {
            return(T) this.parserFactory.create().parseList( componentType, src, charset );
        } else if (type == Set.class) {
            return (T) new HashSet(this.parserFactory.create().parseList( componentType, src, charset ));
        } else if (type == LinkedHashSet.class) {
            return (T) new LinkedHashSet<>(this.parserFactory.create().parseList( componentType, src, charset ));
        } else {
            return(T) this.parserFactory.create().parseList( componentType, src, charset );
        }
    }

    @Override
    public void writeValue( File dest, Object value ) {
        IO.write( IO.path( dest.toString() ), serializerFactory.create().serialize( value ).toString());
    }

    @Override
    public void writeValue( OutputStream dest, Object value ) {

        IO.writeNoClose( dest, serializerFactory.create().serialize( value ).toString() );
    }

    @Override
    public void writeValue( Writer dest, Object value ) {

        char [] chars =  serializerFactory.create().serialize( value ).toCharArray();

        try {
            dest.write( chars );
        } catch ( IOException e ) {
            Exceptions.handle(e);
        }
    }

    @Override
    public String writeValueAsString( Object value ) {
        return serializerFactory.create().serialize( value ).toString();
    }

    @Override
    public char[] writeValueAsCharArray( Object value ) {
        return serializerFactory.create().serialize( value ).toCharArray();
    }

    @Override
    public byte[] writeValueAsBytes( Object value ) {
        return serializerFactory.create().serialize( value ).toString().getBytes( StandardCharsets.UTF_8 );
    }

    @Override
    public byte[] writeValueAsBytes( Object value, Charset charset ) {
        return serializerFactory.create().serialize( value ).toString().getBytes( charset );
    }

    @Override
    public JsonParserAndMapper parser() {
        return parserFactory.create();
    }

    @Override
    public JsonSerializer serializer() {
        return serializerFactory.create();
    }

    @Override
    public String toJson( Object value ) {
        return this.writeValueAsString ( value );
    }

    @Override
    public void toJson( Object value, Appendable appendable ) {
        try {
            appendable.append ( this.writeValueAsString ( value )  );
        } catch ( IOException e ) {
            Exceptions.handle ( e );
        }
    }

    @Override
    public <T> T fromJson( String json, Class<T> clazz ) {
            return readValue ( json, clazz );
    }

    @Override
    public <T> T fromJson( byte[] bytes, Class<T> clazz ) {
        return readValue ( bytes, clazz );
    }

    @Override
    public <T> T fromJson( char[] chars, Class<T> clazz ) {
        return readValue ( chars, clazz );
    }

    @Override
    public <T> T fromJson( Reader reader, Class<T> clazz ) {
        return readValue ( reader, clazz );
    }

    @Override
    public <T> T fromJson( InputStream inputStream, Class<T> clazz ) {
        return readValue ( inputStream, clazz );
    }

    @Override
    public Object fromJson( String json ) {
        return parser().parse(json);
    }

    @Override
    public Object fromJson( Reader reader ) {
        return parser().parse(reader);
    }

    @Override
    public Object fromJson( byte[] bytes ) {
        return parser().parse(bytes);
    }

    @Override
    public Object fromJson( char[] chars ) {
        return parser().parse(chars);
    }

    @Override
    public Object fromJson( InputStream reader ) {
        return parser().parse(reader);
    }
}
