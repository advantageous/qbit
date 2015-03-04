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

import io.advantageous.boon.core.reflection.fields.*;
import io.advantageous.boon.json.serializers.*;
import io.advantageous.boon.json.serializers.impl.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by rick on 12/26/13.
 */
public class JsonSerializerFactory {

    private boolean outputType = false;

    private FieldAccessMode fieldAccessType = FieldAccessMode.FIELD;
    private boolean includeNulls = false;
    private boolean useAnnotations = false;
    private boolean includeEmpty = false;
    private boolean jsonFormatForDates = false;
    private boolean handleSimpleBackReference = true;
    private boolean handleComplexBackReference = false;
    private boolean includeDefault = false;
    private boolean cacheInstances = true;
    private boolean encodeStrings = true;
    private boolean serializeAsSupport = true;
    private boolean asciiOnly = true;
    private String view;

    private List<FieldFilter> filterProperties = null;
    private List<CustomFieldSerializer> customFieldSerializers = null;
    private Map<Class, CustomObjectSerializer> customObjectSerializers = null;


    public JsonSerializer create() {

        if ( !outputType && !includeEmpty && !includeNulls && !useAnnotations &&
                !jsonFormatForDates && handleSimpleBackReference &&
                !handleComplexBackReference && !includeDefault && filterProperties == null
                && customFieldSerializers == null && customObjectSerializers == null &&
                fieldAccessType == FieldAccessMode.FIELD) {
            return new JsonSimpleSerializerImpl (view, encodeStrings, serializeAsSupport, asciiOnly);
        } else {

            InstanceSerializer instanceSerializer;
            CollectionSerializer collectionSerializer;
            ArraySerializer arraySerializer;
            UnknownSerializer unknownSerializer;
            DateSerializer dateSerializer;
            FieldsAccessor fieldsAccessor;

            ObjectSerializer objectSerializer;
            StringSerializer stringSerializer;
            MapSerializer mapSerializer;
            FieldSerializer fieldSerializer;


            instanceSerializer = new InstanceSerializerImpl();

            if (customObjectSerializers != null ) {

                objectSerializer = new CustomObjectSerializerImpl(outputType, customObjectSerializers, includeNulls);
            } else {
                objectSerializer = new BasicObjectSerializerImpl(includeNulls, outputType);
            }


            stringSerializer = new StringSerializerImpl (encodeStrings, asciiOnly);
            mapSerializer = new MapSerializerImpl (includeNulls);

            if ( useAnnotations || includeNulls || includeEmpty || handleComplexBackReference
                    || !includeDefault || view!=null) {
                fieldSerializer = new FieldSerializerUseAnnotationsImpl(
                        includeNulls,
                        includeDefault, useAnnotations,
                        includeEmpty, handleSimpleBackReference,
                        handleComplexBackReference,
                        customObjectSerializers,
                        filterProperties,
                        null,
                        customFieldSerializers,
                        view);
            } else {
                fieldSerializer = new FieldSerializerImpl();
            }
            collectionSerializer = new CollectionSerializerImpl();
            arraySerializer = ( ArraySerializer ) collectionSerializer;
            unknownSerializer = new UnknownSerializerImpl ();

            if (jsonFormatForDates) {
                dateSerializer = new JsonDateSerializer();
            } else {
                dateSerializer = new DateSerializerImpl();
            }



            switch ( fieldAccessType )  {
                case FIELD:
                    fieldsAccessor = new FieldFieldsAccessor( useAnnotations );
                    break;
                case PROPERTY:
                    fieldsAccessor = new PropertyFieldAccessor( useAnnotations );
                    break;
                case FIELD_THEN_PROPERTY:
                    fieldsAccessor = new FieldsAccessorFieldThenProp( useAnnotations );
                    break;
                case PROPERTY_THEN_FIELD:
                    fieldsAccessor = new FieldsAccessorsPropertyThenField( useAnnotations );
                    break;
                default:
                    fieldsAccessor = new FieldFieldsAccessor( useAnnotations );

            }

            return new JsonSerializerImpl (
                    objectSerializer,
                    stringSerializer,
                    mapSerializer,
                    fieldSerializer,
                    instanceSerializer,
                    collectionSerializer,
                    arraySerializer,
                    unknownSerializer,
                    dateSerializer,
                    fieldsAccessor,
                    asciiOnly
            );
        }

    }


    public JsonSerializerFactory addFilter ( FieldFilter filter ) {
        if ( filterProperties == null ) {
            filterProperties = new CopyOnWriteArrayList<> ();
        }
        filterProperties.add ( filter );
        return this;
    }

    public JsonSerializerFactory addPropertySerializer ( CustomFieldSerializer serializer ) {
        if ( customFieldSerializers == null ) {
            customFieldSerializers = new CopyOnWriteArrayList<> ();
        }
        customFieldSerializers.add ( serializer );
        return this;
    }

