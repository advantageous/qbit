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

package io.advantageous.boon.json.serializers.impl;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.core.reflection.fields.FieldsAccessorFieldThenProp;
import io.advantageous.boon.json.serializers.*;
import io.advantageous.boon.core.reflection.fields.FieldsAccessor;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Collection;
import java.util.Date;
import java.util.Map;


/**
 * JSON serializer
 * Created by rick on 1/1/14.
 */
public class JsonSerializerImpl implements JsonSerializerInternal {

    private final ObjectSerializer objectSerializer;
    private final StringSerializer stringSerializer;
    private final MapSerializer mapSerializer;
    private final FieldSerializer fieldSerializer;

    private final InstanceSerializer instanceSerializer;
    private final CollectionSerializer collectionSerializer;
    private final ArraySerializer arraySerializer;
    private final UnknownSerializer unknownSerializer;
    private final DateSerializer dateSerializer;
    private final FieldsAccessor fieldsAccessor;


    private CharBuf builder = CharBuf.create( 4000 );

    public JsonSerializerImpl ()  {

        instanceSerializer = new InstanceSerializerImpl ();
        objectSerializer = new BasicObjectSerializerImpl (false, true);
        stringSerializer = new StringSerializerImpl (true, false);
        mapSerializer = new MapSerializerImpl (false);
        fieldSerializer = new FieldSerializerImpl ();
        collectionSerializer = new CollectionSerializerImpl ();
        arraySerializer = (ArraySerializer) collectionSerializer;
        unknownSerializer = new UnknownSerializerImpl ();
        dateSerializer = new DateSerializerImpl ();
        fieldsAccessor = new FieldsAccessorFieldThenProp(true);

    }

    public JsonSerializerImpl ( final ObjectSerializer objectSerializer,
                                final StringSerializer stringSerializer,
                                final MapSerializer mapSerializer,
                                final FieldSerializer fieldSerializer,
                                final InstanceSerializer instanceSerializer,
                                final CollectionSerializer collectionSerializer,
                                final ArraySerializer arraySerializer,
                                final UnknownSerializer unknownSerializer,
                                final DateSerializer dateSerializer,
                                final FieldsAccessor fieldsAccessor,
                                final boolean asAscii

    ) {




        if (fieldsAccessor == null) {
            this.fieldsAccessor = new FieldsAccessorFieldThenProp (true);
        } else {
            this.fieldsAccessor = fieldsAccessor;
        }


        if (dateSerializer == null) {
            this.dateSerializer = new DateSerializerImpl ();
        } else {
            this.dateSerializer = dateSerializer;
        }

        if (unknownSerializer == null) {
            this.unknownSerializer = new UnknownSerializerImpl();
        } else {
            this.unknownSerializer = unknownSerializer;
        }


        if (arraySerializer == null) {
            this.arraySerializer = new CollectionSerializerImpl ();
        } else {
            this.arraySerializer = arraySerializer;
        }

        if (collectionSerializer == null) {
            this.collectionSerializer = new CollectionSerializerImpl ();
        } else {
            this.collectionSerializer = collectionSerializer;
        }


        if (instanceSerializer == null) {
            this.instanceSerializer = new InstanceSerializerImpl ();
        } else {
            this.instanceSerializer = instanceSerializer;
        }

        if (objectSerializer == null) {
            this.objectSerializer = new BasicObjectSerializerImpl (false, true);
        } else {
            this.objectSerializer = objectSerializer;
        }

        if (stringSerializer == null) {
            this.stringSerializer = new StringSerializerImpl (true, asAscii);
        } else {
            this.stringSerializer = stringSerializer;
        }

        if (mapSerializer == null) {
            this.mapSerializer = new MapSerializerImpl(false);
        } else {
            this.mapSerializer = mapSerializer;
        }

        if (fieldSerializer == null) {
            this.fieldSerializer = new FieldSerializerImpl();
        } else {
            this.fieldSerializer = fieldSerializer;
        }

    }







    public final CharBuf serialize( Object obj ) {

        builder.readForRecycle ();
        try {
            serializeObject( obj, builder );
        } catch ( Exception ex ) {
            return Exceptions.handle(CharBuf.class, "unable to serializeObject", ex);
        }
        return builder;
    }


    public final boolean serializeField ( Object parent, FieldAccess fieldAccess, CharBuf builder )  {

        return fieldSerializer.serializeField ( this, parent, fieldAccess, builder );
    }

    public  final void serializeObject( Object obj, CharBuf builder )  {

        objectSerializer.serializeObject ( this, obj, builder );

    }

    public final  void serializeString( String str, CharBuf builder ) {
        this.stringSerializer.serializeString ( this, str, builder );
    }


    public final void serializeMap( Map<Object, Object> map, CharBuf builder )  {
        this.mapSerializer.serializeMap ( this, map, builder );

    }

    public final void serializeCollection( Collection<?> collection, CharBuf builder )  {

        this.collectionSerializer.serializeCollection ( this, collection, builder );
    }



    public final void serializeArray( Object obj, CharBuf builder ) {
        this.arraySerializer.serializeArray ( this, obj, builder );
    }



    public final void serializeUnknown ( Object obj, CharBuf builder ) {
        this.unknownSerializer.serializeUnknown ( this, obj, builder );
    }

    public final void serializeDate ( Date date, CharBuf builder ) {

        this.dateSerializer.serializeDate ( this, date, builder );

    }





    public final void serializeInstance ( Object obj, CharBuf builder )  {
           this.instanceSerializer.serializeInstance ( this, obj, builder );

    }

    @Override
    public void serializeInstance(Object obj, CharBuf builder, boolean includeTypeInfo) {
        this.instanceSerializer.serializeInstance ( this, obj, builder, includeTypeInfo );

    }

    @Override
    public void serializeSubtypeInstance( Object obj, CharBuf builder ) {
        this.instanceSerializer.serializeSubtypeInstance ( this, obj, builder );
    }

    public final Map<String, FieldAccess> getFields ( Class<? extends Object> aClass ) {
        return fieldsAccessor.getFields ( aClass );
    }


    @Override
    public void serialize(CharBuf builder, Object obj) {

        try {
            serializeObject( obj, builder );
        } catch ( Exception ex ) {
            Exceptions.handle("unable to serializeObject", ex);
        }
    }

}

