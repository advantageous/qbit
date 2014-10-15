package io.advantageous.qbit.json;

/**
 * Abstraction for JSON parsing.
 * <p>
 * Created by gcc on 10/14/14.
 */
public interface JsonMapper {

    Object fromJson(String json);

    <T> T fromJson(Class<T> clazz, String json);

    String toJson(Object object);

}
