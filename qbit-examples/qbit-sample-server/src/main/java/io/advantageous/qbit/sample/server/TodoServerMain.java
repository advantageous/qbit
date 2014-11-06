package io.advantageous.qbit.sample.server;


import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.sample.server.service.TodoService;
import io.advantageous.qbit.server.Server;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.vertx.http.HttpServerVertx;
import org.boon.Sets;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoServerMain {

    public static void main(String... args) {


        RegisterBoonWithQBit.registerBoonWithQBit();

        final HttpServer httpServer = new HttpServerVertx(8080, "localhost");

        final Factory factory = QBit.factory();

        final ProtocolEncoder encoder = factory.createEncoder();
        final ServiceBundle serviceBundle = factory.createServiceBundle("/services/");
        final JsonMapper jsonMapper = new BoonJsonMapper();

        Server server = new Server(httpServer, encoder, serviceBundle, jsonMapper);

        server.initServices(Sets.set(new TodoService()));

        server.run();







    }
}
