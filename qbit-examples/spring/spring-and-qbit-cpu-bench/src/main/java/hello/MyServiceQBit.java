package hello;


import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import org.boon.core.Sys;

import java.util.Collections;
import java.util.List;

/**
 * Created by rhightower on 2/2/15.
 */
@RequestMapping("/myservice")
public class MyServiceQBit {

    ActualService actualService = new ActualService();

    @RequestMapping("/ping")
    public List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping("/addkey/" )
    public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        return actualService.addKey(key, value);
    }

    public static void main(String... args) throws Exception {



        final ServiceServer serviceServer = new ServiceServerBuilder().setPort(6060)
                .build();

        serviceServer.initServices(new MyServiceQBit());
        serviceServer.start();


        while (true) Sys.sleep(100_000_000);
    }


}
