package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/*
 ./wrk -c 3000 -d 10s http://localhost:8080/services/myservice/ping -H "X_USER_ID: RICK"   --timeout 100000s -t 8

 */

@RestController
@EnableAutoConfiguration
public class MyService {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MyService.class, args);
    }

    @RequestMapping(value = "/services/myservice/ping", produces = "application/json")
    @ResponseBody
    List home() {
        return Collections.singletonList("Hello World!");
    }
}
