package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


/**
 * Created by rick on 6/30/15.
 */
public class DefinitionClassCollectorTest {

    DefinitionClassCollector definitions;

    @Before
    public void setup() {
        definitions = new DefinitionClassCollector();
    }



    public static class Phone {

        String countryCode;
        String areaCode;
        String digits;
    }

    public static class Employee {

        Phone phone;
        String name;
        int age;
        long iq;

        byte byteA;
        Byte byteB;
        byte[] byteC;
        Byte[] byteD;

        float floatA;
        Float floatB;

        double dA;
        Double dB;

    }

    public static class Department {
        List<Employee> employeeList;
        Employee[] employeeArray;
    }

    @Test
    public void test() {
        definitions.addClass(Department.class);


        JsonSerializer jsonSerializer = new JsonSerializerFactory().setUseAnnotations(true).create();

        System.out.println(jsonSerializer.serialize(definitions.getDefinitionMap()));
    }

}