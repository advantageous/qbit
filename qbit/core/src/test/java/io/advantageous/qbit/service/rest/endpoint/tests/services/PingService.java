package io.advantageous.qbit.service.rest.endpoint.tests.services;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;

@RequestMapping("/")
public class PingService {


    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }


}
