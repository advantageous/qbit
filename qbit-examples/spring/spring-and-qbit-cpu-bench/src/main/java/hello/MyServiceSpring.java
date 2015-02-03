package hello;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/*
 ./wrk -c 3000 -d 10s http://localhost:8080/services/myservice/ping -H "X_USER_ID: RICK"   --timeout 100000s -t 8

 */

@RestController
@EnableAutoConfiguration
public class MyServiceSpring {

    ActualService actualService = new ActualService();


    @RequestMapping(value = "/services/myservice/ping" , produces = "application/json")
    @ResponseBody
    List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping(value = "/services/myservice/addkey/" , produces = "application/json")
    @ResponseBody
    synchronized public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        return actualService.addKey(key, value);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MyServiceSpring.class, args);
    }
}
