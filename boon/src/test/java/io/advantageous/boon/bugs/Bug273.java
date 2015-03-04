package io.advantageous.boon.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonParserFactory;
import org.junit.Test;

import java.util.Map;

public class Bug273 {

        public static class A {
            public Map m;
        }

    @Test
        public  void test() {
            A a = new JsonParserFactory().createUTF8DirectByteParser().parse(A.class, "{\"m\":{\"a\":\"b\"}}");
            System.out.println(JsonFactory.toJson(a));
        }
}
