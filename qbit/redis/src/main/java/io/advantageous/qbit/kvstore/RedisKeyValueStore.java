package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

import java.util.Optional;

public class RedisKeyValueStore implements LowLevelKeyValueStoreService {



    public RedisKeyValueStore() {

    }

    @Override
    public void putString(String key, String value) {

    }

    @Override
    public void putStringWithConfirmation(final Callback<Boolean> confirmation, final String key, final String value) {


    }

    @Override
    public void putStringWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, String value, Duration expiry) {

    }

    @Override
    public void putStringWithTimeout(String key, String value, Duration expiry) {

    }

    @Override
    public void getString(Callback<Optional<String>> confirmation, String key) {

    }

    @Override
    public void putBytes(String key, byte[] value) {

    }

    @Override
    public void putBytesWithConfirmation(Callback<Boolean> confirmation, String key, byte[] value) {

    }

    @Override
    public void putBytesWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, byte[] value, Duration expiry) {

    }

    @Override
    public void putBytesWithTimeout(String key, byte[] value, Duration expiry) {

    }

    @Override
    public void getBytes(Callback<Optional<byte[]>> callback, String key) {

    }

    @Override
    public void hasKey(Callback<Boolean> hasKeyCallback, String key) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void deleteWithConfirmation(Callback<Boolean> confirmation, String key) {

    }

    @Override
    public void process() {

    }
}
