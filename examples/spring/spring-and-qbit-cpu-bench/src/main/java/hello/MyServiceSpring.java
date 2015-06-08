package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@EnableAutoConfiguration
@Scope
public class MyServiceSpring {

    @RequestMapping(value = "/services/myservice/ping",
            produces = "application/json")
    @ResponseBody
    List<String> ping() {
        return Collections.singletonList("Hello World!");
    }

    public static void main(String[] args) throws Exception {

        SpringApplication.run(MyServiceSpring.class, args);
    }

}
