package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.qbit.meta.swagger.builders.DefinitionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefinitionClassCollector {

    private final Map<String, Definition> definitionMap = new HashMap<>();


    private final Logger logger = LoggerFactory.getLogger(DefinitionClassCollector.class);

    private final Map<Class, Schema> mappings = Maps.map(
            /* Adding common primitive and basic type mappings. */
            String.class, Schema.schema("string"),
            StringBuffer.class, Schema.schema("string"),
            Date.class, Schema.schemaWithFormat("string", "dateTime"),
            Integer.class, Schema.schemaWithFormat("integer", "int32"),
            int.class, Schema.schemaWithFormat("integer", "int32"),
            Long.class, Schema.schemaWithFormat("integer", "int64"),
            long.class, Schema.schemaWithFormat("integer", "int64"),
            Float.class, Schema.schemaWithFormat("number", "float"),
            float.class, Schema.schemaWithFormat("number", "float"),
            Double.class, Schema.schemaWithFormat("number", "double"),
            double.class, Schema.schemaWithFormat("number", "double"),
            Boolean.class, Schema.schemaWithFormat("boolean", ""),
            boolean.class, Schema.schemaWithFormat("boolean", ""),
            byte.class, Schema.schemaWithFormat("string", "byte"),
            Byte.class, Schema.schemaWithFormat("string", "byte")

    );


    {
        /* Adding common primitive and basic type arrays. */
        mappings.put(String[].class, Schema.array(mappings.get(String.class)));
        mappings.put(StringBuffer[].class, Schema.array(mappings.get(StringBuffer.class)));
        mappings.put(Date[].class, Schema.array(mappings.get(Date.class)));
        mappings.put(int[].class, Schema.array(mappings.get(int.class)));
        mappings.put(Integer[].class, Schema.array(mappings.get(Integer.class)));
        mappings.put(long[].class, Schema.array(mappings.get(long.class)));
        mappings.put(Long[].class, Schema.array(mappings.get(Long.class)));
        mappings.put(Double[].class, Schema.array(mappings.get(Double.class)));
        mappings.put(double[].class, Schema.array(mappings.get(double.class)));
        mappings.put(Float[].class, Schema.array(mappings.get(Float.class)));
        mappings.put(float[].class, Schema.array(mappings.get(float.class)));
        mappings.put(Boolean[].class, Schema.array(mappings.get(Boolean.class)));
        mappings.put(boolean[].class, Schema.array(mappings.get(boolean.class)));
        mappings.put(byte[].class, Schema.array(mappings.get(byte.class)));
        mappings.put(Byte[].class, Schema.array(mappings.get(Byte.class)));

    }


    public Schema getSchema(final Class<?> cls) {
        return getSchemaWithComponentClass(cls, null);
    }

    public Schema getSchemaWithComponentClass(final Class<?> cls, Class<?> componentClass) {

        Schema schema = mappings.get(cls);

        if (schema != null) {
            return schema;
        }

        if (cls != null) {
            TypeType type = TypeType.getType(cls);

            if (type.isArray()) {
                final Schema componentSchema = Schema.definitionRef(cls.getComponentType().getSimpleName(), "");
                return Schema.array(componentSchema, "");
            } else if (type.isCollection()) {


                if (componentClass != null) {
                    if (componentClass.getName().startsWith("java.lang")) {
                        final Schema schemaForComponent = mappings.get(componentClass);
                        return Schema.array(schemaForComponent, "");
                    } else {
                        return Schema.array(Schema.definitionRef(componentClass.getSimpleName(), ""), "");
                    }
                } else {
                    logger.info("Component class was null defaulting to string");
                    return Schema.array(Schema.definitionRef("string", ""), "");
                }
            }

            return Schema.definitionRef(cls.getSimpleName(), "");
        } else {
            return Schema.schema("string");
        }

    }

    public Schema getSchemaForJSend(final Class<?> cls, Class<?> componentClass) {
        return Schema.definitionRef("jsend-" + componentClass.getSimpleName(), "");

    }

    public void addJSendClass(final Class<?> cls) {
        addClass(cls);
        addJSendClass(ClassMeta.classMeta(cls));

    }

    public void addClass(final Class<?> cls) {

        /*
        Don't add void.
         */
        if (cls == void.class || cls == Void.class) {
            return;
        }

        /* If it is a common built in type, don't add. */
        if (mappings.containsKey(cls)) {
            return;
        }

        final ClassMeta<?> classMeta = ClassMeta.classMeta(cls);
        addClass(classMeta);
    }

    private void addClass(final ClassMeta<?> classMeta) {

        try {

            if (definitionMap.containsKey(classMeta.name())) {
                return;
            }

            definitionMap.put(classMeta.name(), null);

            final DefinitionBuilder definitionBuilder = new DefinitionBuilder();

            final String description = getDescription(classMeta);

            definitionBuilder.setDescription(description);

            final Map<String, FieldAccess> fieldAccessMap = classMeta.fieldMap();


            fieldAccessMap.entrySet().forEach(fieldAccessEntry -> {

                final FieldAccess fieldAccess = fieldAccessEntry.getValue();
                if (fieldAccess.ignore() || fieldAccess.isStatic()) {
                    return;
                }
                definitionBuilder.addProperty(fieldAccess.name(), convertFieldToSchema(fieldAccess));

            });

            final Definition definition = definitionBuilder.build();


            definitionMap.put(classMeta.name(), definition);

        } catch (Exception ex) {
            logger.warn("Unable to add class " + classMeta.longName(), ex);
        }
    }

    private void addJSendClass(final ClassMeta<?> classMeta) {

        try {

            if (definitionMap.containsKey("jsend-" + classMeta.name())) {
                return;
            }

            definitionMap.put("jsend-" + classMeta.name(), null);

            final DefinitionBuilder definitionBuilder = new DefinitionBuilder();


            definitionBuilder.setDescription("jsend standard response");

            Schema schema = mappings.get(classMeta.cls());


            if (schema != null) {
                definitionBuilder.addProperty("data", schema);
            } else {
                schema = convertFieldToDefinitionRef(classMeta);
                definitionBuilder.addProperty("data", schema);
            }
            definitionBuilder.addProperty("status", Schema.schemaWithDescription(mappings.get(String.class),
                    "Status of return, this can be 'success', 'fail' or 'error'"));

            final Definition definition = definitionBuilder.build();


            definitionMap.put("jsend-" + classMeta.name(), definition);

        } catch (Exception ex) {
            logger.warn("Unable to add class " + classMeta.longName(), ex);
        }
    }


    private Schema convertFieldToSchema(final FieldAccess fieldAccess) {

        try {

            final Class<?> type = fieldAccess.type();
            final Schema schema = mappings.get(type);

            final String description = getDescription(fieldAccess);

            if (schema != null) {

                if (description == null) {
                    return schema;
                } else {
                    return Schema.schemaWithDescription(schema, description);

                }
            }

            return convertFieldToComplexSchema(fieldAccess);
        } catch (Exception ex) {

            logger.warn("unable to convert field " + fieldAccess.name() + " from " + fieldAccess.declaringParent(), ex);
            return Schema.schemaWithFormat("error", "error.see.logs");
        }

    }

    private Schema convertFieldToComplexSchema(final FieldAccess fieldAccess) {

        if (isArraySchema(fieldAccess)) {
            return convertFieldToArraySchema(fieldAccess);
        } else if (isMap(fieldAccess)) {
            return convertFieldToMapSchema(fieldAccess);
        } else if (isOptional(fieldAccess)) {
            return null; //TODO not done
        } else {
            return convertFieldToDefinitionRef(fieldAccess);
        }

    }


    private Schema convertFieldToMapSchema(final FieldAccess fieldAccess) {

        final Type[] actualTypeArguments = fieldAccess.getParameterizedType().getActualTypeArguments();

        final String description = getDescription(fieldAccess);


        if (actualTypeArguments[1] instanceof Class) {

            Schema componentSchema = mappings.get(actualTypeArguments[1]);
            /* If it was not in the mapping, then it is complex. */
            if (componentSchema == null) {
                if (!definitionMap.containsKey(fieldAccess.getComponentClass().getSimpleName())) {
                    addClass(fieldAccess.getComponentClass());
                }
                componentSchema = Schema.definitionRef(fieldAccess.getComponentClass().getSimpleName(), "");
            }
            return Schema.map(componentSchema, description);
        } else {
            return null;
        }

    }

    private boolean isArraySchema(FieldAccess fieldAccess) {

        switch (fieldAccess.typeEnum()) {
            case SET:
                return true;
            case LIST:
                return true;
            case COLLECTION:
                return true;
            case ARRAY:
                return true;
        }
        return false;
    }


    private boolean isMap(FieldAccess fieldAccess) {

        switch (fieldAccess.typeEnum()) {
            case MAP:
                return true;

        }
        return false;
    }


    private boolean isOptional(FieldAccess fieldAccess) {

        return (fieldAccess.type() == Optional.class);
    }


    private Schema convertFieldToDefinitionRef(final FieldAccess fieldAccess) {


        if (!definitionMap.containsKey(fieldAccess.type().getSimpleName())) {
            addClass(fieldAccess.type());
        }

        final String description = getDescription(fieldAccess);
        return Schema.definitionRef(fieldAccess.type().getSimpleName(), description);
    }

    private Schema convertFieldToDefinitionRef(final ClassMeta classMeta) {


        if (!definitionMap.containsKey(classMeta.name())) {
            addClass(classMeta.cls());
        }

        final String description = getDescription(classMeta);
        return Schema.definitionRef(classMeta.name(), description);
    }


    private String getDescription(final FieldAccess fieldAccess) {
        String description = "";
        final Map<String, Object> descriptionMap = fieldAccess.getAnnotationData("Description");
        if (descriptionMap != null) {

            if (descriptionMap.containsKey("value")) {
                description = descriptionMap.get("value").toString();
            }
        }
        return description;
    }


    private String getDescription(ClassMeta classMeta) {
        String description = "";

        AnnotationData annotationData = classMeta.annotation("Description");

        if (annotationData == null) {
            return "";
        }

        final Map<String, Object> descriptionMap = annotationData.getValues();
        if (descriptionMap != null) {
            if (descriptionMap.containsKey("value")) {
                description = descriptionMap.get("value").toString();
            }
        }
        return description;
    }

    private Schema convertFieldToArraySchema(final FieldAccess fieldAccess) {

        String description = getDescription(fieldAccess);

        Schema componentSchema = mappings.get(fieldAccess.getComponentClass());
            /* If it was not in the mapping, then it is complex. */
        if (componentSchema == null) {
            if (!definitionMap.containsKey(fieldAccess.getComponentClass().getSimpleName())) {
                addClass(fieldAccess.getComponentClass());
            }
            componentSchema = Schema.definitionRef(fieldAccess.getComponentClass().getSimpleName(), description);
        }
        return Schema.array(componentSchema);
    }

    private Schema convertFieldToArraySchema(final ClassMeta classMeta) {

        String description = getDescription(classMeta);

        Schema componentSchema = mappings.get(classMeta.cls());
            /* If it was not in the mapping, then it is complex. */
        if (componentSchema == null) {
            if (!definitionMap.containsKey(classMeta.name())) {
                addClass(classMeta.cls());
            }
            componentSchema = Schema.definitionRef(classMeta.name(), description);
        }
        return Schema.array(componentSchema);
    }

    public Map<String, Definition> getDefinitionMap() {
        return definitionMap;
    }

    public Schema getSchemaWithMapClass(final Class<?> returnType,
                                        final Class<?> returnTypeComponentKey,
                                        final Class<?> returnTypeComponentValue) {

        if (returnTypeComponentValue != null) {
            if (returnTypeComponentValue.getName().startsWith("java.lang")) {
                final Schema schemaForComponent = mappings.get(returnTypeComponentValue);
                return Schema.map(schemaForComponent, "");
            } else {
                return Schema.map(Schema.definitionRef(returnTypeComponentValue.getSimpleName(), ""), "");
            }
        } else {
            logger.info("Component class was null defaulting to string");
            return Schema.map(Schema.definitionRef("string", ""), "");
        }
    }

    public Schema getSchemaForJSendArray(Class<?> returnType, Class<?> returnTypeComponent) {

        return Schema.definitionRef("jsend-array-" + returnTypeComponent.getSimpleName(), "");

    }

    public void addJSendArray(Class<?> cls) {
        addClass(cls);
        addJSendArray(ClassMeta.classMeta(cls));


    }

    public void addJSendArray(ClassMeta classMeta) {

        try {

            if (definitionMap.containsKey("jsend-array-" + classMeta.name())) {
                return;
            }

            definitionMap.put("jsend-array-" + classMeta.name(), null);

            final DefinitionBuilder definitionBuilder = new DefinitionBuilder();


            definitionBuilder.setDescription("jsend standard response");

            Schema schema = mappings.get(classMeta.cls());


            if (schema != null) {
                definitionBuilder.addProperty("data", schema);
            } else {
                schema = convertFieldToArraySchema(classMeta);
                definitionBuilder.addProperty("data", schema);
            }

            definitionBuilder.addProperty("status", Schema.schemaWithDescription(mappings.get(String.class),
                    "Status of return, this can be 'success', 'fail' or 'error'"));

            final Definition definition = definitionBuilder.build();


            definitionMap.put("jsend-array-" + classMeta.name(), definition);

        } catch (Exception ex) {
            logger.warn("Unable to add class " + classMeta.longName(), ex);
        }
    }
}
