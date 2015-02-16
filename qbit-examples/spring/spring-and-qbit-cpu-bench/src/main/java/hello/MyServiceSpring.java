package hello;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static org.boon.Boon.puts;

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
        puts("created MyServiceSpring");
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
