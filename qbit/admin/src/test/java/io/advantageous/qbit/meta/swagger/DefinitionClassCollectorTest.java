package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by rick on 6/30/15.
 */
public class DefinitionClassCollectorTest {

    DefinitionClassCollector definitions;

    @Before
    public void setup() {
        definitions = new DefinitionClassCollector();
    }


    @Test
    public void test() {
        definitions.addClass(Department.class);


        JsonSerializer jsonSerializer = new JsonSerializerFactory().setUseAnnotations(true).create();

        System.out.println(jsonSerializer.serialize(definitions.getDefinitionMap()));
    }

}