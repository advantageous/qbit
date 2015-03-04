package io.advantageous.boon.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.ObjectMapper;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 1/5/15.
 */
public class Bug287 {

    boolean ok;


    public static class SomeClass {
        Class<?> clazz;

        SomeClass(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    @Test
    public void serializingClassFieldCausesSegFault() {

        SomeClass someClassInstance = new SomeClass(Bug287.class);

        ObjectMapper mapper = JsonFactory.create();

        final String json = mapper.toJson(someClassInstance);

        puts(json);

        SomeClass someClassInstance2 = mapper.readValue("{\"clazz\":\"io.advantageous.boon.bugs.Bug287\"} ", SomeClass.class);

        ok = someClassInstance2.clazz.getName().equals("Bug287");

    }

}
