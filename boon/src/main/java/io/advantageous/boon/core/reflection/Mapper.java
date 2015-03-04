package io.advantageous.boon.core.reflection;

import io.advantageous.boon.core.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 9/18/14.
 */
public interface Mapper {
    <T> List<T> convertListOfMapsToObjects(List<Map> list, Class<T> componentType);

    <T> T fromMap(Map<String, Object> map, Class<T> cls);

    <T> T fromList(List<?> argList, Class<T> clazz);

    @SuppressWarnings("unchecked")
    Object fromValueMap(Map<String, Value> valueMap
    );

    @SuppressWarnings("unchecked")
    <T> T fromValueMap(Map<String, Value> valueMap,
                       Class<T> cls);

    Object fromMap(Map<String, Object> map);

    Map<String, Object> toMap(Object object);

    List<Map<String, Object>> toListOfMaps(Collection<?> collection);

    List<?> toList(Object object);
}