    public JsonSerializerFactory addTypeSerializer ( Class<?> type, CustomObjectSerializer serializer ) {

        if ( customObjectSerializers == null ) {
            customObjectSerializers = new ConcurrentHashMap<> ();
        }
        customObjectSerializers.put ( type, serializer );
        return this;
    }

    public boolean isOutputType () {
        return outputType;
    }

    public JsonSerializerFactory setOutputType ( boolean outputType ) {
        this.outputType = outputType;
        return this;
    }



    public JsonSerializerFactory outputType (  ) {
        this.outputType = true;
        return this;
    }

    public boolean isUsePropertiesFirst () {
        return fieldAccessType == FieldAccessMode.PROPERTY_THEN_FIELD;
    }


    public JsonSerializerFactory usePropertiesFirst () {
        fieldAccessType = FieldAccessMode.PROPERTY_THEN_FIELD;
        return this;
    }

    public boolean isUseFieldsFirst () {
        return this.fieldAccessType == FieldAccessMode.FIELD_THEN_PROPERTY;

    }


    public JsonSerializerFactory useFieldsFirst () {
        this.fieldAccessType  = FieldAccessMode.FIELD_THEN_PROPERTY;
        return this;
    }


    public JsonSerializerFactory useFieldsOnly () {
        this.fieldAccessType  = FieldAccessMode.FIELD;
        return this;
    }



    public JsonSerializerFactory usePropertyOnly () {
        this.fieldAccessType  = FieldAccessMode.PROPERTY;
        return this;
    }

    public boolean isIncludeNulls () {
        return includeNulls;
    }

    public JsonSerializerFactory setIncludeNulls ( boolean includeNulls ) {
        this.includeNulls = includeNulls;
        return this;
    }


    public boolean isAsciiOnly () {
        return asciiOnly;
    }


    public JsonSerializerFactory setAsciiOnly ( boolean asciiOnly ) {
        this.asciiOnly = asciiOnly;
        return this;
    }

    public JsonSerializerFactory asciiOnly (  ) {
        this.asciiOnly = true;
        return this;
    }

    public JsonSerializerFactory includeNulls () {
        this.includeNulls = true;
        return this;
    }

    public boolean isUseAnnotations () {
        return useAnnotations;
    }

    public JsonSerializerFactory setUseAnnotations ( boolean useAnnotations ) {
        this.useAnnotations = useAnnotations;
        return this;
    }


    public JsonSerializerFactory useAnnotations () {
        this.useAnnotations = true;
        return this;
    }


    public boolean isIncludeEmpty () {
        return includeEmpty;
    }

    public JsonSerializerFactory setIncludeEmpty ( boolean includeEmpty ) {
        this.includeEmpty = includeEmpty;
        return this;
    }


    public JsonSerializerFactory includeEmpty () {
        this.includeEmpty = true;
        return this;
    }

    public boolean isHandleSimpleBackReference () {
        return handleSimpleBackReference;
    }

    public JsonSerializerFactory setHandleSimpleBackReference ( boolean handleSimpleBackReference ) {
        this.handleSimpleBackReference = handleSimpleBackReference;
        return this;
    }

    public boolean isHandleComplexBackReference () {
        return handleComplexBackReference;
    }

    public JsonSerializerFactory setHandleComplexBackReference ( boolean handleComplexBackReference ) {
        this.handleComplexBackReference = handleComplexBackReference;
        return this;
    }


    public JsonSerializerFactory handleComplexBackReference () {
        this.handleComplexBackReference = true;
        return this;
    }


    public boolean isJsonFormatForDates () {
        return jsonFormatForDates;
    }

    public JsonSerializerFactory setJsonFormatForDates ( boolean jsonFormatForDates ) {
        this.jsonFormatForDates = jsonFormatForDates;
        return this;
    }


    public JsonSerializerFactory useJsonFormatForDates () {
        this.jsonFormatForDates = true;
        return this;
    }


    public boolean isIncludeDefault () {
        return includeDefault;
    }

    public JsonSerializerFactory setIncludeDefault ( boolean includeDefault ) {
        this.includeDefault = includeDefault;
        return this;
    }


    public JsonSerializerFactory includeDefaultValues () {
        this.includeDefault = true;
        return this;
    }


    public boolean isCacheInstances () {
        return cacheInstances;
    }

    public JsonSerializerFactory setCacheInstances ( boolean cacheInstances ) {
        this.cacheInstances = cacheInstances;
        return this;
    }


    public JsonSerializerFactory usedCacheInstances () {
        this.cacheInstances = true;
        return this;
    }

    public String getView() {
        return view;
    }

    public JsonSerializerFactory setView( String view ) {
        this.view = view;
        return this;
    }

    public boolean isEncodeStrings() {
        return encodeStrings;
    }

    public JsonSerializerFactory setEncodeStrings(boolean encodeStrings) {
        this.encodeStrings = encodeStrings;
        return this;
    }

    public boolean isSerializeAsSupport() {
        return serializeAsSupport;
    }

    public JsonSerializerFactory setSerializeAsSupport(boolean serializeAsSupport) {
        this.serializeAsSupport = serializeAsSupport;
        return this;
    }
}
