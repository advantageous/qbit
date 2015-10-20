package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;

import java.util.List;

public class JsonKeyValueStoreServiceBuilder {

    private LowLevelKeyValueStoreService lowLevelKeyValueStoreService;

    private JsonMapper jsonMapper;

    private Reactor reactor;

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
        if (jsonMapper==null) {
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
     *
     * @param componentClass component class type
     * @param <T> T
     * @return new kv store that works with lists of componentClass instances
     */
    public  <T> StringDecoderEncoderKeyValueStore<List<T>> buildKeyListOfValueStore(final Class<T> componentClass) {

        final JsonMapper jsonMapper = getJsonMapper();

        return new StringDecoderEncoderKeyValueStore<>(
                json -> jsonMapper.fromJsonArray(json, componentClass),
                jsonMapper::toJson, this.getLowLevelKeyValueStoreService(), getReactor());

    }


    /**
     *
     * @param componentClass component class type
     * @param <T> T
     * @return new kv store that works with componentClass instances
     */
    public  <T> StringDecoderEncoderKeyValueStore<T> buildKeyValueStore(final Class<T> componentClass) {

        final JsonMapper jsonMapper = getJsonMapper();
        return new StringDecoderEncoderKeyValueStore<>(
                json -> jsonMapper.fromJson(json, componentClass),
                jsonMapper::toJson, this.getLowLevelKeyValueStoreService(), getReactor());

    }


    /**
     * Create a new builder
     * @return new builder
     */
    public static JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder () {
        return new JsonKeyValueStoreServiceBuilder();
    }

}