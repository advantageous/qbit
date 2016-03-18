package io.advantageous.qbit.example.kvstore;

import io.advantageous.qbit.kvstore.JsonKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.KeyValueStoreService;
import io.advantageous.qbit.kvstore.RedisKeyValueStore;
import io.advantageous.qbit.kvstore.RedisKeyValueStoreBuilder;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelWriteBehindReadFallbackKeyValueStore;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.time.Duration;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.Sys.sleep;


public class KVExample {


    static  class Todo {
        final String name;

        private Todo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Todo{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static void main(String... args) throws Exception {


        final RedisKeyValueStore keyValueStore;

        final RedisKeyValueStoreBuilder builder;


        builder = RedisKeyValueStoreBuilder.redisKeyValueStoreBuilder();


            builder.setRedisUri(new URI("redis://redisdb:foobared@localhost:6379/0"));
        keyValueStore = builder.build();


        final LowLevelLocalKeyValueStoreServiceBuilder lowLevelLocalKeyValueStoreServiceBuilder =
                LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder();


        final LowLevelWriteBehindReadFallbackKeyValueStore writeBehindReadFallbackKeyValueStoreInternal =
                new LowLevelWriteBehindReadFallbackKeyValueStore(lowLevelLocalKeyValueStoreServiceBuilder.build(),
                        keyValueStore,
                        ReactorBuilder.reactorBuilder().build());

        final LowLevelKeyValueStoreService lowLevelKeyValueStoreService = ServiceBuilder.serviceBuilder()
                .setServiceObject(writeBehindReadFallbackKeyValueStoreInternal)
                .buildAndStartAll()
                .createProxyWithAutoFlush(LowLevelKeyValueStoreService.class, Duration.FIFTY_MILLIS);


        final KeyValueStoreService<Todo> todoKVStoreInternal = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder()
                .setLowLevelKeyValueStoreService(lowLevelKeyValueStoreService).buildKeyValueStore(Todo.class);


        final KeyValueStoreService<Todo> todoKVStore = ServiceBuilder.serviceBuilder()
                .setServiceObject(todoKVStoreInternal)
                .buildAndStartAll()
                .createProxyWithAutoFlush(KeyValueStoreService.class, Duration.FIFTY_MILLIS);


        sleep(1000);

        for (int index=0; index<10000; index++) {
            final Duration timeout = new Duration(2, TimeUnit.SECONDS);
            final int idx = index;

            final Todo todoSent = new Todo("todo" + index);
            final String key = "key" + index;
            todoKVStore.putWithTimeout(key, todoSent, timeout);

            sleep(1);

            todoKVStore.get(todo -> {

                try {
                    todo.get();

                    if (!todo.get().name.equals(todoSent.name)) {
                        System.out.print("E");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (idx % 100 == 0) {
                    System.out.print(".");
                }

            }, key);
        }



    }

}
