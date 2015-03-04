package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.core.reflection.impl.OverloadedMethod;
import org.junit.Before;
import org.junit.Test;


import static io.advantageous.boon.Boon.equalsOrDie;
import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 9/22/14.
 */
public class OverloadedMethodTest {
    OverloadedMethod method;

    public static class Employee {
        String name="employee";

        public Employee(String name) {
            this.name = name;
        }

        Employee(){}

        @Override
        public String toString() {
            return "Employee{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class SomeClass {


        String add(char c, char b) {
            return "addTwoChars_" + c + "_" + b;
        }

        String add(int c, int b) {
            return "addTwoInts_" + c + "_" + b;
        }

        String add(long c, long b) {
            return "addTwoLongs" + c + "_" + b;
        }


        String add(String c, String b) {
            return "addTwoStrings_" + c + "_" + b;
        }


        String add(Employee c, Employee b) {
            return "addTwoEmployees" + c + "_" + b;
        }

        String add(int c, long b) {
            return "addIntLong" + c + "_" + b;
        }

        String add(String c, char b) {
            return "addStringChar_" + c + "_" + b;
        }

    }

    @Before
    public void setup() {

        method = new OverloadedMethod();

        final ClassMeta<SomeClass> classMeta = ClassMeta.classMeta(SomeClass.class);
        for (MethodAccess ma : classMeta.methods("add")) {
            method.add(ma);
        }


    }


    @Test
    public void addStringAndChar() {
        String str = (String) method.invokeDynamic(new SomeClass(), "a", 'b');

        puts(str);
        equalsOrDie("addStringChar_a_b", str);

    }

    @Test
    public void testTwoInts() {
        String str = (String) method.invokeDynamic(new SomeClass(), 1, 2);

        equalsOrDie("addTwoInts_1_2", str);
        puts(str);
    }

    @Test
    public void testTwoChars() {
        String str = (String) method.invokeDynamic(new SomeClass(), 'a', 'b');

        equalsOrDie("addTwoChars_a_b", str);
        puts(str);
    }


    @Test
    public void testTwoStrings() {
        String str = (String) method.invokeDynamic(new SomeClass(), "a", "b");
        equalsOrDie("addTwoStrings_a_b", str);

        puts(str);
    }


    @Test
    public void testTwoLongs() {
        String str = (String) method.invokeDynamic(new SomeClass(), 1L, 1L);
        equalsOrDie("addTwoLongs1_1", str);

        puts(str);
    }


    @Test
    public void testTwoEmployee() {
        String str = (String) method.invokeDynamic(new SomeClass(), new Employee(), new Employee());

        puts(str);
        equalsOrDie("addTwoEmployeesEmployee{name='employee'}_Employee{name='employee'}", str);

    }

    @Test
    public void testTwoEmployeeWithMap() {
        String str = (String) method.invokeDynamic(new SomeClass(),

                Maps.map("name", "emp1"), Maps.map("name", "emp2"));

        puts(str);
        equalsOrDie("addTwoEmployeesEmployee{name='emp1'}_Employee{name='emp2'}", str);

    }

    @Test
    public void testTwoEmployeeWithList() {
        String str = (String) method.invokeDynamic(new SomeClass(),

                Lists.list("emp1"), Lists.list("emp2"));

        puts(str);
        equalsOrDie("addTwoEmployeesEmployee{name='emp1'}_Employee{name='emp2'}", str);

    }


    @Test
    public void addCharAndString() {
        String str = (String) method.invokeDynamic(new SomeClass(), 'a', "b");

        puts(str);
        equalsOrDie("addTwoStrings_a_b", str);

    }




    @Test
    public void addIntLong() {
        String str = (String) method.invokeDynamic(new SomeClass(), 1, 1L);

        puts(str);
        equalsOrDie("addIntLong1_1", str);

    }

    @Test
    public void addLongInt() {
        String str = (String) method.invokeDynamic(new SomeClass(), 1L, 1);

        puts(str);
        equalsOrDie("addTwoLongs1_1", str);

    }


    @Test
    public void addLongInt2() {
        String str = (String) method.invokeDynamic(new SomeClass(), Long.MAX_VALUE, 1);

        puts(str);
        equalsOrDie("addTwoLongs9223372036854775807_1", str);

    }
}
