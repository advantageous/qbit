package io.advantageous.qbit.json;

/**
 * Abstraction for JSON parsing.
 * QBit allows Jackson, GSON, or Boon to be plugged in as JSON serializer providers.
 * <p>
 * Created by gcc on 10/14/14.
 */
public interface JsonMapper {

    Object fromJson(String json);
    
    String toJson(Object object);

}
