package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


/*
 ./wrk -c 3000 -d 10s http://localhost:8080/services/myservice/ping -H "X_USER_ID: RICK"   --timeout 100000s -t 8

 */

@RestController
@EnableAutoConfiguration
@Scope
public class MyServiceSpring {

    static volatile int count;

    ActualService actualService = new ActualService();

    MyServiceSpring() {
        System.out.println("created MyServiceSpring");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MyServiceSpring.class, args);
    }

    @RequestMapping(value = "/services/myservice/ping", produces = "application/json")
    @ResponseBody
    List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping(value = "/services/myservice/addkey/", produces = "application/json")
    @ResponseBody
    synchronized public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        count++;
        if (count > 5) {
            count = 0;
            write();
        }
        return actualService.addKey(key, value);
    }

    private synchronized void write() {
        actualService.write();
    }
}
