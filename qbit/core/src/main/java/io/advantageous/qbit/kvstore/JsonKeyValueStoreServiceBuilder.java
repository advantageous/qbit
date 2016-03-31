package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.kvstore.impl.StringDecoderEncoderKeyValueStore;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;

import java.util.List;

/**
 * ***JsonKeyValueStoreServiceBuilder*** produces `StringDecoderEncoderKeyValueStore`
 * that can serialize/parse object to/for Java/JSON.
 * You don't typically use the `StringDecoderEncoderKeyValueStore` directly but you could.
 * Instead you use it in conjunction with the ***JsonKeyValueStoreServiceBuilder*** which constructs
 * `StringDecoderEncoderKeyValueStore` that do JSON encoding and decoding.
 * <p>
 * #### Example using JsonKeyValueStoreServiceBuilder
 * <p>
 * ```java
 * <p>
 * <p>
 * private JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder;
 * private LowLevelLocalKeyValueStoreService localKeyValueStoreService = ...;
 * private KeyValueStoreService<Todo> keyValueStoreService;
 * <p>
 * jsonKeyValueStoreServiceBuilder.setLowLevelKeyValueStoreService(localKeyValueStoreService);
 * <p>
 * keyValueStoreService = jsonKeyValueStoreServiceBuilder.buildKeyValueStore(Todo.class);
 * keyValueStoreService.put("key", new Todo("value"));
 * <p>
 * <p>
 * ```
 * <p>
 * Essentially `JsonKeyValueStoreServiceBuilder` can turn a `LowLevelLocalKeyValueStoreService`
 * into a `KeyValueStoreService<Todo>` (object store).
 */
public class JsonKeyValueStoreServiceBuilder {

    private LowLevelKeyValueStoreService lowLevelKeyValueStoreService;

    private JsonMapper jsonMapper;

    private Reactor reactor;

    /**
     * Create a new builder
     *
     * @return new builder
     */
    public static JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder() {
        return new JsonKeyValueStoreServiceBuilder();
    }

    public Reactor getReactor() {
        if (reactor == null) {
            reactor = ReactorBuilder.reactorBuilder().build();
        }
        return reactor;
    }

    public JsonKeyValueStoreServiceBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public JsonMapper getJsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = QBit.factory().createJsonMapper();
        }
        return jsonMapper;
    }

    public JsonKeyValueStoreServiceBuilder setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public LowLevelKeyValueStoreService getLowLevelKeyValueStoreService() {
        return lowLevelKeyValueStoreService;
    }

    public JsonKeyValueStoreServiceBuilder setLowLevelKeyValueStoreService(LowLevelKeyValueStoreService lowLevelKeyValueStoreService) {
        this.lowLevelKeyValueStoreService = lowLevelKeyValueStoreService;
        return this;
    }

    /**
     * @param componentClass component class type
     * @param <T>            T
     * @return new kv store that works with lists of componentClass instances
     */
    public <T> StringDecoderEncoderKeyValueStore<List<T>> buildKeyListOfValueStore(final Class<T> componentClass) {

        final JsonMapper jsonMapper = getJsonMapper();

        return new StringDecoderEncoderKeyValueStore<>(
                json -> jsonMapper.fromJsonArray(json, componentClass),
                jsonMapper::toJson, this.getLowLevelKeyValueStoreService(), getReactor());

    }

    /**
     * @param componentClass component class type
     * @param <T>            T
     * @return new kv store that works with componentClass instances
     */
    public <T> StringDecoderEncoderKeyValueStore<T> buildKeyValueStore(final Class<T> componentClass) {

        final JsonMapper jsonMapper = getJsonMapper();
        return new StringDecoderEncoderKeyValueStore<>(
                json -> jsonMapper.fromJson(json, componentClass),
                jsonMapper::toJson, this.getLowLevelKeyValueStoreService(), getReactor());

    }

}
