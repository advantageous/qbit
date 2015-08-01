package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.qbit.meta.swagger.builders.DefinitionBuilder;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DefinitionClassCollector {

    private final Map<String, Definition> definitionMap = new HashMap<>();

    private final  Map<Class, Schema> mappings =  Maps.map(
            /* Adding common primitive and basic type mappings. */
            String.class,           Schema.schema("string"),
            StringBuffer.class,     Schema.schema("string"),
            Date.class,             Schema.schema("string", "dateTime"),
            Integer.class,          Schema.schema("integer", "int32"),
            int.class,              Schema.schema("integer", "int32"),
            Long.class,             Schema.schema("integer", "int64"),
            long.class,             Schema.schema("integer", "int64"),
            Float.class,            Schema.schema("number", "float"),
            float.class,            Schema.schema("number", "float"),
            Double.class,           Schema.schema("number", "double"),
            double.class,           Schema.schema("number", "double"),
            Boolean.class,          Schema.schema("boolean", ""),
            boolean.class,          Schema.schema("boolean", ""),
            byte.class,             Schema.schema("string", "byte"),
            Byte.class,             Schema.schema("string", "byte")

    );


    {
        /* Adding common primitive and basic type arrays. */
        mappings.put(String[].class,        Schema.array(mappings.get(String.class)));
        mappings.put(StringBuffer[].class,  Schema.array(mappings.get(StringBuffer.class)));
        mappings.put(Date[].class,  Schema.array(mappings.get(Date.class)));
        mappings.put(int[].class,  Schema.array(mappings.get(int.class)));
        mappings.put(Integer[].class,  Schema.array(mappings.get(Integer.class)));
        mappings.put(long[].class,  Schema.array(mappings.get(long.class)));
        mappings.put(Long[].class,  Schema.array(mappings.get(Long.class)));
        mappings.put(Double[].class,  Schema.array(mappings.get(Double.class)));
        mappings.put(double[].class,  Schema.array(mappings.get(double.class)));
        mappings.put(Float[].class,  Schema.array(mappings.get(Float.class)));
        mappings.put(float[].class,  Schema.array(mappings.get(float.class)));
        mappings.put(Boolean[].class,  Schema.array(mappings.get(Boolean.class)));
        mappings.put(boolean[].class,  Schema.array(mappings.get(boolean.class)));
        mappings.put(byte[].class,  Schema.array(mappings.get(byte.class)));
        mappings.put(Byte[].class,  Schema.array(mappings.get(Byte.class)));

    }


    public Schema getSchema(final Class<?> cls) {
        return getSchema(cls, null);
    }

    public Schema getSchema(final Class<?> cls, Class<?> componentClass) {

        Schema schema = mappings.get(cls);

        if (schema != null) {
            return schema;
        }

        /* This does not really handle generic returns or generic params which are collections but....
                We know how to do this see io.advantageous.boon.core.reflection.fields.BaseField constructor
                protected BaseField ( String name, Method getter, Method setter ) for an example.

         */

        if (cls != null) {
            TypeType type = TypeType.getType(cls);

            if (type.isArray()) {
                return Schema.array(Schema.definitionRef(cls.getComponentType().getSimpleName()));
            } else if (type.isCollection()) {
                return Schema.array(Schema.definitionRef(componentClass.getSimpleName()));
            }

            return Schema.definitionRef(cls.getSimpleName());
        } else {
              return Schema.schema("string");
        }

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

            final DefinitionBuilder definitionBuilder = new DefinitionBuilder();

            Map<String, FieldAccess> fieldAccessMap = classMeta.fieldMap();


            fieldAccessMap.entrySet().forEach(fieldAccessEntry -> {

                final FieldAccess fieldAccess = fieldAccessEntry.getValue();
                if (fieldAccess.ignore() || fieldAccess.isStatic()) {
                    return;
                }
                definitionBuilder.addProperty(fieldAccess.name(), convertFieldToSchema(fieldAccess));

            });

            final Definition definition = definitionBuilder.build();

            definitionMap.put(classMeta.name(), definition);
        }catch (Exception ex) {
            throw new RuntimeException("Unable to add class " + classMeta.longName(), ex);
        }
    }

    private Schema convertFieldToSchema(final FieldAccess fieldAccess) {

        try {

            final Class<?> type = fieldAccess.type();
            final Schema schema = mappings.get(type);

            if (schema != null) {
                return schema;
            }

            return convertFieldToComplexSchema(fieldAccess);
        } catch (Exception ex) {
            throw new RuntimeException("unable to convert field " + fieldAccess.name() + " from " + fieldAccess.declaringParent(), ex);
        }

    }

    private Schema convertFieldToComplexSchema(final FieldAccess fieldAccess) {

        if (isArraySchema(fieldAccess)) {

            return convertFieldToArraySchema(fieldAccess);
        } else if (isMap(fieldAccess)) {

            return convertFieldToMapSchema(fieldAccess);
        } else {
            return convertFieldToDefinitionRef(fieldAccess);
        }

    }

    private Schema convertFieldToMapSchema(final FieldAccess fieldAccess) {

        Type[] actualTypeArguments = fieldAccess.getParameterizedType().getActualTypeArguments();


        if (actualTypeArguments[1] instanceof  Class) {

            Schema componentSchema = mappings.get(actualTypeArguments[1]);
            /* If it was not in the mapping, then it is complex. */
            if (componentSchema == null) {
                if (!definitionMap.containsKey(fieldAccess.getComponentClass().getSimpleName())) {
                    addClass(fieldAccess.getComponentClass());
                }
                componentSchema = Schema.definitionRef(fieldAccess.getComponentClass().getSimpleName());
            }
            return Schema.map(componentSchema);
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

    private Schema convertFieldToDefinitionRef(final FieldAccess fieldAccess) {
        if (!definitionMap.containsKey(fieldAccess.type().getSimpleName())) {
            addClass(fieldAccess.type());
        }
       return Schema.definitionRef(fieldAccess.type().getSimpleName());
    }

    private Schema convertFieldToArraySchema(final FieldAccess fieldAccess) {

        Schema componentSchema = mappings.get(fieldAccess.getComponentClass());
            /* If it was not in the mapping, then it is complex. */
        if (componentSchema == null) {
            if (!definitionMap.containsKey(fieldAccess.getComponentClass().getSimpleName())) {
                addClass(fieldAccess.getComponentClass());
            }
            componentSchema = Schema.definitionRef(fieldAccess.getComponentClass().getSimpleName());
        }
        return Schema.array(componentSchema);
    }


    public Map<String, Definition> getDefinitionMap() {
        return definitionMap;
    }
}
