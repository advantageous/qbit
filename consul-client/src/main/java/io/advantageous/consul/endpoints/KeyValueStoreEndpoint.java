/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
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
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
package io.advantageous.consul.endpoints;

import io.advantageous.boon.Boon;
import io.advantageous.boon.Str;
import io.advantageous.consul.domain.KeyValue;

import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.KeyValuePutOptions;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponse;

import java.util.*;

import static io.advantageous.consul.domain.ConsulException.die;

/**
 * HTTP Client for /v1/kv/ endpoints.
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class KeyValueStoreEndpoint {


    private final HttpClient httpClient;
    private final String rootPath;


    /**
     *
     * @param httpClient http client
     * @param rootPath root path
     */
    public KeyValueStoreEndpoint(final HttpClient httpClient, final String rootPath) {
        this.httpClient = httpClient;
        this.rootPath = rootPath;
    }


    /**
     * Retrieves a {@link io.advantageous.consul.domain.KeyValue} for a specific key
     * from the key/value store.
     *
     * GET /v1/keyValueStore/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value or {@link java.util.Optional#empty()}
     */
    public Optional<KeyValue> getValue(String key) {
        return getValue(key, RequestOptions.BLANK);
    }

    /**
     * Retrieves a {@link io.advantageous.consul.domain.KeyValue} for a specific key
     * from the key/value store.
     *
     * GET /v1/keyValueStore/{key}
     *
     * @param key The key to retrieve.
     * @param requestOptions The query options.
     * @return An {@link Optional} containing the value or {@link java.util.Optional#empty()}
     */
    public Optional<KeyValue> getValue(final String key, RequestOptions requestOptions) {

        final String path = rootPath + "/" + key;

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, requestOptions, path);

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());

        if (httpResponse.code()==404) {
            return Optional.empty();
        }

        if (httpResponse.code()!=200) {
            die("Unable to retrieve the key", key, path, httpResponse.code(), httpResponse.body());
        }

        return getKeyValueOptional(httpResponse);
    }

    private Optional<KeyValue> getKeyValueOptional(HttpResponse httpResponse) {
        final List<KeyValue> keyValues = Boon.fromJsonArray(httpResponse.body(), KeyValue.class);

        return keyValues != null && keyValues.size() > 0 ? Optional.of(keyValues.get(0)) : Optional.<KeyValue>empty();
    }

    /**
     * Retrieves a list of {@link io.advantageous.consul.domain.KeyValue} objects for a specific key
     * from the key/value store.
     *
     * GET /v1/keyValueStore/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many {@link io.advantageous.consul.domain.KeyValue} objects.
     */
    public List<KeyValue> getValues(String key) {


        final String path = rootPath + "/" + key;
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, path);
        httpRequestBuilder.addParam("recurse", "true");

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());
        if (httpResponse.code()!=200) {
            die("Unable to retrieve the service", path, httpResponse.code(), httpResponse.body());
        }

        return Boon.fromJsonArray(httpResponse.body(), KeyValue.class);

    }

    /**
     * Retrieves a string value for a specific key from the key/value store.
     *
     * GET /v1/keyValueStore/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value as a string or
     * {@link java.util.Optional#empty()}
     */
    public Optional<String> getValueAsString(String key) {
        Optional<KeyValue> value = getValue(key);

        return value.isPresent() ? Optional.of(RequestUtils.decodeBase64(value.get().getValue()))
                : Optional.<String>empty();
    }

    /**
     * Retrieves a list of string values for a specific key from the key/value
     * store.
     *
     * GET /v1/keyValueStore/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many string values.
     */
    public List<String> getValuesAsString(String key) {
        List<String> result = new ArrayList<>();

        for(KeyValue keyValue : getValues(key)) {
            result.add(RequestUtils.decodeBase64(keyValue.getValue()));
        }

        return result;
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value) {
        return putValue(key, value, 0L, KeyValuePutOptions.BLANK);
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param flags The flags for this key.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, long flags) {
        return putValue(key, value, flags, KeyValuePutOptions.BLANK);
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param putOptions PUT options (e.g. wait, acquire).
     * @return <code>true</code> if the value was successfully indexed.
     */
    private boolean putValue(final String key, final String value, final long flags, final KeyValuePutOptions putOptions) {
        Integer cas = putOptions.getCas();
        String release = putOptions.getRelease();
        String acquire = putOptions.getAcquire();


        final String path = rootPath + "/" + key;

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, path);


        if(cas != null) {
            httpRequestBuilder.addParam("cas", cas.toString());
        }

        if(!Str.isEmpty(release)) {
            httpRequestBuilder.addParam("release", release);
        }

        if(!Str.isEmpty(acquire)) {
            httpRequestBuilder.addParam("acquire", acquire);
        }

        if (flags != 0) {
            httpRequestBuilder.addParam("flags", String.valueOf(flags));
        }

        httpRequestBuilder.setBody(value);
        httpRequestBuilder.setMethodPut();
        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());

        if (httpResponse.code()==200) {
            return Boolean.parseBoolean(httpResponse.body());
        } else {
            die("Unable to put value", path, putOptions, httpResponse.code(), httpResponse.body());
            return false;
        }

    }

    /**
     * Retrieves a list of matching keys for the given key.
     *
     * GET /v1/keyValueStore/{key}?keys
     *
     * @param key The key to retrieve.
     * @return A list of zero to many keys.
     */
    public List<String> getKeys(String key) {
        final String path = rootPath + "/" + key;

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, path);

        httpRequestBuilder.addParam("keys", "true");

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());

        if (httpResponse.code()==200) {
            return Boon.fromJsonArray(httpResponse.body(), String.class);
        } else {
            die("Unable to get nested keys", path, key, httpResponse.code(), httpResponse.body());
            return Collections.emptyList();
        }
    }

    /**
     * Deletes a specified key.
     *
     * DELETE /v1/keyValueStore/{key}
     *
     * @param key The key to delete.
     */
    public void deleteKey(String key) {
        delete(key, Collections.EMPTY_MAP);
    }

    /**
     * Deletes a specified key and any below it.
     *
     * DELETE /v1/keyValueStore/{key}?recurse
     *
     * @param key The key to delete.
     */
    public void deleteKeys(String key) {
        delete(key, Collections.singletonMap("recurse", "true"));
    }

    /**
     * Deletes a specified key.
     *
     * @param key The key to delete.
     * @param params Map of parameters, e.g. recurse.
     */
    private void delete(String key, Map<String, String> params) {

        final String path = rootPath + "/" + key;

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, path);


        final Set<Map.Entry<String, String>> entries = params.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            httpRequestBuilder.addParam(entry.getKey(), entry.getValue());
        }

        httpRequestBuilder.setMethodDelete();

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());



        if (httpResponse.code()!=200) {
            die("Unable to delete key", path, key, httpResponse.code(), httpResponse.body());
        }
    }
}
