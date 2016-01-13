package io.advantageous.qbit.example.rawmap;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.PUT;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.reactive.Callback;

import java.util.Map;

@RequestMapping("/map")
public class HandleMap {

    private final JsonMapper jsonMapper = QBit.factory().createJsonMapper();

    @PUT("/put")
    public void putMap(final Callback<Map<String,Object>> httpCallback,
                       final Map<String, Object> map) {

        httpCallback.returnThis(map);

    }


    public static void main(final String... args) {

        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");

        managedServiceBuilder.addEndpointService(new HandleMap());

        managedServiceBuilder.getEndpointServerBuilder().build().startServer();
    }


}
