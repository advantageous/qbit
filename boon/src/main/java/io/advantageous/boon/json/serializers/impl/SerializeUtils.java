package io.advantageous.boon.json.serializers.impl;

import io.advantageous.boon.Boon;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.json.serializers.CustomObjectSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Richard on 9/16/14.
 */
public class SerializeUtils {


    public static void  handleInstance(JsonSerializerInternal jsonSerializer,
                                        Object obj, CharBuf builder,
                                        Map<Class, CustomObjectSerializer> overrideMap,
                                        Set<Class> noHandle, boolean typeInfo,
                                        TypeType type) {
        if (overrideMap!=null) {
            Class<?> cls = Boon.cls(obj);
            if (cls != null && !cls.isPrimitive() && !noHandle.contains(cls)) {
                CustomObjectSerializer customObjectSerializer = overrideMap.get(cls);
                if (customObjectSerializer != null) {
                    customObjectSerializer.serializeObject(jsonSerializer, obj, builder);
                    return;
                }
                customObjectSerializer = overrideMap.get(cls.getSuperclass());
                if (customObjectSerializer != null) {
                    overrideMap.put(cls.getSuperclass(), customObjectSerializer); //Remember this
                    customObjectSerializer.serializeObject(jsonSerializer, obj, builder);
                    return;
                }

                final Class<?>[] interfaces = cls.getInterfaces();
                for (Class interf : interfaces) {

                    customObjectSerializer = overrideMap.get(interf);
                    if (customObjectSerializer != null) {
                        overrideMap.put(interf, customObjectSerializer); //Remember this
                        customObjectSerializer.serializeObject(jsonSerializer, obj, builder);
                        return;
                    }

                }

                noHandle.add(cls);

            }
        }


        switch (type) {
            case MAP:
                jsonSerializer.serializeMap((Map) obj, builder);
                return;
            case COLLECTION:
            case LIST:
            case SET:
                jsonSerializer.serializeCollection((Collection)obj, builder);
                return;
            case INSTANCE:
                jsonSerializer.serializeInstance(obj, builder, typeInfo);
                return;
            case INTERFACE:
            case ABSTRACT:
                jsonSerializer.serializeSubtypeInstance(obj, builder);
                return;

            default:
                jsonSerializer.serializeUnknown(obj, builder);
        }
    }

}
